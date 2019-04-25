package org.krybrig.concept.androidlifecycle;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class DataProducer {
    private static final int INTERVAL_PERIOD = 1;

    public Observable<Integer> retrieve(int num) {
        return Observable.interval(INTERVAL_PERIOD, TimeUnit.SECONDS)
                .take(num)
                .map(new Function<Long, Integer>() {
                    @Override
                    public Integer apply(Long interval) throws Exception {
                        return interval.intValue() + 1;
                    }
                });
    }
}
