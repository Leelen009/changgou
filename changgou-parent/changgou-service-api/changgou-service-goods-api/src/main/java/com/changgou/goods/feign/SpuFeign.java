package com.changgou.goods.feign;

import com.changgou.goods.pojo.Spu;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "goods")
@RequestMapping("/spu")
public interface SpuFeign {
    /**
     * 根据id查询spu
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Spu> findById(@PathVariable(name = "id") Long id);
}
