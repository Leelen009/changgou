package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通过goods微服务的"/sku"的findAll查询全部sku
 */
@FeignClient(value = "goods")
@RequestMapping("/sku")
public interface SkuFeign {

    /**
     * 查询Sku全部数据
     * @return  d
     */
    @GetMapping
    Result<List<Sku>> findAll();

    /**
     * 查询符合条件的状态的sku列表
     * @param status
     * @return
     */
    @GetMapping("/status/{status}")
    public Result<List<Sku>> findByStatus(@PathVariable(name = "status") String status);

    /**
     * 根据id查询sku
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Sku> findById(@PathVariable(name = "id") Long id);

    /**
     *
     * @param sku
     * @return
     */
    @PostMapping("/search")
    public Result<List<Sku>> findList(@RequestBody(required = false) Sku sku);

    /**
     * 库存递减
     * @param decrmap
     * @return
     */
    @GetMapping(value="/decr/count")
    public Result decrCount(@RequestParam Map<String, Integer> decrmap);
}
