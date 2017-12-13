package com.commissar.handy.feeding.rxjava;

import com.commissar.handy.feeding.Delivery;
import com.commissar.handy.feeding.Feeder;
import com.commissar.handy.feeding.Stopcock;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import java.util.concurrent.TimeUnit;

/**
 * 投食器
 *
 * @param <T> 物料类型
 * @author babu
 */
public class RXJavaFeeder<T> implements Feeder<T> {

    private T materiel;
    private Delivery<T> delivery;
    private Stopcock<T> stopcock = (sequence, intervalMilliseconds, materiel) -> true;
    private long intervalMilliseconds = 1000;

    public RXJavaFeeder(T materiel, Delivery<T> delivery) {
        this.materiel = materiel;
        this.delivery = delivery;
    }

    @Override
    public void stopcock(Stopcock<T> stopcock) {
        this.stopcock = stopcock;
    }

    @Override
    public void intervalMilliseconds(long intervalMilliseconds) {
        this.intervalMilliseconds = intervalMilliseconds;
    }

    @Override
    public void start() {
        Observable.interval(this.intervalMilliseconds, TimeUnit.MILLISECONDS).subscribe(new Observer<Long>() {
            private Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(Long aLong) {
                if (stopcock.isStop(aLong, intervalMilliseconds, materiel)) {
                    disposable.dispose();
                } else {
                    delivery.deliver(aLong, intervalMilliseconds, materiel);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

}
