package com.changgou.goods.dao;
import com.changgou.goods.pojo.Sku;
import com.changgou.order.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;
import org.apache.ibatis.annotations.Update;

/****
 * @Author:admin
 * @Description:Sku的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface SkuMapper extends Mapper<Sku> {

    /**
     * 库存递减
     * @param num
     * @return
     */
    @Update(value="update tb_sku set num=num-#{num},sale_num=sale_num+#{num} where id =#{skuId} and num >=#{num}")
    public int decrCount(@Param(value = "id") Long id, @Param(value = "num") Integer num);
}
