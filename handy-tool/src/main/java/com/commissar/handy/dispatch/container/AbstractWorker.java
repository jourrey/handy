package com.commissar.handy.dispatch.container;

import com.commissar.handy.dispatch.exception.OperationWorkerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Worker抽象模版类
 *
 * @param <P> 生产者参数类型
 * @param <C> 消费者参数类型
 * @author babu
 */
public abstract class AbstractWorker<P, C> implements Worker<P, C> {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    /**
     * 是否关闭
     */
    private boolean shutdown;
    /**
     * 执行器ID
     */
    private String unique;
    /**
     * 是否标准游标
     */
    private boolean standardCursor;
    /**
     * 执行器容量
     */
    private int capacity;
    /**
     * 执行器生产者
     */
    private WorkerProducer<P, C> producer;
    /**
     * 执行器消费者
     */
    private WorkerConsumer<C> consumer;

    public AbstractWorker(String unique) {
        this.shutdown = false;
        this.unique = unique;
    }

    @Override
    public String unique() {
        return this.unique;
    }

    @Override
    public boolean isStandardCursor() {
        return standardCursor;
    }

    @Override
    public boolean process(P value) {
        this.checkDestroy();
        if (this.getProducer() == null || this.getConsumer() == null) {
            throw new OperationWorkerException(
                    MessageFormat.format("WorkerProducer or WorkerConsumer can not be null. unique:{0}", this.unique()));
        }
        C data = this.getProducer().produce(System.currentTimeMillis(), 0, value);
        return this.getConsumer().consume(data);
    }

    @Override
    public boolean isAvailable() {
        return !this.shutdown;
    }

    @Override
    public boolean destroy() {
        LOG.debug("try destroy Worker unique:{}", this.unique);
        // 这里不做checkShutdown检查是因为,重复关闭并不会有什么影响,另外防止后续动作失败,需要重试
        this.shutdown = true;
        LOG.debug("destroy Worker unique:{} success", this.unique);
        return true;
    }

    /**
     * 检查是否已注销
     */
    protected void checkDestroy() {
        if (this.shutdown) {
            throw new OperationWorkerException(MessageFormat.format("The worker has been destroy. unique:{0}"
                    , this.unique));
        }
    }

    protected void setStandardCursor(boolean standardCursor) {
        this.standardCursor = standardCursor;
    }

    protected int getCapacity() {
        return capacity;
    }

    protected void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    protected WorkerProducer<P, C> getProducer() {
        return producer;
    }

    protected void setProducer(WorkerProducer<P, C> producer) {
        this.producer = producer;
    }

    protected WorkerConsumer<C> getConsumer() {
        return consumer;
    }

    protected void setConsumer(WorkerConsumer<C> consumer) {
        this.consumer = consumer;
    }

}
