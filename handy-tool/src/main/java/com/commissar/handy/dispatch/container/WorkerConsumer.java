package com.commissar.handy.dispatch.container;

/**
 * Worker的消费者
 *
 * @param <T> 消费数据类型
 * @author babu
 */
@FunctionalInterface
public interface WorkerConsumer<T> {

    /**
     * 消费接收的数据
     *
     * @param value 接收的数据
     * @return 是否成功
     */
    boolean consume(T value);

}

