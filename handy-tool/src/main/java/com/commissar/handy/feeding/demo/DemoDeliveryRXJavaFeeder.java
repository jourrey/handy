package com.commissar.handy.feeding.demo;

import com.commissar.handy.feeding.Delivery;
import com.commissar.handy.feeding.Feeder;
import com.commissar.handy.feeding.rxjava.RXJavaFeeder;

public class DemoDeliveryRXJavaFeeder extends RXJavaFeeder<String> {

    public DemoDeliveryRXJavaFeeder(String materiel, Delivery<String> delivery) {
        super(materiel, delivery);
    }

    public static void main(String[] args) throws InterruptedException {
        Feeder<String> feeder = new DemoDeliveryRXJavaFeeder("DemoDeliveryRXJavaFeeder", new DemoDelivery());
        feeder.start();

        for (int i = 0; i < 30; i++) {
            Thread.sleep(1000);
        }
    }

}
