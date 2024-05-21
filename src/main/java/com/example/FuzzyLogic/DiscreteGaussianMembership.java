package com.example.FuzzyLogic;

public class DiscreteGaussianMembership extends GaussianMembership<Integer> {

    private Integer mean;
    private Integer stdDev;

    public DiscreteGaussianMembership(Integer mean, Integer stdDev) {
        this.mean = mean;
        this.stdDev = stdDev;
    }

    @Override
    public float grade(Integer x) {
        return (float)Math.exp(-0.5*Math.pow(((x - mean) / (float)stdDev), 2));
    }

    @Override
    public Integer getMean() {
        return mean;
    }

    @Override
    public Integer getStdDev() {
        return stdDev;
    }
}
