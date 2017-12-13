package com.commissar.handy.dispatch.container.lmax;

import com.commissar.handy.dispatch.container.WorkerConsumer;
import com.lmax.disruptor.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于LMAX实现的消费者
 *
 * @param <T> 消费数据类型
 * @author babu
 */
public abstract class LmaxWorkerConsumer<T> implements WorkerConsumer<T>, EventHandler<LmaxSlot<T>> {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onEvent(LmaxSlot<T> event, long sequence, boolean endOfBatch) throws Exception {
        if (event.getValue() != null && !consume(event.getValue())) {
            LOG.warn("consumer failure data:{}", event.getValue());
        }
    }

}
