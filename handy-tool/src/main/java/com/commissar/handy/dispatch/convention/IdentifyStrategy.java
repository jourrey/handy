package com.commissar.handy.dispatch.convention;

import com.commissar.handy.dispatch.config.ConventionConfig;

import java.text.MessageFormat;
import java.util.UUID;

/**
 * 标识符策略
 *
 * @author babu
 */
public class IdentifyStrategy {

    /**
     * 生成EngineID
     *
     * @return 唯一标识
     */
    public static String generateEngineUnique() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成WorkerID
     *
     * @return 唯一标识
     */
    public static String generateWorkerUnique() {
        return UUID.randomUUID().toString();
    }

    /**
     * 获取标准游标
     * 必须保证线程间一致性,保证所有Worker同步
     *
     * @return 游标值
     */
    public static long standardCursor() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 计算Worker游标
     *
     * @param standardCursor 标准游标
     * @param workerCapacity Worker的容量
     * @return Worker的游标
     */
    public static int computeWorkerCursor(long standardCursor, int workerCapacity) {
        // 计算Worker游标,公式:标准游标%Worker容量
        return workerCapacity < 1 ? 0 : (int) (standardCursor % workerCapacity);
    }

    /**
     * 生成Worker的序列号
     *
     * @param workerUnique Worker的唯一标识
     * @param workerCursor Worker的游标
     * @return Worker的序列号
     */
    public static String generateWorkerSequence(String workerUnique, int workerCursor) {
        return MessageFormat.format(ConventionConfig.WORKER_SEQUENCE_TEMPLATE, workerUnique, workerCursor);
    }

}
