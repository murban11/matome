package com.example.FeatureExtractors;

import com.example.Feature;
import com.example.FeatureExtractor;
import com.example.Subject;
import com.example.FuzzyLogic.ContinuousInterval;

public class HeightFeatureExtractor implements FeatureExtractor<Subject> {

    @Override
    public float extract(Subject subject) {
        return subject.getHeight();
    }

    @Override
    public Feature getFeature() {
        return Feature.HEIGHT;
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
            universum =  new ContinuousInterval(120.0f, 200.0f);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return universum;
    }
}
