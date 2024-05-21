package com.example.FuzzyLogic;

public class ContinuousCrispSet implements CrispSet<Float, Float> {

    // Only a single interval for now
    private ContinuousInterval interval;

    public ContinuousCrispSet(ContinuousInterval interval) {
        this.interval = interval;
    }

    @Override
    public boolean contains(Float x) {
        return interval.contains(x);
    }

    @Override
    public Float cardinality() {
        return interval.size();
    }
}
