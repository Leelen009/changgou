package com.changgou.goods.service;

import com.changgou.goods.pojo.CategoryBrand;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @Description CategoryBrand业务层接口
 */
public interface CategoryBrandService {

    /**
     * 多条件+分页查询
     * @param categoryBrand
     * @param page
     * @param size
     * @return
     */
    PageInfo<CategoryBrand> findPage(CategoryBrand categoryBrand, int page, int size);

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<CategoryBrand> findPage(int page, int size);

    /**
     * 多条件搜索
     * @param categoryBrand
     * @return
     */
    List<CategoryBrand> findList(CategoryBrand categoryBrand);

    /**
     * 删除
     * @param id
     */
    void delete(Integer id);

    /**
     * 修改
     * @param categoryBrand
     */
    void update(CategoryBrand categoryBrand);

    /**
     * 新增
     * @param categoryBrand
     */
    void add(CategoryBrand categoryBrand);

    /**
     * 根据id查询
     * @param id
     * @return
     */
    CategoryBrand findById(Integer id);

    /**
     * 查询所有CategoryBrand
     * @return
     */
    List<CategoryBrand> findAll();
}
