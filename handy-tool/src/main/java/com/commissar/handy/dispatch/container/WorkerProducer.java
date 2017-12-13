package com.commissar.handy.dispatch.container;

/**
 * Worker的生产者
 *
 * @param <T> 生产参数类型
 * @param <R> 生产结果类型
 * @author babu
 */
@FunctionalInterface
public interface WorkerProducer<T, R> {

    /**
     * 生产数据
     *
     * @param sequence 生产序列号,趋势递增/绝对递增,由实现决定
     * @param cursor   生产下标,范围[0,capacity)
     * @param value    生产数据
     * @return 待消费数据
     */
    R produce(long sequence, int cursor, T value);

}

