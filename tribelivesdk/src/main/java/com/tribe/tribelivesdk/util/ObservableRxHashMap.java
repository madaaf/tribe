package com.tribe.tribelivesdk.util;

import android.support.annotation.StringDef;

import java.util.HashMap;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Based onhttps://gist.github.com/meNESS/e3cd620cd71270a20816e015d78792cd
 */
public class ObservableRxHashMap<T, R> {

    @StringDef({ADD, REMOVE, UPDATE, CLEAR})
    public @interface ChangeType {}

    public static final String ADD = "add";
    public static final String REMOVE = "remove";
    public static final String UPDATE = "update";
    public static final String CLEAR = "clear";

    protected final HashMap<T, R> map;
    protected final PublishSubject<RxHashMap<R>> subject;

    public ObservableRxHashMap() {
        this.map = new HashMap<>();
        this.subject = PublishSubject.create();
    }

    public void put(T key, R value) {
        map.put(key, value);
        subject.onNext(new RxHashMap<>(ADD, value));
    }

    public void update(T key, R value) {
        map.put(key, value);
        subject.onNext(new RxHashMap<>(UPDATE, value));
    }

    public void clear() {
        map.clear();
        subject.onNext(new RxHashMap<>(CLEAR, null));
    }

    public void remove(T key) {
        R r = map.remove(key);
        subject.onNext(new RxHashMap<>(REMOVE, r));
    }

    public R get(T key) {
        return map.get(key);
    }

    public Observable<RxHashMap<R>> getObservable() {
        return subject;
    }

    public Observable<R> getCurrentMap() {
        return Observable.from(map.values());
    }

    public static class RxHashMap<R> {

        public @ChangeType String changeType;
        public R item;

        public RxHashMap(@ChangeType String changeType, R item) {
            this.changeType = changeType;
            this.item = item;
        }
    }
}