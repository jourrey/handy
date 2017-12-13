package com.commissar.handy.dispatch;

import com.commissar.handy.dispatch.container.Worker;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * 容器代理
 *
 * @param <P> 生产者参数类型
 * @param <C> 消费者参数类型
 * @author babu
 */
public class ContainerProxy<P, C> {

    private String engineUnique;
    private Worker<P, C> worker;

    public ContainerProxy(String engineUnique, Worker<P, C> worker) {
        this.engineUnique = engineUnique;
        this.worker = worker;
    }

    /**
     * 是否可用
     * 不可用,请重新申请
     *
     * @return 是否可用
     */
    public boolean isAvailable() {
        return this.worker != null && this.worker.isAvailable();
    }

    /**
     * 处理数据
     *
     * @param value 处理的数据
     * @return 是否成功
     */
    public boolean process(P value) {
        return isAvailable() && this.worker.process(value);
    }

    /**
     * 容器代理的唯一标识
     *
     * @return 唯一标识
     */
    public String unique() {
        return this.worker == null ? null : this.worker.unique();
    }

    /**
     * 容器管理者的唯一标识
     *
     * @return 唯一标识
     */
    public String containerCuratorUnique() {
        return this.engineUnique;
    }

    /**
     * 获取执行器对象
     * 权限是本包内实现,请不要更改
     * 为了更安全的维护,请不要获取执行器对象,只用于 {@link ContainerCurator#del(ContainerProxy)} 方法
     *
     * @return 执行器
     */
    Worker<P, C> getWorker() {
        return worker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ContainerProxy<?, ?> that = (ContainerProxy<?, ?>) o;

        return new EqualsBuilder()
                .append(engineUnique, that.engineUnique)
                .append(worker, that.worker)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(engineUnique)
                .append(worker)
                .toHashCode();
    }

}
