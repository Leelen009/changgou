package com.changgou.goods.service;
import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.PageInfo;
import java.util.List;
/****
 * @Author:admin
 * @Description:Spu业务层接口
 * @Date 2019/6/14 0:16
 *****/
public interface SpuService {

    /***
     * Spu多条件分页查询
     * @param spu
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(Spu spu, int page, int size);

    /***
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(int page, int size);

    /***
     * Spu多条件搜索方法
     * @param spu
     * @return
     */
    List<Spu> findList(Spu spu);

    /***
     * 删除Spu
     * @param id
     */
    void delete(Long id);

    /***
     * 修改Spu数据
     * @param spu
     */
    void update(Spu spu);

    /***
     * 新增Spu
     * @param spu
     */
    void add(Spu spu);

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
     Spu findById(Long id);

    /**
     * 查询所有Spu
     * @return
     */
    List<Spu> findAll();

    /**
     * 添加商品(SPU+ SKUlIST)
     * @param goods   update  add
     */
    void save(Goods goods);

    /**
     * 根据点击到的商品(SPU)的ID获取到GOODS数据
     * @param id
     * @return
     */
    Goods findGoodsById(Long id);

    /**
     * 商品审核 自动上架
     * @param id
     */
    void auditSpu(Long id);

    /**
     * 下架
     * @param id
     */
    void pullSpu(Long id);

    /**
     * 上架
     * @param id
     */
    void putSpu(Long id);

    /**
     * 批量上架
     * @param ids 要上架的所有商品的spuId
     */
    void putMany(Long[] ids);


    /**
     * 逻辑删除 设置isDelete=1
     * @param id
     */
    void logicDeleteSpu(Long id);

    /**
     * 还原
     * @param id
     */
    void restoreSpu(Long id);
}
