package com.commissar.handy.dispatch.container;

/**
 * 引擎
 * 负责管理一组执行器
 *
 * @param <P> 生产者参数类型
 * @param <C> 消费者参数类型
 * @author babu
 */
public interface Engine<P, C> {

    /**
     * Engine的唯一标识
     *
     * @return 唯一标识
     */
    String unique();

    /**
     * 添加一个Worker
     *
     * @param worker 执行器
     * @return 是否成功
     */
    boolean add(Worker<P, C> worker);

    /**
     * 删除一个Worker
     *
     * @param worker 执行器
     * @return 是否成功
     */
    boolean del(Worker<P, C> worker);

    /**
     * 分配一个Worker
     *
     * @return 成功返回一个执行器, 失败则是null
     */
    Worker<P, C> alloc();

    /**
     * 释放一个Worker
     *
     * @param worker 执行器
     * @return 是否成功
     */
    boolean free(Worker<P, C> worker);

    /**
     * Engine的Worker数量
     *
     * @return Worker数量
     */
    int size();

    /**
     * Engine的空闲Worker数量
     *
     * @return 空闲Worker数量
     */
    int remaining();

    /**
     * 关闭Engine资源
     *
     * @return 是否成功
     */
    boolean shutdown();

}
