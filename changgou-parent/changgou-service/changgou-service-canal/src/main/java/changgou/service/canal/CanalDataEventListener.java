package changgou.service.canal;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @Description 实现MYSQL数据监听
 */
@CanalEventListener
public class CanalDataEventListener {

    @Autowired
    private ContentFeign contentFeign;

    @Autowired
    private StringRedisTemplate stringRedisTemple;


    /**
     *
     * @param eventType
     * @param rowData
     */
    @ListenPoint(
            destination = "example",
            schema = "changgou_content",
            table = {"tb_content"},
            eventType = {CanalEntry.EventType.UPDATE,
                    CanalEntry.EventType.INSERT,
                    CanalEntry.EventType.DELETE}
    )
    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        //1.获取被修改的category_id
        String categoryId = getColumnValue(eventType, rowData);
        //2.调用feign获取数据
        Result<List<Content>> byCategory = contentFeign.findByCateogry(Long.valueOf(categoryId));
        //3.存储到redis中
        List<Content> data = byCategory.getData();
        stringRedisTemple.boundValueOps("content_" + categoryId).set(JSON.toJSONString(data));
    }

    /**
     * getColumnValue()
     * @param eventType
     * @param rowData
     * @return
     */
    private String getColumnValue(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //1.判断更改类型 如果是删除 则需要获取到before的数据
        String categoryId = "";
        if (CanalEntry.EventType.DELETE == eventType) {
            List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
            for (CanalEntry.Column column : beforeColumnsList) {
                //column.getName(列的名称   column.getValue() 列对应的值
                if (column.getName().equals("category_id")) {
                    categoryId = column.getValue();
                    return categoryId;
                }
            }
        } else {
            //2判断是 更新 新增 获取after的数据
            List<CanalEntry.Column> beforeColumnsList = rowData.getAfterColumnsList();
            for (CanalEntry.Column column : beforeColumnsList) {
                //column.getName(列的名称   column.getValue() 列对应的值
                if (column.getName().equals("category_id")) {
                    categoryId = column.getValue();
                    return categoryId;
                }
            }
        }
        //3.返回
        return categoryId;
    }

    //监听商品数据库的spu的表的数据的变化,变了,调用feign 生成静态页就可以了

    @Autowired
    private PageFeign pageFeign;

    @ListenPoint(destination = "example",
            schema = "changgou_goods",
            table = {"tb_spu"},
            eventType = {CanalEntry.EventType.UPDATE, CanalEntry.EventType.INSERT, CanalEntry.EventType.DELETE})
    public void onEventCustomSpu(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {

        //判断操作类型
        if (eventType == CanalEntry.EventType.DELETE) {
            String spuId = "";
            List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
            for (CanalEntry.Column column : beforeColumnsList) {
                if (column.getName().equals("id")) {
                    spuId = column.getValue();//spuid
                    break;
                }
            }
            //todo 删除静态页

        } else {
            //新增 或者 更新
            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
            String spuId = "";
            for (CanalEntry.Column column : afterColumnsList) {
                if (column.getName().equals("id")) {
                    spuId = column.getValue();
                    break;
                }
            }
            //更新 生成静态页
            pageFeign.createHtml(Long.valueOf(spuId));
        }
    }

    /**
     * 增加数据监听
     * @param eventType
     * @param rowData
     */
    /*@InsertListenPoint
    public void onEventInsert(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        for(CanalEntry.Column column : rowData.getAfterColumnsList()){
            System.out.println("列名：" + column.getName() + "------变更的数据:" +
                column.getValue());
        }
    }*/

    /**
     * 修改数据监听
     * @param eventType
     * @param rowData
     */
    /*@UpdateListenPoint
    public void onEventUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        for(CanalEntry.Column column : rowData.getBeforeColumnsList()){
            System.out.println("修改前：列名：" + column.getName() + "------变更的数据:" +
                    column.getValue());
        }

        for(CanalEntry.Column column : rowData.getAfterColumnsList()){
            System.out.println("修改后：列名：" + column.getName() + "------变更的数据:" +
                    column.getValue());
        }
    }*/

    /**
     * 删除数据监听
     * @param eventType
     * @param rowData
     */
    /*@DeleteListenPoint
    public void onEventDelete(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        for(CanalEntry.Column column : rowData.getBeforeColumnsList()){
            System.out.println("删除前：列名：" + column.getName() + "------变更的数据:" +
                    column.getValue());
        }
    }*/

    /**
     * 自定义监听
     * @param eventType
     * @param rowData
     */
    /*@ListenPoint(
        eventType = {CanalEntry.EvenType.DELETE, CanalEntry.EventType.Update},
        schema = {"changgou_content"}, //指定监听的数据
        table = {"tb_content"},        //指定监听的表
        destination = "example"        //指定实例的地址
    )
    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData){
        for(CanalEntry.Column column : rowData.getBeforeColumnsList()){
            System.out.println("===自定义操作前：列名：" + column.getName() + "------变更的数据:" +
                    column.getValue());
        }

        for(CanalEntry.Column column : rowData.getAfterColumnsList()){
            System.out.println("===自定义操作后：列名：" + column.getName() + "------变更的数据:" +
                    column.getValue());
        }
    }*/
}
