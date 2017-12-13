package com.commissar.handy.feeding.demo;

import com.commissar.handy.dispatch.ContainerFeature;
import com.commissar.handy.feeding.bridge.dispatch.DispatchBridgeStabilityVoucher;
import com.commissar.handy.feeding.bridge.dispatch.DispatchDeliveryConsumerBridge;
import com.google.common.collect.Lists;

import java.text.MessageFormat;
import java.util.List;

public class DemoDispatchLmaxDeliveryConsumerBridge extends DispatchDeliveryConsumerBridge<String, Integer, String> {

    public DemoDispatchLmaxDeliveryConsumerBridge(ContainerFeature containerFeature
            , DispatchBridgeStabilityVoucher dispatchBridgeStabilityVoucher) {
        super(containerFeature, dispatchBridgeStabilityVoucher);
    }

    @Override
    public List<Integer> split(long sequence, long intervalMilliseconds, String materiel) {
        System.out.println(MessageFormat.format("{0}_split_sequence_{1}_intervalMilliseconds_{2}_materiel_{3}"
                , Thread.currentThread().getName(), sequence, intervalMilliseconds, materiel));
        return Lists.newArrayList(materiel.length());
    }

}
