package com.example;

import com.example.FuzzyLogic.ContinuousInterval;

public interface FeatureExtractor<S> {
    float extract(S subject);
    Feature getFeature();
    String getPostLabelStr();
    String getPreQualifierVerb();
    String getPreSummarizerVerb();
    ContinuousInterval getUniversum();
}
