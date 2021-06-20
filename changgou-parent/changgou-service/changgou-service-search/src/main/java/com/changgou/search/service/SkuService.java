package com.changgou.search.service;

import java.util.Map;

public interface SkuService {

    /**
     * 导入数据
     */
    void importData();

    /**
     * 条件搜索
     * @param searchMap
     * @return Map
     */
    Map<String, Object> search(Map<String, String> searchMap);
}
