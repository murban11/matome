package com.example;

import com.example.FuzzyLogic.ContinuousInterval;

public class DiastolicFeatureExtractor implements FeatureExtractor<Subject> {

    @Override
    public float extract(Subject subject) {
        return subject.getSystolic();
    }

    @Override
    public String getFeatureName() {
        return "systolic";
    }

    @Override
    public ContinuousInterval getUniversum() {
        ContinuousInterval universum = null;
        try {
            universum =  new ContinuousInterval(0.0f, 210.0f);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return universum;
    }
}
