package com.example.FuzzyLogic;

public interface Interval<T> {
    public boolean contains(T x);
    public T getStart();
    public T getEnd();
    public T size();
}
