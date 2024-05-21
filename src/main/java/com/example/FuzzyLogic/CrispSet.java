package com.example.FuzzyLogic;

public interface CrispSet<T, E> {
    public boolean contains(T x);
    public E cardinality();
}
