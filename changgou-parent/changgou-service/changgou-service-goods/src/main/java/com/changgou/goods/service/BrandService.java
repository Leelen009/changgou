package com.changgou.goods.service;

import com.changgou.goods.pojo.Brand;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface BrandService {

    /**
     * 根据id查询
     */
    Brand findById(Integer id);

    /***
     * 查询所有
     */
    List<Brand> findAll();

    /**
     * 增加品牌
     */
    void add(Brand brand);

    /**
     * 修改品牌
     */
    void update(Brand brand);

    /**
     * 根据id删除品牌
     */
    void delete(Integer id);

    /**
     * 根据品牌信息多条件搜索
     * @Param brand
     */
    List<Brand> findList(Brand brand);

    /**
     * 分页搜索
     * @param page
     * @param size
     * @return
     */
    PageInfo<Brand> findPage(Integer page, Integer size);

    /**
     * 分页+条件搜索
     * @param brand
     * @param page
     * @param size
     * @return
     */
    PageInfo<Brand> findPage(Brand brand, Integer page, Integer size);

    /**
     * 根据分类id查询品牌集合
     * @param categoryId 分类id
     * @return
     */
    List<Brand> findByCategory(Integer categoryId);
}
