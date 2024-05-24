package com.example.FeatureExtractors;

import com.example.FeatureExtractor;
import com.example.Subject;
import com.example.FuzzyLogic.ContinuousInterval;

public class SitAndBendForwardFeatureExtractor implements FeatureExtractor<Subject> {

    @Override
    public float extract(Subject subject) {
        return subject.getSitAndBendForward();
    }

    @Override
    public String getFeatureName() {
        return "sit and bend forward";
    }

    @Override
    public ContinuousInterval getUniversum() {
        ContinuousInterval universum = null;
        try {
            universum =  new ContinuousInterval(-20.0f, 220.0f);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return universum;
    }
}
