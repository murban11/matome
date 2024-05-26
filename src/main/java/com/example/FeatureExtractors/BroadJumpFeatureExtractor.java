package com.example.FeatureExtractors;

import com.example.Feature;
import com.example.FeatureExtractor;
import com.example.Subject;
import com.example.FuzzyLogic.ContinuousInterval;

public class BroadJumpFeatureExtractor implements FeatureExtractor<Subject> {

    @Override
    public float extract(Subject subject) {
        return subject.getBroadJump();
    }

    @Override
    public Feature getFeature() {
        return Feature.BROAD_JUMP;
    }

    @Override
    public String getPostLabelStr() {
        return "broad jump";
    }

    @Override
    public String getPreQualifierVerb() {
        return "having";
    }

    @Override
    public String getPreSummarizerVerb() {
        return "have";
    }

    @Override
    public ContinuousInterval getUniversum() {
        ContinuousInterval universum = null;
        try {
            universum =  new ContinuousInterval(0.0f, 310.0f);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return universum;
    }
}
