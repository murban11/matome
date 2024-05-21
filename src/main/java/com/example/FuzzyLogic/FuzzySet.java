package com.example.FuzzyLogic;

public interface FuzzySet<T> extends CrispSet<T, Float> {
    float grade(T x);
    Float cardinality();
    float height();
    CrispSet<T, T> support() throws Exception;
    float degreeOfImprecision();
    Interval<T> getUniversum();
}
