package com.example;

import java.util.Comparator;
import java.util.List;

public class SummaryQualityComparator<S> implements Comparator<Summary<S>> {

    private List<S> subjects;
    private float[] weights;

    public SummaryQualityComparator(List<S> subjects, float[] weights) {
        this.subjects = subjects;
        this.weights = weights;
    }

    @Override
    public int compare(Summary<S> s1, Summary<S> s2) {
        float q1 = 0.0f;
        float q2 = 0.0f;

        for (int i = 0; i < weights.length; ++i) {
            try {
                q1 += weights[i] * s1.getQuality(i, subjects);
                q2 += weights[i] * s2.getQuality(i, subjects);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return Float.compare(q1, q2);
    }
}
