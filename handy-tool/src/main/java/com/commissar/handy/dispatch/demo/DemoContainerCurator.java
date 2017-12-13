package com.commissar.handy.dispatch.demo;

import com.commissar.handy.dispatch.ContainerCurator;
import com.commissar.handy.dispatch.config.EngineConfig;
import com.commissar.handy.dispatch.container.IsomorphicEngine;
import com.commissar.handy.dispatch.container.lmax.LmaxWorker;
import com.commissar.handy.dispatch.container.lmax.LmaxWorkerConsumer;

import java.text.MessageFormat;

public class DemoContainerCurator extends ContainerCurator<Integer, String> {

    public DemoContainerCurator(String unique) {
        super(new IsomorphicEngine<>(unique));
    }

    @Override
    public boolean add(String workerUnique) {
        return engine != null && engine.add(new LmaxWorker<Integer, String>(workerUnique)
                .init(EngineConfig.ENGINE_WORKER_CAPACITY
                        , (sequence, cursor, value) -> {
                            System.out.println(MessageFormat.format("{0}_producer_{1}"
                                    , Thread.currentThread().getName(), value));
                            return "" + value;
                        }, new LmaxWorkerConsumer<String>() {
                            @Override
                            public boolean consume(String value) {
                                System.out.println(MessageFormat.format("{0}_consumer_{1}"
                                        , Thread.currentThread().getName(), value));
                                return true;
                            }
                        }));
    }

}
