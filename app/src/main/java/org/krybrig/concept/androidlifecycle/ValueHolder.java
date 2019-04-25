package org.krybrig.concept.androidlifecycle;

import io.reactivex.Observable;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.PublishSubject;

public class ValueHolder {
    private static ValueHolder instance;

    public static synchronized ValueHolder getInstance() {
        if (instance == null) {
            instance = new ValueHolder();
        }

        return instance;
    }

    private AsyncSubject<Integer> last;
    private PublishSubject<Integer> subject;

    public void setObservableSource(Observable<Integer> source) {
        this.last = AsyncSubject.create();
        this.subject = PublishSubject.create();

        source.subscribe(subject);
        subject.subscribe(last);
    }

    public Observable<Integer> getLiveValue() {
        if (last.hasComplete()) {
            return last;
        } else {
            return subject;
        }
    }
}
