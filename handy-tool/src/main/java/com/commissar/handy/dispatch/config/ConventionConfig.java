package com.commissar.handy.dispatch.config;

/**
 * 规约的配置
 *
 * @author babu
 */
public interface ConventionConfig {

    /**
     * 生成Worker序列号模版
     */
    String WORKER_SEQUENCE_TEMPLATE = "{0}_{1}";

    class DynamicConfig {

        /**
         * Worker序列号的偏移量
         * 自行覆盖实现动态,这里返回默认值
         */
        public static int workerSequenceOffset() {
            return 3;
        }

    }

}
