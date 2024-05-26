package com.example.FeatureExtractors;

import com.example.Feature;
import com.example.FeatureExtractor;
import com.example.Subject;
import com.example.FuzzyLogic.ContinuousInterval;

public class DiastolicFeatureExtractor implements FeatureExtractor<Subject> {

    @Override
    public float extract(Subject subject) {
        return subject.getSystolic();
    }

    @Override
    public Feature getFeature() {
        return Feature.DIASTOLIC;
    }

    @Override
    public String getPostLabelStr() {
        return "(systolic)";
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
            universum =  new ContinuousInterval(0.0f, 210.0f);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return universum;
    }
}
