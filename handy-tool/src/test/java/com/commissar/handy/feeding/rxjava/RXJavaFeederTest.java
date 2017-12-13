package com.commissar.handy.feeding.rxjava;

import com.commissar.handy.feeding.Feeder;
import com.commissar.handy.feeding.demo.DemoDeliveryRXJavaFeeder;
import org.junit.FixMethodOrder;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@FixMethodOrder()
public class RXJavaFeederTest {

    @Test
    public void testStart() throws InterruptedException {
        Integer[] count = new Integer[]{0};
        Feeder rxJavaFeeder = new DemoDeliveryRXJavaFeeder("junit"
                , (sequence, intervalMilliseconds, materiel) -> count[0] = count[0] + 1);
        rxJavaFeeder.stopcock((sequence, intervalMilliseconds, materiel) -> sequence > 1);
        rxJavaFeeder.intervalMilliseconds(100);
        rxJavaFeeder.start();

        Thread.sleep(1000);
        assertThat(count[0], is(2));
    }

}
