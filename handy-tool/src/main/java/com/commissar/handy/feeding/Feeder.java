package com.commissar.handy.feeding;

/**
 * 投食器
 *
 * @param <T> materiel type 物料类型
 * @author babu
 */
public interface Feeder<T> {

    /**
     * 设置停止旋塞阀
     *
     * @param stopcock
     */
    void stopcock(Stopcock<T> stopcock);

    /**
     * 设置每次调用间隔时间
     *
     * @param intervalMilliseconds
     */
    void intervalMilliseconds(long intervalMilliseconds);

    /**
     * 启动投食器
     */
    void start();

}
