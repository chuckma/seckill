package com.lucasma.seckill.service;

import com.lucasma.seckill.dao.OrderDao;
import com.lucasma.seckill.domain.OrderInfo;
import com.lucasma.seckill.domain.SeckillOrder;
import com.lucasma.seckill.domain.SeckillUser;
import com.lucasma.seckill.redis.OrderKey;
import com.lucasma.seckill.redis.RedisService;
import com.lucasma.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Author: lucasma
 */

@Service
public class OrderService {


    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisService redisService;
    public SeckillOrder getSeckillOrderByUserIdGoodsId(Long userId, long goodsId) {
        return orderDao.getSeckillOrderByUserIdGoodsId(userId, goodsId);
    }

    @Transactional
    public OrderInfo createOrder(SeckillUser user, GoodsVo goods) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());


        orderDao.insert(orderInfo);
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setGoodsId(goods.getId());
        seckillOrder.setOrderId(orderInfo.getId());
        seckillOrder.setUserId(user.getId());
        orderDao.insertSeckillOrder(seckillOrder);

        redisService.set(OrderKey.getSeckillOrderByUidGid, ""+user.getId()+"_"+goods.getId(), seckillOrder);

        return orderInfo;
    }
}
