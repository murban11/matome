package com.example.FuzzyLogic;

public abstract class GaussianMembership<T> implements Membership<T> {
    public abstract T getMean();
    public abstract T getStdDev();
}
