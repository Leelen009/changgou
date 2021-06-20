package com.changgou.search.controller;

import com.changgou.search.service.SkuService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    /**
     * 数据导入
     * @return
     */
    @GetMapping(value = "/import")
    public Result importData(){
        skuService.importData();
        return new Result(true, StatusCode.OK, "导入数据到索引库成功！");
    }

    /**
     * 调用搜索实现
     * @param searchMap
     * @return
     */
    @GetMapping
    public Map<String, Object> search(@RequestParam(required = false) Map<String, String> searchMap)
                        throws Exception {
        return skuService.search(searchMap);
    }
}
