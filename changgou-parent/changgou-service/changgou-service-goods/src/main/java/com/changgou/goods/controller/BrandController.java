package com.changgou.goods.controller;

import com.changgou.goods.pojo.Brand;
import com.changgou.goods.service.BrandService;
import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author Leelen
 * @Description 品牌
 */
@RestController
@RequestMapping(value="/brand")
@CrossOrigin //跨域
public class BrandController {

    @Autowired
    private BrandService brandService;


    @GetMapping(value = "/{id}")
    public Result<Brand> findById(@PathVariable(value = "id")Integer id){
        Brand brand = brandService.findById(id);
        return new Result<Brand>(true, StatusCode.OK, "根据id查询品牌成功！", brand);
    }

    /**
     * 查询所有品牌
     * @return
     */
    @GetMapping
    public Result<List<Brand>> findAll (){
        List<Brand> brands = brandService.findAll();
        //响应结果封装
        return new Result<List<Brand>>(true, StatusCode.OK,
                "查询品牌集合成功！", brands);
    }

    /**
     * 增加品牌实现
     * @param brand
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Brand brand){
        brandService.add(brand);
        return new Result(true, StatusCode.OK, "增加品牌成功！");
    }

    /**
     * 更新品牌实现
     * @param brand
     * @return
     */
    @PutMapping(value = "/{id}")
    public Result update(@PathVariable(value = "id") Integer id, @RequestBody Brand brand){
        brand.setId(id);
        brandService.update(brand);
        return new Result(true, StatusCode.OK, "更新品牌成功！");
    }

    /**
     * 删除品牌实现
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable(value = "id") Integer id){
        brandService.delete(id);
        return new Result(true, StatusCode.OK, "删除品牌成功！");
    }

    @PostMapping(value = "/search")
    public Result<List<Brand>> findList(@RequestBody Brand brand){
        List<Brand> brands = brandService.findList(brand);
        return new Result<List<Brand>>(true, StatusCode.OK, "条件搜索查询成功！", brands);
    }

    /**
     * 分页查询
     * @param page 当前页
     * @param size 每一页显示条数
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    public Result<PageInfo<Brand>> findPage(@PathVariable(value = "page")Integer page,
                                            @PathVariable(value = "size")Integer size){
        PageInfo<Brand> pageInfo = brandService.findPage(page, size);
        return new Result<PageInfo<Brand>>(true, StatusCode.OK, "分页查询成功！", pageInfo);
    }

    /**
     * 分页+条件搜索实现
     * @param brand
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}")
    public Result<PageInfo<Brand>> findPage(@RequestBody Brand brand,
                                            @PathVariable(value = "page")Integer page,
                                            @PathVariable(value = "size")Integer size){
        PageInfo<Brand> pageInfo = brandService.findPage(brand, page, size);
        return new Result<PageInfo<Brand>>(true, StatusCode.OK, "分页查询成功！", pageInfo);
    }

    /**
     * 根据分类id查询品牌集合
     * @param categoryId
     * @return
     */
    @GetMapping(value = "/category/{id}")
    public Result<List<Brand>> findBrandByCategory(@PathVariable(value = "id")Integer categoryId){
        List<Brand> brands = brandService.findByCategory(categoryId);
        return new Result<List<Brand>>(true, StatusCode.OK, "查询成功！", brands);
    }
}
