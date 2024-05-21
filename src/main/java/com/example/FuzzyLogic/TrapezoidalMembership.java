package com.example.FuzzyLogic;

public abstract class TrapezoidalMembership<T> implements Membership<T> {
    public abstract T getA();
    public abstract T getB();
    public abstract T getC();
    public abstract T getD();
}
