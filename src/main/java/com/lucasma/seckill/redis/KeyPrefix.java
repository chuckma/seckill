package com.lucasma.seckill.redis;

/**
 * @author Administrator
 */
public interface KeyPrefix {

    public int expireSeconds();

    public String getPrefix();
}
