package com.commissar.handy.feeding.bridge;

import com.commissar.handy.feeding.Delivery;

import java.util.List;

/**
 * 数据桥接器
 *
 * @param <T> materiel type 物料类型
 * @param <R> product type 产品类型
 * @author babu
 */
public interface DeliveryConsumerBridge<T, R> extends Delivery<T> {

    default void deliver(long sequence, long intervalMilliseconds, T materiel) {
        List<R> segmentList = split(sequence, intervalMilliseconds, materiel);
        produce(segmentList);
    }

    /**
     * 切分物料
     *
     * @param sequence             投放序号
     * @param intervalMilliseconds 间隔时间,单位:毫秒
     * @param materiel             物料
     * @return 不要返回null, 请使用{@link java.util.Collections.EmptyList}
     */
    List<R> split(long sequence, long intervalMilliseconds, T materiel);

    /**
     * 生产产品
     *
     * @param segmentList 切分好的物料
     */
    void produce(List<R> segmentList);

}

