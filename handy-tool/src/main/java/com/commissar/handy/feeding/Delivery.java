package com.commissar.handy.feeding;

/**
 * 投放行为
 *
 * @param <T> 投放物料类型
 * @author babu
 */
@FunctionalInterface
public interface Delivery<T> {

    /**
     * 投放食物
     *
     * @param sequence             投放序号
     * @param intervalMilliseconds 间隔时间,单位:毫秒
     * @param materiel             物料
     */
    void deliver(long sequence, long intervalMilliseconds, T materiel);

}

