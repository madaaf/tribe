package com.tribe.tribelivesdk.util;

import android.support.annotation.StringDef;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Based onhttps://gist.github.com/meNESS/e3cd620cd71270a20816e015d78792cd
 */
public class ObservableRxHashMap<T, R> {

    @StringDef({INIT, ADD, ADD_ALL, REMOVE, UPDATE, CLEAR})
    public @interface ChangeType {}

    public static final String INIT = "init";
    public static final String ADD = "add";
    public static final String ADD_ALL = "add_all";
    public static final String REMOVE = "remove";
    public static final String UPDATE = "update";
    public static final String CLEAR = "clear";

    protected final HashMap<T, R> map;
    protected final PublishSubject<RxHashMap<T, R>> subject;
    protected final PublishSubject<Map<T, R>> mapSubject;

    public ObservableRxHashMap() {
        this.map = new HashMap<>();
        this.subject = PublishSubject.create();
        this.mapSubject = PublishSubject.create();
    }

    public void put(T key, R value) {
        map.put(key, value);
        subject.onNext(new RxHashMap<>(ADD, key, value));
        mapSubject.onNext(this.map);
    }

    public void putAll(Map<T, R> map) {
        this.map.putAll(map);
        subject.onNext(new RxHashMap<>(ADD_ALL, null, null));
        mapSubject.onNext(this.map);
    }

    public void update(T key, R value) {
        map.put(key, value);
        subject.onNext(new RxHashMap<>(UPDATE, key, value));
        mapSubject.onNext(map);
    }

    public void clear() {
        map.clear();
        subject.onNext(new RxHashMap<>(CLEAR, null, null));
        mapSubject.onNext(map);
    }

    public void remove(T key) {
        R r = map.remove(key);
        subject.onNext(new RxHashMap<>(REMOVE, key, r));
    }

    public R get(T key) {
        return map.get(key);
    }

    public Observable<RxHashMap<T, R>> getObservable() {
        return subject;
    }

    public Observable<Map<T, R>> getMapObservable() {
        return mapSubject;
    }

    public Observable<Map.Entry<T, R>> getCurrentMap() {
        return Observable.from(map.entrySet());
    }

    public static class RxHashMap<T, R> {

        public @ChangeType String changeType;
        public T key;
        public R item;

        public RxHashMap(@ChangeType String changeType, T key, R item) {
            this.changeType = changeType;
            this.key = key;
            this.item = item;
        }

        public RxHashMap() {
            this.changeType = INIT;
            this.key = null;
            this.item = null;
        }
    }
}