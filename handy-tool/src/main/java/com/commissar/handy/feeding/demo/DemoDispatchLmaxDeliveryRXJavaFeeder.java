package com.commissar.handy.feeding.demo;

import com.commissar.handy.dispatch.ContainerFeature;
import com.commissar.handy.dispatch.Dispatcher;
import com.commissar.handy.dispatch.demo.DemoContainerCurator;
import com.commissar.handy.feeding.Delivery;
import com.commissar.handy.feeding.Feeder;
import com.commissar.handy.feeding.bridge.dispatch.DispatchBridgeStabilityVoucher;
import com.commissar.handy.feeding.rxjava.RXJavaFeeder;

public class DemoDispatchLmaxDeliveryRXJavaFeeder extends RXJavaFeeder<String> {

    public DemoDispatchLmaxDeliveryRXJavaFeeder(String materiel, Delivery<String> delivery) {
        super(materiel, delivery);
    }

    public static void main(String[] args) throws InterruptedException {
        ContainerFeature shanghaiFeature = Dispatcher.containerFeature("shanghai");
        DemoContainerCurator demoContainerCurator = new DemoContainerCurator("shanghai");
        // 注册容器管理器
        Dispatcher.register(shanghaiFeature, demoContainerCurator);
        // 添加容器代理
        Dispatcher.addContainerProxy(demoContainerCurator.unique(), "001");
        Dispatcher.addContainerProxy(demoContainerCurator.unique(), "002");
        Dispatcher.addContainerProxy(demoContainerCurator.unique(), "003");

        DispatchBridgeStabilityVoucher stabilityVoucher = () -> "shanghai";

        Feeder feeder = new DemoDispatchLmaxDeliveryRXJavaFeeder("DemoDispatchLmaxDeliveryRXJavaFeeder"
                , new DemoDispatchLmaxDeliveryConsumerBridge(shanghaiFeature, stabilityVoucher));
        feeder.intervalMilliseconds(9000);
        feeder.stopcock((sequence, intervalMilliseconds, materiel) -> sequence > 1);
        feeder.start();

        for (int i = 0; i < 30; i++) {
            Thread.sleep(1000);
        }
    }

}
