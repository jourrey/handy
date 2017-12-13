package com.commissar.handy.dispatch.container;

/**
 * 执行器
 * 一个生产消费者模型
 * 每个执行器拥有固定大小的容量，Producer负责生产，Consumer负责消费
 *
 * @param <P> 生产者参数类型
 * @param <C> 消费者参数类型
 * @author babu
 */
public interface Worker<P, C> {

    /**
     * Worker的唯一标识
     *
     * @return 唯一标识
     */
    String unique();

    /**
     * 初始化并获取Worker
     *
     * @param capacity Worker的容量
     * @param producer 生产者
     * @param consumer 消费者
     * @return 初始化完成的Worker
     */
    Worker<P, C> init(int capacity, WorkerProducer<P, C> producer, WorkerConsumer<C> consumer);

    /**
     * 是否标准游标
     * 决定 {@link WorkerProducer#produce(long, int, Object)} 第二个参数的协同性,true保证所有Worker一致
     *
     * @return 是否标准游标
     */
    boolean isStandardCursor();

    /**
     * 处理数据
     * 会将数据委托给 {@link WorkerProducer#produce(long, int, Object)} 第三个参数
     *
     * @param value 处理的数据
     * @return 是否成功
     */
    boolean process(P value);

    /**
     * 是否可用
     * 不可用时,不保证Worker能提供服务,由具体实现决定
     *
     * @return 是否可用, 不可用请放弃使用该Worker
     */
    boolean isAvailable();

    /**
     * 销毁Worker
     * 销毁所有使用到的资源,例如:线程池\数据路链接
     *
     * @return 是否成功
     */
    boolean destroy();

}
