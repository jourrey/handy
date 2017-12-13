package com.commissar.handy.dispatch.container;

import com.commissar.handy.dispatch.exception.OperationEngineException;
import com.commissar.handy.dispatch.exception.OperationWorkerException;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.LongAdder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 同构引擎
 * 引擎内每一个Worker都要求行为一致
 *
 * @param <P> 生产者参数类型
 * @param <C> 消费者参数类型
 * @author babu
 */
public class IsomorphicEngine<P, C> implements Engine<P, C> {
    private static final Logger LOG = LoggerFactory.getLogger(IsomorphicEngine.class);

    /**
     * 是否关闭
     */
    private boolean shutdown;
    /**
     * 引擎unique
     */
    private String unique;
    /**
     * 已经注册的Worker,保证唯一性和支持删除功能
     */
    private Map<String, String> existsWorkerIds;
    /**
     * 空闲的Worker资源
     */
    private Queue<Worker<P, C>> idleWorkers;
    /**
     * 已分配的Worker资源
     */
    private Map<String, Worker<P, C>> busyWorkers;
    /**
     * 引擎容量
     * 没有用existsWorkerIds.size()的原因是为了避免性能ConcurrentHashMap损耗
     */
    private LongAdder count;
    /**
     * 引擎余量
     */
    private LongAdder remaining;

    public IsomorphicEngine(String unique) {
        this.shutdown = false;
        this.unique = unique;
        this.existsWorkerIds = Maps.newConcurrentMap();
        this.idleWorkers = Queues.newConcurrentLinkedQueue();
        this.busyWorkers = Maps.newConcurrentMap();
        this.count = new LongAdder();
        this.remaining = new LongAdder();
    }

    @Override
    public String unique() {
        // 这里不做checkShutdown检查是因为,方便自定义wrapper类进行维护操作
        return this.unique;
    }

    @Override
    public boolean add(Worker<P, C> worker) {
        checkNotNull(worker, "worker can not be null");
        LOG.debug("try add Worker unique:{}", worker.unique());
        this.checkShutdown();
        if (!worker.isAvailable()) {
            throw new OperationWorkerException("Worker is not available");
        }
        if (this.count.longValue() >= Integer.MAX_VALUE) {
            throw new OperationWorkerException("Worker count exceed the Integer.MAX_VALUE");
        }

        // 检查workerID是否已存在
        String existsWorkerID = this.existsWorkerIds.putIfAbsent(worker.unique(), worker.unique());
        if (existsWorkerID != null) {
            throw new OperationWorkerException(MessageFormat.format("Worker unique {0} already exist", worker.unique()));
        }
        // 加入空闲Worker队列
        idleWorkers.add(worker);
        this.count.increment();
        this.remaining.increment();

        LOG.debug("add Worker unique:{} success", worker.unique());
        return true;
    }

    @Override
    public boolean del(Worker<P, C> worker) {
        checkNotNull(worker, "worker can not be null");
        LOG.debug("try del Worker unique:{}", worker.unique());
        this.checkShutdown();
        if (this.count.longValue() <= 0) {
            throw new OperationWorkerException("There is no Worker");
        }

        // Worker已存在,才可以删除
        this.existsWorkerIds.computeIfPresent(worker.unique(), (s, s1) -> {
            worker.destroy();
            this.busyWorkers.remove(worker.unique());
            this.count.decrement();
            this.remaining.decrement();
            LOG.debug("del Worker unique:{} success", worker.unique());
            return null;
        });

        return true;
    }

    @Override
    public Worker<P, C> alloc() {
        this.checkShutdown();

        // 获取一个空闲的Worker
        final Worker[] idleWorker = new Worker[]{idleWorkers.poll()};
        if (idleWorker[0] == null) {
            return null;
        }
        // 判断是否可用,不可用则重新申请
        if (!idleWorker[0].isAvailable()) {
            idleWorker[0] = alloc();
        }
        // 判断是否被Engine删除,删除则重新申请
        this.existsWorkerIds.computeIfAbsent(idleWorker[0].unique(), s -> {
            idleWorker[0] = alloc();
            return null;
        });
        // 重新申请已无Worker直接返回
        if (idleWorker[0] == null) {
            return null;
        }

        // 把Worker记录到忙碌的集合
        this.busyWorkers.computeIfAbsent(idleWorker[0].unique(), s -> {
            remaining.decrement();
            LOG.debug("alloc Worker unique:{} success", idleWorker[0].unique());
            return idleWorker[0];
        });

        return idleWorker[0];
    }

    @Override
    public boolean free(Worker<P, C> worker) {
        checkNotNull(worker, "worker can not be null");
        LOG.debug("try free Worker unique:{}", worker.unique());
        this.checkShutdown();

        // Worker已存在,才可以归还,从繁忙集合移除,并添加到空闲队列
        this.existsWorkerIds.computeIfPresent(worker.unique(), (s, s1) -> {
            this.busyWorkers.computeIfPresent(worker.unique(), (s2, tWorker) -> {
                idleWorkers.add(worker);
                remaining.increment();
                LOG.debug("free Worker unique:{} success", worker.unique());
                return null;
            });
            return worker.unique();
        });

        return true;
    }

    @Override
    public int size() {
        this.checkShutdown();
        return this.count.intValue();
    }

    @Override
    public int remaining() {
        this.checkShutdown();
        return this.remaining.intValue();
    }

    @Override
    public boolean shutdown() {
        LOG.debug("try shutdown Engine unique:{}", this.unique);
        // 这里不做checkShutdown检查是因为,重复关闭并不会有什么影响,另外防止后续动作失败,需要重试
        this.shutdown = true;
        this.existsWorkerIds.clear();
        this.idleWorkers.forEach(Worker::destroy);
        this.idleWorkers.clear();
        this.busyWorkers.forEach((key, value) -> value.destroy());
        this.busyWorkers.clear();
        this.count.reset();
        this.remaining.reset();
        LOG.debug("shutdown Engine unique:{} success", this.unique);
        return true;
    }

    /**
     * 检查引擎是否已关闭
     */
    private void checkShutdown() {
        if (this.shutdown) {
            throw new OperationEngineException(MessageFormat.format("The engine has been shutdown. unique:{0}"
                    , this.unique));
        }
    }

}
