package com.example.FuzzyLogic;

public class ContinuousGaussianMembership extends GaussianMembership<Float> {

    private Float mean;
    private Float stdDev;

    public ContinuousGaussianMembership(Float mean, Float stdDev) {
        this.mean = mean;
        this.stdDev = stdDev;
    }

    @Override
    public float grade(Float x) {
        return (float)Math.exp(-0.5*Math.pow(((x - mean) / stdDev), 2));
    }

    @Override
    public Float getMean() {
        return mean;
    }

    @Override
    public Float getStdDev() {
        return stdDev;
    }
}
