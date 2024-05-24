package com.example.FeatureExtractors;

import com.example.FeatureExtractor;
import com.example.Subject;
import com.example.FuzzyLogic.ContinuousInterval;

public class SitUpsCountFeatureExtractor implements FeatureExtractor<Subject> {

    @Override
    public float extract(Subject subject) {
        return subject.getSitUpCount();
    }

    @Override
    public String getFeatureName() {
        return "sit-ups count";
    }

    @Override
    public ContinuousInterval getUniversum() {
        ContinuousInterval universum = null;
        try {
            universum =  new ContinuousInterval(0.0f, 80.0f);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return universum;
    }
}
