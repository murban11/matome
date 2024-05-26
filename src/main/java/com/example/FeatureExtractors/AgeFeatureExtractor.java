package com.example.FeatureExtractors;

import com.example.Feature;
import com.example.FeatureExtractor;
import com.example.Subject;
import com.example.FuzzyLogic.ContinuousInterval;

public class AgeFeatureExtractor implements FeatureExtractor<Subject> {

    @Override
    public float extract(Subject subject) {
        return subject.getAge();
    }

    @Override
    public Feature getFeature() {
        return Feature.AGE;
    }

    @Override
    public String getPostLabelStr() {
        return "";
    }

    @Override
    public String getPreQualifierVerb() {
        return "being";
    }

    @Override
    public String getPreSummarizerVerb() {
        return "are";
    }

    @Override
    public ContinuousInterval getUniversum() {
        ContinuousInterval universum = null;
        try {
            universum =  new ContinuousInterval(20.0f, 70.0f);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return universum;
    }
}
