package com.lucasma.seckill.redis;

/**
 * @author Administrator
 * 前缀接口
 */
public interface KeyPrefix {

    // 有效期
    public int expireSeconds();
    // 前缀
    public String getPrefix();
}
