package com.changgou.seckill.timer;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import entity.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 定时将秒杀商品存入Redis缓存
 * @Author Lee
 * @Date 2021/7/28
 */
@Component
public class SeckillGoodsPushTask {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 定时操作
     */
    @Scheduled(cron = "0/5 * * * * ?") //0s开始每5s执行一次
    public void loadGoodsPushRedis(){
        //求时间菜单
        List<Date> dateMenus = DateUtil.getDateMenus();

        //循环查询每个时间区间的秒杀商品
        for(Date dateMenu : dateMenus){
            //时间的字符串格式yyyyHHddHH
            String timespace = "SeckillGoods_" + DateUtil.data2str(dateMenu, "yyyyMMddHH");

            /**
             * 1.查询复合当前参与秒杀的时间菜单
             * 2.秒杀商品库存>0 stock_count
             * 3.审核状态>审核通过 status:1
             * 4.开始时间 start_time，结束时间end_time
             * 时间菜单的开始时间<=start_time && end_time<时间菜单的开始时间+2小时
             */
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();

            //审核状态>审核通过 status:1
            criteria.andEqualTo("status", "1");
            //秒杀商品库存>0 stock_count
            criteria.andGreaterThan("stockCount", 0);
            //时间菜单的开始时间<=start_time && end_time<时间菜单的开始时间+2小时
            criteria.andGreaterThanOrEqualTo("startTime", dateMenu);
            criteria.andLessThan("endTime", DateUtil.addDateHour(dateMenu, 2));

            //排除已经存入到Redis中的商品
            //求出当前命名空间下所有的商品的id
            Set keys = redisTemplate.boundHashOps(timespace).keys();
            if(keys != null && keys.size()>0){
                //排除
                criteria.andNotIn("id", keys);
            }

            //查询数据
            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);

            for(SeckillGoods seckillGood : seckillGoods){
                //存入Redis
                redisTemplate.boundHashOps(timespace).put(seckillGood.getId(), seckillGood);
//                System.out.println(seckillGood.getId());
            }
        }
    }
}
