package com.commissar.handy.feeding.demo;

import com.commissar.handy.feeding.Delivery;

import java.text.MessageFormat;

public class DemoDelivery implements Delivery<String> {

    @Override
    public void deliver(long sequence, long intervalMilliseconds, String materiel) {
        System.out.println(MessageFormat.format("{0}_deliver_sequence_{1}_intervalMilliseconds_{2}_materiel_{3}"
                , Thread.currentThread().getName(), sequence, intervalMilliseconds, materiel));
    }

}
