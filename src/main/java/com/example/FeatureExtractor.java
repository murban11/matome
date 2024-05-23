package com.example;

import com.example.FuzzyLogic.ContinuousInterval;

public interface FeatureExtractor<S> {
    float extract(S subject);
    String getFeatureName();
    ContinuousInterval getUniversum();
}
