package com.lucasma.seckill.service;

import com.lucasma.seckill.dao.SeckillUserDao;
import com.lucasma.seckill.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

/**
 * Author: lucasma
 */
@Service
public class SeckillUserService {


    @Autowired
    SeckillUserDao seckillUserDao;

    public String login(HttpServletResponse response, LoginVo loginVo) {
        return null;
    }
}
