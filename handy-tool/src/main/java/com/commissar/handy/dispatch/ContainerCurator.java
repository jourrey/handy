package com.commissar.handy.dispatch;

import com.commissar.handy.dispatch.container.Engine;
import com.commissar.handy.dispatch.container.Worker;
import com.google.common.collect.ComparisonChain;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * 容器管理者
 *
 * @param <P> 生产者参数类型
 * @param <C> 消费者参数类型
 * @author babu
 */
public abstract class ContainerCurator<P, C> implements Comparable<ContainerCurator<P, C>> {

    /**
     * 绑定的引擎
     */
    protected Engine<P, C> engine;

    public ContainerCurator(Engine<P, C> engine) {
        this.engine = engine;
    }

    /**
     * 添加一个执行器
     *
     * @param workerUnique 执行器的唯一标识
     * @return 是否成功
     */
    public abstract boolean add(String workerUnique);

    /**
     * 容器管理者唯一标记
     *
     * @return 唯一标记 可能是null
     */
    public String unique() {
        return engine == null ? null : engine.unique();
    }

    /**
     * 删除一个执行器
     *
     * @param containerProxy 容器代理
     * @return 是否成功
     */
    public boolean del(ContainerProxy<P, C> containerProxy) {
        return engine != null && engine.del(containerProxy.getWorker());
    }

    /**
     * 分配一个执行器
     *
     * @return 容器代理, 可能是null
     */
    public ContainerProxy<P, C> alloc() {
        if (engine == null) {
            return null;
        }
        Worker<P, C> worker = engine.alloc();
        return worker == null ? null : new ContainerProxy<>(engine.unique(), worker);
    }

    /**
     * 释放一个执行器
     *
     * @param containerProxy 容器代理
     * @return 是否成功
     */
    public boolean free(ContainerProxy<P, C> containerProxy) {
        return engine != null && engine.free(containerProxy.getWorker());
    }

    /**
     * 关闭容器
     *
     * @return 是否成功
     */
    public boolean shutdown() {
        return engine != null && engine.shutdown();
    }

    /**
     * 获取可用资源数
     *
     * @return 可用资源数
     */
    public int remaining() {
        return engine == null ? 0 : engine.remaining();
    }

    @Override
    public int compareTo(ContainerCurator<P, C> o) {
        return ComparisonChain.start()
                .compare(this.remaining(), o.remaining())
                .result();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ContainerCurator<P, C> that = (ContainerCurator<P, C>) o;

        return new EqualsBuilder()
                .append(engine, that.engine)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(engine)
                .toHashCode();
    }

}
