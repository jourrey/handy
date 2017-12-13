package com.commissar.handy.dispatch.config;

/**
 * Worker的配置
 *
 * @author babu
 */
public interface EngineConfig {

    /**
     * Engine所属Worker的容量（确保一个Engine的所有Worker容量一致）
     */
    int ENGINE_WORKER_CAPACITY = 1024;

}
