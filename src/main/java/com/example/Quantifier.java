package com.example;

import com.example.FuzzyLogic.CrispSet;
import com.example.FuzzyLogic.FuzzySet;
import com.example.FuzzyLogic.Interval;
import com.example.FuzzyLogic.Membership;

public abstract class Quantifier<T> implements LabeledFuzzySet<T> {
    private FuzzySet<T> fuzzySet;

    public Quantifier(FuzzySet<T> fuzzySet) {
        this.fuzzySet = fuzzySet;
    }

    @Override
    public float grade(T x) {
        return fuzzySet.grade(x);
    }

    @Override
    public Float cardinality() {
        return fuzzySet.cardinality();
    }

    @Override
    public float height() {
        return fuzzySet.height();
    }

    @Override
    public CrispSet<T, T> support() throws Exception {
        return fuzzySet.support();
    }

    @Override
    public float degreeOfImprecision() {
        return fuzzySet.degreeOfImprecision();
    }

    @Override
    public Interval<T> getUniversum() {
        return fuzzySet.getUniversum();
    }

    @Override
    public boolean contains(T x) {
        return fuzzySet.contains(x);
    }

    @Override
    public Membership<T> getMembership() {
        return fuzzySet.getMembership();
    }
}
