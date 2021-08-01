package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.swing.*;
import java.util.*;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    //ElasticsearchTemplate：可以实现索引库的增删改查【高级搜索】
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 导入索引库
     */
    @Override
    public void importData() {
        //Feign调用，查询List<Sku>
        Result<List<Sku>> skuResult = skuFeign.findAll();
        /**
         * 将List<Sku>转成List<SkuInfo>
         * List<Sku> -> [{skuJSON}] -> List<SkuInfo>
         */
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuResult.getData()),
                SkuInfo.class);

        //循环当前的SkuInfoList
        for(SkuInfo skuInfo : skuInfoList){
            //获取spec -> Map(String) -> Map类型
            Map<String, Object> specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(specMap);
        }
        //调用Dao实现数据批量导入
        skuEsMapper.saveAll(skuInfoList);
    }

    /**
     * 多条件搜索
     * @param searchMap
     * @return Map
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        //搜索条件封装
        NativeSearchQueryBuilder nativeSearchQueryBuilder = buildBasicQuery(searchMap);

        //集合搜索
        Map<String, Object> resultMap = searchList(nativeSearchQueryBuilder);

        //当用户选择了分类，将分类作为搜索条件，则不需要对分类进行分组搜索，因为分组搜索的数据是用于显示分类搜索条件的
        //分类->searchMap->category
        if(searchMap==null || StringUtils.isEmpty(searchMap.get("category"))){
            //分类分组查询
            List<String> categoryList = searchCategoryList(nativeSearchQueryBuilder);
            resultMap.put("categoryList", categoryList);
        }

        //当用户选择了品牌，将品牌作为搜索条件，则不需要对品牌进行分组搜索，因为分组搜索的数据是用于显示品牌搜索条件的
        //品牌->searchMap->brand
        if(searchMap==null || StringUtils.isEmpty(searchMap.get("brand"))){
            //查询品牌集合
            List<String> brandList = searchBrandList(nativeSearchQueryBuilder);
            resultMap.put("brandList", brandList);
        }

        //规格查询
        Map<String, Set<String>> specMap = searchSpecList(nativeSearchQueryBuilder);
        resultMap.put("specMap", specMap);

        return resultMap;
    }

    /**
     * 搜索条件封装 方法
     * @param searchMap
     * @return
     */
    public NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        //NativeSearchQueryBuilder：搜索条件构建对象，用于封装各种搜索条件
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        //BoolQueryBuilder must,must_not,should
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if(searchMap!=null && searchMap.size()>0){
            //根据关键词搜索
            String keywords = searchMap.get("keywords");

            //如果关键词不为空，则搜索关键词数据
            if(!StringUtils.isEmpty(keywords)) {
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }

            //输入了分类
            if(!StringUtils.isEmpty(searchMap.get("category"))) {
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName", searchMap.get("category")));
            }

            //输入了品牌
            if(!StringUtils.isEmpty(searchMap.get("brand"))) {
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
            }

            //规格过滤实现：spec_
            for(Map.Entry<String, String> entry : searchMap.entrySet()){
                String key = entry.getKey();
                //如果key以spec_开始，则表示规格筛选查询
                if(key.startsWith("spec_")){
                    String value = entry.getValue();
                    //spec_xx去掉前面5位
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword",
                            value));
                }
            }

            //价格区间筛选
            String price = searchMap.get("price");
            if(!StringUtils.isEmpty(price)){
                //去掉中文“元”、”以上“
                price = price.replace("元", "").replace("以上", "");
                //根据“-”分割
                String[] prices = price.split("-");
                if(prices != null && prices.length > 0){
                    //price>prices[0] gt: greater than
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(prices[0])));
                    //price<prices[1] lte: less than or equal to
                    if(prices.length == 2){
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.parseInt(prices[1])));
                    }
                }
            }

            //排序实现
            String sortField = searchMap.get("sortField"); //指定排序的域
            String sortRule = searchMap.get("sortRule"); //指定排序的规则
            if(!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRule)){
                nativeSearchQueryBuilder.withSort(
                        new FieldSortBuilder(sortField) //指定排序域
                                .order(SortOrder.valueOf(sortRule))); //指定排序规则
            }
        }

        //分页，用户如果不传入分页参数，则默认为第1页
        Integer pageNumber =coverterPage(searchMap);
        //每页的数量，用户如果不传入数量参数，则默认为10
        Integer pageSize = coverterSize(searchMap);
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNumber - 1, pageSize));

        //将boolQueryBuilder填充给nativeSearchQueryBuilder
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        return nativeSearchQueryBuilder;
    }

    /**
     * 结果集搜索 方法
     * @param nativeSearchQueryBuilder
     * @return
     */
    public Map<String, Object> searchList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {

        //高亮配置
        HighlightBuilder.Field field = new HighlightBuilder.Field("name"); //指定高亮域
        //前缀 <em style="color: red;">
        field.preTags("<em style=\"color: red;\">");
        //后缀 </em>
        field.postTags("</em>");
        //碎片长度 关键词数据的长度
        field.fragmentSize(100);

        //添加高亮
        nativeSearchQueryBuilder.withHighlightFields(field);

        /**
         * 执行搜索，响应结果
         * 1.搜索条件封装对象
         * 2.搜索的结果集（集合数据）需要转换的类型
         * AggregatedPage<SkuInfo>：搜索结果集的封装
         */
        AggregatedPage<SkuInfo> page = elasticsearchTemplate
                .queryForPage(
                        nativeSearchQueryBuilder.build(), //搜索条件封装
                        SkuInfo.class,                    //数据集合要转换的类型的字节码
                        new SearchResultMapper() {        //执行搜索后，将数据结果集封装到该对象中
                            @Override
                            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass,
                                                                    Pageable pageable) {
                                //存储所有转换后的高亮数据对象
                                List<T> list = new ArrayList<T>();

                                //执行查询，获取所有数据->结果集[非高亮数据|高亮数据]
                                for(SearchHit hit : searchResponse.getHits()){
                                    //分析结果集数据，获取[非高亮]数据
                                    SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                                    //分析结果集数据，获取高亮数据->只有某个域的高亮数据
                                    HighlightField highlightField = hit.getHighlightFields().get("name");
                                    if(highlightField != null && highlightField.getFragments() != null){
                                        //高亮数据读取出来
                                        Text[] fragments = highlightField.getFragments();
                                        StringBuffer buffer = new StringBuffer();
                                        for(Text fragment : fragments){
                                            buffer.append(fragment.toString());
                                        }
                                        //非高亮数据中指定的域替换成高亮数据
                                        skuInfo.setName(buffer.toString());
                                    }
                                    //将高亮数据添加到集合
                                    list.add((T) skuInfo);
                                }
                                //将数据返回
                                /**
                                 *  AggregatedPageImpl<T>
                                 *  1)搜索的集合数据: (携带高亮)List<T> content
                                 *  2)分页对象信息: Pageable pageable
                                 *  3)搜索记录的总条数：long total
                                 */
                                return new AggregatedPageImpl<T>(list, pageable, searchResponse.getHits().getTotalHits());
                            }
                        }
                );

        //获取搜索封装信息
        NativeSearchQuery query = nativeSearchQueryBuilder.build();
        Pageable pageable = query.getPageable();
        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();

        //分页参数-总记录数
        long totalElements = page.getTotalElements();
        //总页数
        int totalPages = page.getTotalPages();
        //获取数据结果集
        List<SkuInfo> contents = page.getContent();
        //封装一个Map存储所有数据，并返回
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("rows", contents);
        resultMap.put("total", totalElements);
        resultMap.put("totalPages", totalPages);

        //分页数据
        resultMap.put("pageSize", pageSize);
        resultMap.put("pageNumber", pageNumber);

        return resultMap;
    }

    /**
     * 分类分组查询 方法
     * @param nativeSearchQueryBuilder
     * @return
     */
    public List<String> searchCategoryList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        //分组查询分类集合
        //addAggregation：添加一个聚合操作
        //1)取别名
        //2)表示根据哪个域进行分组
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory")
                .field("categoryName"));

        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate
                .queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        //获取分组
        //aggregatedPage.getAggregations()：获取的是集合，可以根据多个域进行分组
        //get("skuCategory")：获取指定域的集合数
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuCategory");
        List<String> categoryList = new ArrayList<String>();
        for(StringTerms.Bucket bucket : stringTerms.getBuckets()){
            String categoryName = bucket.getKeyAsString(); //其中的一个分类名字
            categoryList.add(categoryName);
        }
        return categoryList;
    }

    /**
     * 品牌分组查询 方法
     * @param nativeSearchQueryBuilder
     * @return
     */
    public List<String> searchBrandList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        //分组查询品牌 集合
        //addAggregation：添加一个聚合操作
        //1)取别名
        //2)表示根据哪个域进行分组
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand")
                .field("brandName"));

        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate
                .queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        //获取分组
        //aggregatedPage.getAggregations()：获取的是集合，可以根据多个域进行分组
        //get("skuCategory")：获取指定域的集合数
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuBrand");
        List<String> brandList = new ArrayList<String>();
        for(StringTerms.Bucket bucket : stringTerms.getBuckets()){
            String brandName = bucket.getKeyAsString(); //其中的一个品牌名字
            brandList.add(brandName);
        }
        return brandList;
    }

    /**
     * 规格分组查询 方法
     * @param nativeSearchQueryBuilder
     * @return
     */
    public Map<String, Set<String>> searchSpecList(NativeSearchQueryBuilder nativeSearchQueryBuilder){
        //分组查询规格 集合
        //addAggregation：添加一个聚合操作
        //1)取别名
        //2)表示根据哪个域进行分组 .keyword：不要;
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders
                .terms("skuSpec").field("spec.keyword").size(10000));

        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate
                .queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        //获取分组数据
        //aggregatedPage.getAggregations()：获取的是集合，可以根据多个域进行分组
        //get("skuSpec")：获取指定域的集合数
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuSpec");
        List<String> specList = new ArrayList<String>();
        for(StringTerms.Bucket bucket : stringTerms.getBuckets()){
            String specName = bucket.getKeyAsString(); //其中的一个规格名字
            specList.add(specName);
        }

        //合并后的Map对象
        Map<String, Set<String>> allSpec = new HashMap<String,Set<String>>();

        //1.循环specList
        for(String spec : specList){
            //2.将每个JSON字符串转成Map
            Map<String,String> specMap = JSON.parseObject(spec, Map.class);
            //3.将每个Map对象合成一个Map<String,Set<String>>
            //4. 合并流程
            //4.1 循环所有Map
            for(Map.Entry<String,String> entry : specMap.entrySet()){
                //4.2 取出当前Map，并且获取对应的Key以及对应value
                String key = entry.getKey(); //规格名字
                String value = entry.getValue(); //规格值
                //4.3 将当前循环的数据合并到一个Map<String,Set<String>>中
                Set<String> specSet = allSpec.get(key);
                if(specSet==null){
                    //之前allSpec中没有该规格
                    specSet = new HashSet<String>();
                }
                specSet.add(value);
                allSpec.put(key, specSet);
            }
        }
        return allSpec;
    }

    /**
     * 接收前端传入的分页参数
     * @param searchMap
     * @return
     */
    public Integer coverterPage(Map<String, String> searchMap){
        if(searchMap != null){
            String pageNum = searchMap.get("pageNum");
            try{
                return Integer.parseInt(pageNum);
            } catch (NumberFormatException e){
            }
        }
        return 1;
    }

    /**
     * 接收前端传入的每一页的数量
     * @param searchMap
     * @return
     */
    public Integer coverterSize(Map<String, String> searchMap){
        if(searchMap != null){
            String size = searchMap.get("size");
            try{
                return Integer.parseInt(size);
            } catch (NumberFormatException e){
            }
        }
        return 10;
    }
}
