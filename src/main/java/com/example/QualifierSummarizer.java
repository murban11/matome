package com.example;

import com.example.FuzzyLogic.ContinuousFuzzySet;
import com.example.FuzzyLogic.CrispSet;
import com.example.FuzzyLogic.Interval;
import com.example.FuzzyLogic.Membership;

public class QualifierSummarizer<S> implements LabeledFuzzySet<Float> {

    private String label;
    private FeatureExtractor<S> featureExtractor;
    private ContinuousFuzzySet fuzzySet;

    public QualifierSummarizer(
        String label,
		Membership<Float> membership,
		FeatureExtractor<S> featureExtractor
    ) {
        this.label = label;
        this.featureExtractor = featureExtractor;
        this.fuzzySet = new ContinuousFuzzySet(
            featureExtractor.getUniversum(),
            membership
        );
    }

    public float qualify(S subject) {
        return grade(featureExtractor.extract(subject));
    }

    public String getFeatureName() {
        return featureExtractor.getFeatureName();
    }

    @Override
    public float grade(Float x) {
        return fuzzySet.grade(x);
    }

    @Override
    public Float cardinality() {
        return fuzzySet.cardinality();
    }

    @Override
    public float height() {
        return fuzzySet.height();
    }

    @Override
    public CrispSet<Float, Float> support() throws Exception {
        return fuzzySet.support();
    }

    @Override
    public float degreeOfImprecision() {
        return fuzzySet.degreeOfImprecision();
    }

    @Override
    public Interval<Float> getUniversum() {
        return fuzzySet.getUniversum();
    }

    @Override
    public boolean contains(Float x) {
        return fuzzySet.contains(x);
    }

    @Override
    public String getLabel() {
        return label;
    }
}
