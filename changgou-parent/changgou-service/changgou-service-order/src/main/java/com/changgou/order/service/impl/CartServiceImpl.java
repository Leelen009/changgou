package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车服务实现
 * @author www.itheima.com
 * @version 1.0
 * @package com.changgou.order.service.impl *
 * @since 1.0
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    private RedisTemplate redisTemplate;

    //创建feign (SKUfeign SPU的feign)

    /**
     * 加入购物车实现
     * @param id  sku的ID
     * @param num 购买的数量
     * @param username  购买的商品的用户名
     */
    @Override
    public void add(Long id, Integer num, String username) {
        if(num<=0){
            //当加入购物车数量<=0的时候，需要删除掉原来的商品
            redisTemplate.boundHashOps("Cart_" + username).delete(id);

            //如果此时购物车数量为空，则连购物车一起移除
            Long size = redisTemplate.boundHashOps("Cart_" + username).size();
            if(size == null || size <= 0){
                redisTemplate.delete("Cart_" + username);
            }
            return;
        }

        //1.根据商品的SKU的ID 获取sku的数据
        Result<Sku> skuResult = skuFeign.findById(id);
        Sku sku = skuResult.getData();

        if (sku != null) {
            //2.根据sku的数据对象 获取 该SKU对应的SPU的数据
            Long spuId = sku.getSpuId();

            Result<Spu> spuResult = spuFeign.findById(spuId);
            Spu spu = spuResult.getData();

            //3.将数据存储到 购物车对象(order_item)中
            OrderItem orderItem = createOrderItem(id, num, sku, spu);

            //4.数据添加到redis中  key:用户名 field:sku的ID  value:购物车数据(order_item)
            redisTemplate.boundHashOps("Cart_" + username).put(id, orderItem);// hset key field value   hget key field
        }
    }

    /**
     * 购物车列表
     * @param username
     * @return
     */
    @Override
    public List<OrderItem> list(String username) {
        List<OrderItem> orderItemList = redisTemplate.boundHashOps("Cart_" + username).values();
        return orderItemList;
    }

    /**
     * 创建一个OrderItem对象
     * @param id
     * @param num
     * @param sku
     * @param spu
     * @return
     */
    public OrderItem createOrderItem(Long id, Integer num, Sku sku, Spu spu) {
        OrderItem orderItem = new OrderItem();
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId());
        orderItem.setSkuId(id);
        orderItem.setName(sku.getName());//商品的名称  sku的名称
        orderItem.setPrice(sku.getPrice());//sku的单价
        orderItem.setNum(num);//购买的数量
        orderItem.setMoney(orderItem.getNum() * orderItem.getPrice());//总金额=单价* 数量
        orderItem.setPayMoney(orderItem.getNum() * orderItem.getPrice());//应付金额=单价* 数量
        orderItem.setImage(sku.getImage());//商品的图片地址
        return orderItem;
    }
}
