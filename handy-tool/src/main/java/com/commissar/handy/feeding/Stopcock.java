package com.commissar.handy.feeding;

/**
 * 旋塞阀
 * 投食器是否关闭控制
 *
 * @param <T> materiel type 物料类型
 * @author babu
 */
@FunctionalInterface
public interface Stopcock<T> {

    /**
     * 投食器是否关闭
     *
     * @param sequence             生产序号
     * @param intervalMilliseconds 生产间隔时间,单位:毫秒
     * @param materiel             物料
     * @return
     */
    boolean isStop(long sequence, long intervalMilliseconds, T materiel);

}
