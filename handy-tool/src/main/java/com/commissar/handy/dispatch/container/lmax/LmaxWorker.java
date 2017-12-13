package com.commissar.handy.dispatch.container.lmax;

import com.commissar.handy.dispatch.container.AbstractWorker;
import com.commissar.handy.dispatch.container.Worker;
import com.commissar.handy.dispatch.container.WorkerConsumer;
import com.commissar.handy.dispatch.container.WorkerProducer;
import com.commissar.handy.dispatch.convention.IdentifyStrategy;
import com.commissar.handy.dispatch.exception.InitWorkerException;
import com.commissar.handy.dispatch.exception.OperationWorkerException;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.text.MessageFormat;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 基于LMAX实现的Worker
 *
 * @param <P> 生产者参数类型
 * @param <C> 消费者参数类型
 * @author babu
 */
public class LmaxWorker<P, C> extends AbstractWorker<P, C> {

    private Disruptor<LmaxSlot<C>> disruptor;

    public LmaxWorker(String id) {
        super(id);
    }

    @Override
    public Worker<P, C> init(int capacity, WorkerProducer<P, C> producer, WorkerConsumer<C> consumer) {
        LOG.debug("LmaxWorker init unique:{} capacity:{} producer:{} consumer:{}"
                , this.unique(), capacity, producer, consumer);
        checkArgument(capacity > 0, "capacity must be positive number");
        checkNotNull(producer, "producer can not be null");
        checkNotNull(consumer, "consumer can not be null");
        // 初始化校验
        if (this.disruptor != null) {
            throw new InitWorkerException("LmaxWorker can only be initialized once");
        }
        if (!(consumer instanceof LmaxWorkerConsumer)) {
            throw new InitWorkerException("consumer not cast LmaxWorkerConsumer");
        }

        // 保存生产者和消费者
        this.setProducer(producer);
        this.setConsumer(consumer);
        // 初始化LMAX
        this.disruptor = new Disruptor<>(LmaxSlot::new, capacity, DaemonThreadFactory.INSTANCE
                , ProducerType.MULTI, new YieldingWaitStrategy());
        // 注册事件消费者,可传入多个EventHandler ...
        this.disruptor.handleEventsWith((LmaxWorkerConsumer) consumer);
        // 启动
        this.disruptor.start();

        return this;
    }

    @Override
    public boolean process(P value) {
        this.checkDestroy();
        if (this.getProducer() == null || this.getConsumer() == null) {
            throw new OperationWorkerException(
                    MessageFormat.format("WorkerProducer or WorkerConsumer can not be null. unique:{0}", this.unique()));
        }

        // 生产&发布数据,游标协同性的处理逻辑
        long sequence;
        int standardCursor;
        int producerCursor;
        while (true) {
            sequence = disruptor.getRingBuffer().next();
            producerCursor = IdentifyStrategy.computeWorkerCursor(sequence, this.getCapacity());
            // 非标准模式
            if (!this.isStandardCursor()) {
                produce(sequence, producerCursor, value);
                return true;
            }
            // 达到标准游标
            standardCursor = IdentifyStrategy.computeWorkerCursor(IdentifyStrategy.standardCursor(), this.getCapacity());
            if (producerCursor == standardCursor) {
                produce(sequence, standardCursor, value);
                return true;
            }
            // 如果不发布,将会阻塞,消费方需要判断
            produce(sequence, producerCursor, null);
        }
    }

    @Override
    public boolean destroy() {
        LOG.debug("LmaxWorker destroy unique:{}", this.unique());
        super.destroy();
        //关闭 disruptor 阻塞直至所有事件都得到处理
        this.disruptor.shutdown();
        return true;
    }

    /**
     * 生产数据
     *
     * @param sequence Lmax序列号,绝对递增
     * @param cursor   生产下标,范围[0,capacity)
     * @param value    生产者参数值
     */
    private void produce(long sequence, int cursor, P value) {
        // 生产出的待消费数据
        C data = null;
        try {
            // 调用生产者
            if (value != null) {
                data = this.getProducer().produce(sequence, cursor, value);
            }
        } catch (Throwable t) {
            LOG.error("LmaxWorker produce error", t);
        }
        try {
            // 给Event填充数据
            LmaxSlot<C> lmaxSlot = disruptor.getRingBuffer().get(sequence);
            lmaxSlot.setValue(data);
        } finally {
            // 发布Event, 激活观察者去消费, 将sequence传递给该消费者
            // publish应该放在 finally块中以确保一定会被调用->如果某个事件槽被获取但未提交, 将会堵塞后续的publish动作。
            disruptor.getRingBuffer().publish(sequence);
        }
    }

}
