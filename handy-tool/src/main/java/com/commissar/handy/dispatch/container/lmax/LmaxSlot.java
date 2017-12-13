package com.commissar.handy.dispatch.container.lmax;

import com.commissar.handy.dispatch.container.Worker;

/**
 * 槽
 * 用于装载一个数据
 *
 * @param <T> 槽装载的数据类型
 * @author babu
 * @see Worker 的每一个槽
 */
public class LmaxSlot<T> {

    /**
     * 槽的值
     */
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

}
