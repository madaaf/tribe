package com.tribe.app.presentation.utils;

public class Triplet<T, U, V> {

  public final T first;
  public final U second;
  public final V third;

  public Triplet(T first, U second, V third) {
    this.first = first;
    this.second = second;
    this.third = third;
  }

  public T getFirst() {
    return first;
  }

  public U getSecond() {
    return second;
  }

  public V getThird() {
    return third;
  }
}