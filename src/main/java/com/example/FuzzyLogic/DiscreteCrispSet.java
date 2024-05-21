package com.example.FuzzyLogic;

public class DiscreteCrispSet implements CrispSet<Integer, Integer> {

    // Only a single interval for now
    private DiscreteInterval interval;

    public DiscreteCrispSet(DiscreteInterval interval) {
        this.interval = interval;
    }

    @Override
    public boolean contains(Integer x) {
        return interval.contains(x);
    }

    @Override
    public Integer cardinality() {
        return interval.size();
    }
}
