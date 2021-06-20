package com.changgou.goods.service.impl;

import com.changgou.goods.dao.CategoryBrandMapper;
import com.changgou.goods.pojo.CategoryBrand;
import com.changgou.goods.service.CategoryBrandService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Description CategoryBrand业务层接口实现类
 */
@Service
public class CategoryBrandServiceImpl implements CategoryBrandService {

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    /**
     * CategoryBrand多条件+分页查询
     * @param categoryBrand
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<CategoryBrand> findPage(CategoryBrand categoryBrand, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(categoryBrand);
        return new PageInfo<CategoryBrand>(categoryBrandMapper.selectByExample(example));
    }

    /**
     * CategoryBrand分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<CategoryBrand> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        return new PageInfo<CategoryBrand>(categoryBrandMapper.selectAll());
    }

    /**
     * CategoryBrand条件搜索
     * @param categoryBrand
     * @return
     */
    @Override
    public List<CategoryBrand> findList(CategoryBrand categoryBrand) {
        Example example = createExample(categoryBrand);
        return categoryBrandMapper.selectByExample(example);
    }

    /**
     * CategoryBrand构建查询对象
     * @param categoryBrand
     * @return
     */
    private Example createExample(CategoryBrand categoryBrand) {
        Example example=new Example(CategoryBrand.class);
        Example.Criteria criteria = example.createCriteria();
        if(categoryBrand!=null){
            // 分类ID
            if(!StringUtils.isEmpty(categoryBrand.getCategoryId())){
                criteria.andEqualTo("categoryId",categoryBrand.getCategoryId());
            }
            // 品牌ID
            if(!StringUtils.isEmpty(categoryBrand.getBrandId())){
                criteria.andEqualTo("brandId",categoryBrand.getBrandId());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Integer id) {
        categoryBrandMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改CategoryBrand
     * @param categoryBrand
     */
    @Override
    public void update(CategoryBrand categoryBrand) {
        categoryBrandMapper.updateByPrimaryKey(categoryBrand);
    }

    /**
     * 增加CategoryBrand
     * @param categoryBrand
     */
    @Override
    public void add(CategoryBrand categoryBrand) {
        categoryBrandMapper.insert(categoryBrand);
    }

    /**
     * 根据id查询CategoryBrand
     * @param id
     * @return
     */
    @Override
    public CategoryBrand findById(Integer id) {
        return categoryBrandMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询CategoryBrand全部数据
     * @return
     */
    @Override
    public List<CategoryBrand> findAll() {
        return categoryBrandMapper.selectAll();
    }
}
