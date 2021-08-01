package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.order.dao.OrderItemMapper;
import com.changgou.order.dao.OrderMapper;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.OrderService;
import com.changgou.user.feign.UserFeign;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.IdWorker;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/****
 * @Author:admin
 * @Description:Order业务层接口实现类
 * @Date 2019/6/14 0:16
 *****/
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Order条件+分页查询
     * @param order 查询条件
     * @param page  页码
     * @param size  页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Order> findPage(Order order, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        Example example = createExample(order);
        //执行搜索
        return new PageInfo<Order>(orderMapper.selectByExample(example));
    }

    /**
     * Order分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Order> findPage(int page, int size) {
        //静态分页
        PageHelper.startPage(page, size);
        //分页查询
        return new PageInfo<Order>(orderMapper.selectAll());
    }

    /**
     * Order条件查询
     * @param order
     * @return
     */
    @Override
    public List<Order> findList(Order order) {
        //构建查询条件
        Example example = createExample(order);
        //根据构建的条件查询数据
        return orderMapper.selectByExample(example);
    }

    /**
     * Order构建查询对象
     * @param order
     * @return
     */
    public Example createExample(Order order) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if (order != null) {
            // 订单id
            if (!StringUtils.isEmpty(order.getId())) {
                criteria.andEqualTo("id", order.getId());
            }
            // 数量合计
            if (!StringUtils.isEmpty(order.getTotalNum())) {
                criteria.andEqualTo("totalNum", order.getTotalNum());
            }
            // 金额合计
            if (!StringUtils.isEmpty(order.getTotalMoney())) {
                criteria.andEqualTo("totalMoney", order.getTotalMoney());
            }
            // 优惠金额
            if (!StringUtils.isEmpty(order.getPreMoney())) {
                criteria.andEqualTo("preMoney", order.getPreMoney());
            }
            // 邮费
            if (!StringUtils.isEmpty(order.getPostFee())) {
                criteria.andEqualTo("postFee", order.getPostFee());
            }
            // 实付金额
            if (!StringUtils.isEmpty(order.getPayMoney())) {
                criteria.andEqualTo("payMoney", order.getPayMoney());
            }
            // 支付类型，1、在线支付、0 货到付款
            if (!StringUtils.isEmpty(order.getPayType())) {
                criteria.andEqualTo("payType", order.getPayType());
            }
            // 订单创建时间
            if (!StringUtils.isEmpty(order.getCreateTime())) {
                criteria.andEqualTo("createTime", order.getCreateTime());
            }
            // 订单更新时间
            if (!StringUtils.isEmpty(order.getUpdateTime())) {
                criteria.andEqualTo("updateTime", order.getUpdateTime());
            }
            // 付款时间
            if (!StringUtils.isEmpty(order.getPayTime())) {
                criteria.andEqualTo("payTime", order.getPayTime());
            }
            // 发货时间
            if (!StringUtils.isEmpty(order.getConsignTime())) {
                criteria.andEqualTo("consignTime", order.getConsignTime());
            }
            // 交易完成时间
            if (!StringUtils.isEmpty(order.getEndTime())) {
                criteria.andEqualTo("endTime", order.getEndTime());
            }
            // 交易关闭时间
            if (!StringUtils.isEmpty(order.getCloseTime())) {
                criteria.andEqualTo("closeTime", order.getCloseTime());
            }
            // 物流名称
            if (!StringUtils.isEmpty(order.getShippingName())) {
                criteria.andEqualTo("shippingName", order.getShippingName());
            }
            // 物流单号
            if (!StringUtils.isEmpty(order.getShippingCode())) {
                criteria.andEqualTo("shippingCode", order.getShippingCode());
            }
            // 用户名称
            if (!StringUtils.isEmpty(order.getUsername())) {
                criteria.andLike("username", "%" + order.getUsername() + "%");
            }
            // 买家留言
            if (!StringUtils.isEmpty(order.getBuyerMessage())) {
                criteria.andEqualTo("buyerMessage", order.getBuyerMessage());
            }
            // 是否评价
            if (!StringUtils.isEmpty(order.getBuyerRate())) {
                criteria.andEqualTo("buyerRate", order.getBuyerRate());
            }
            // 收货人
            if (!StringUtils.isEmpty(order.getReceiverContact())) {
                criteria.andEqualTo("receiverContact", order.getReceiverContact());
            }
            // 收货人手机
            if (!StringUtils.isEmpty(order.getReceiverMobile())) {
                criteria.andEqualTo("receiverMobile", order.getReceiverMobile());
            }
            // 收货人地址
            if (!StringUtils.isEmpty(order.getReceiverAddress())) {
                criteria.andEqualTo("receiverAddress", order.getReceiverAddress());
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if (!StringUtils.isEmpty(order.getSourceType())) {
                criteria.andEqualTo("sourceType", order.getSourceType());
            }
            // 交易流水号
            if (!StringUtils.isEmpty(order.getTransactionId())) {
                criteria.andEqualTo("transactionId", order.getTransactionId());
            }
            // 订单状态,0:未完成,1:已完成，2：已退货
            if (!StringUtils.isEmpty(order.getOrderStatus())) {
                criteria.andEqualTo("orderStatus", order.getOrderStatus());
            }
            // 支付状态,0:未支付，1：已支付，2：支付失败
            if (!StringUtils.isEmpty(order.getPayStatus())) {
                criteria.andEqualTo("payStatus", order.getPayStatus());
            }
            // 发货状态,0:未发货，1：已发货，2：已收货
            if (!StringUtils.isEmpty(order.getConsignStatus())) {
                criteria.andEqualTo("consignStatus", order.getConsignStatus());
            }
            // 是否删除
            if (!StringUtils.isEmpty(order.getIsDelete())) {
                criteria.andEqualTo("isDelete", order.getIsDelete());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Order
     * @param order
     */
    @Override
    public void update(Order order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 增加Order
     * @param order
     */
    @Override
    public Order add(Order order) {
        //1.添加订单表的数据
        order.setId(idWorker.nextId() + ""); //主键id

        List<OrderItem> values = new ArrayList<OrderItem>();

        for(Long skuId : order.getSkuIds()){
            //将需要下单的商品添加到values中
            values.add((OrderItem) redisTemplate.boundHashOps("Cart_" + order.getUsername()).get(skuId));
            //获取勾选的商品id，需要下单的商品，将要下单的商品的id信息从购物车中移除
            redisTemplate.boundHashOps("Cart_" + order.getUsername()).delete(skuId);
        }

        Integer totalNum = 0;//购买总数量
        Integer totalMoney = 0;//购买的总金额

        //封装Map 递减数据
        Map<String, Integer> decrmap = new HashMap<String, Integer>();

        for (OrderItem orderItem : values) {
            totalNum += orderItem.getNum();//购买的数量
            totalMoney += orderItem.getMoney();//金额

            //2.添加订单选项表的数据
            orderItem.setId(idWorker.nextId() + "");//订单明细的iD
            orderItem.setOrderId(order.getId());//订单的iD
            orderItem.setIsReturn("0");//0：未退货
            orderItemMapper.insertSelective(orderItem);
            //3.减少库存  调用goods 微服务的 feign 减少库存
//            skuFeign.decrCount(orderItem);
            decrmap.put(orderItem.getSkuId().toString(), orderItem.getNum());
        }

        order.setTotalNum(totalNum);//设置总数量
        order.setTotalMoney(totalMoney);//设置总金额
        order.setPayMoney(totalMoney);//设置实付金额
        order.setCreateTime(new Date()); //创建时间
        order.setUpdateTime(order.getCreateTime());//修改时间

        order.setSourceType("1");//订单来源 1：web
        order.setOrderStatus("0");//0:未完成
        order.setPayStatus("0");//未支付
        order.setConsignStatus("0");//未发货
        order.setIsDelete("0");//未删除

        orderMapper.insertSelective(order);

        //添加订单信息
        orderMapper.insertSelective(order);
        //循环添加订单明细信息
        for(OrderItem orderItem : values){
            orderItemMapper.insertSelective(orderItem);
        }

        //库存递减
        skuFeign.decrCount(decrmap);

        //4.增加积分  调用用户微服务的userfeign 增加积分
        userFeign.addPoints(10, order.getUsername());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(simpleDateFormat.format(new Date()));

        //添加订单
        rabbitTemplate.convertAndSend("orderDealyQueue", (Object) order.getId(),
                new MessagePostProcessor() {
                    @Override
                    public Message postProcessMessage(Message message) throws AmqpException {
                        //设置延时读取
                        message.getMessageProperties().setExpiration("10000");
                        return message;
                    }
                });

        //5.清空当前的用户的redis中的购物车
        redisTemplate.delete("Cart_" + order.getUsername());

        return order;
    }

    /**
     * 根据ID查询Order
     * @param id
     * @return
     */
    @Override
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Order全部数据
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     *
     * @param out_trade_no 订单号
     * @param paytime 支付时间
     * @param transaction_id 交易流水号
     */
    @Override
    public void updateStatus(String out_trade_no, String paytime, String transaction_id) throws ParseException {
        //时间转换
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMMMddHHmmss");
        Date payTimeInfo = simpleDateFormat.parse(paytime);

        //根据id 获取订单的数据
        Order order = orderMapper.selectByPrimaryKey(out_trade_no);
        //更新
//        order.setUpdateTime(new Date());

        order.setPayTime(payTimeInfo); //支付的时间  从微信的参数中获取
//        order.setOrderStatus("1");
        order.setPayStatus("1"); //支付状态 0未支付 1已支付
        order.setTransactionId(transaction_id); //交易流水号
        //3.更新到数据库
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     * 删除订单
     * @param outtradeno
     */
    @Override
    public void deleteOrder(String outtradeno) {
        Order order = orderMapper.selectByPrimaryKey(outtradeno);

        //修改状态
        order.setUpdateTime(new Date());
        order.setPayStatus("2"); //支付失败
        //修改到数据库中
        orderMapper.updateByPrimaryKey(order);

        //回滚库存->作业->调用goods微服务

    }
}
