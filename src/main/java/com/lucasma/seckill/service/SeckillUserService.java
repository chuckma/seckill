package com.lucasma.seckill.service;

import com.lucasma.seckill.Util.MD5Util;
import com.lucasma.seckill.Util.UUIDUtil;
import com.lucasma.seckill.dao.SeckillUserDao;
import com.lucasma.seckill.domain.SeckillUser;
import com.lucasma.seckill.exception.GlobalException;
import com.lucasma.seckill.redis.RedisService;
import com.lucasma.seckill.redis.SeckillUserKey;
import com.lucasma.seckill.result.CodeMsg;
import com.lucasma.seckill.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Author: lucasma
 */
@Service
public class SeckillUserService {

    public static final String COOKI_NAME_TOKEN = "token";

    @Autowired
    SeckillUserDao seckillUserDao;

    @Autowired
    RedisService redisService;


    public SeckillUser getById(long id) {
        //取缓存
        SeckillUser user = redisService.get(SeckillUserKey.getById, ""+id, SeckillUser.class);
        if(user != null) {
            return user;
        }
        //取数据库
        user = seckillUserDao.getById(id);
        if(user != null) {
            redisService.set(SeckillUserKey.getById, ""+id, user);
        }
        return user;
    }

    public String login(HttpServletResponse response, LoginVo loginVo) {
        if(loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        //判断手机号是否存在
        SeckillUser user = getById(Long.parseLong(mobile));
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
        if(!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成cookie
        String token	 = UUIDUtil.uuid();
        // 添加Cookie到 redis
        addCookie(response, token, user);
        return token;
    }
    public SeckillUser getByToken(HttpServletResponse response, String token) {
        if(StringUtils.isEmpty(token)) {
            return null;
        }
        SeckillUser user = redisService.get(SeckillUserKey.token, token, SeckillUser.class);
        //延长有效期
        if(user != null) {
            addCookie(response, token, user);
        }
        return user;
    }

    private void addCookie(HttpServletResponse response, String token, SeckillUser user) {
        redisService.set(SeckillUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
        cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
