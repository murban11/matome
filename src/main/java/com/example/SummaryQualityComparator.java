package com.example;

import java.util.Comparator;
import java.util.List;

public class SummaryQualityComparator
    implements Comparator<Pair<List<Float>, String>> {

    private float[] weights;

    public SummaryQualityComparator(float[] weights) {
        this.weights = weights;
    }

    @Override
    public int compare(
        Pair<List<Float>, String> s1,
        Pair<List<Float>, String> s2
    ) {
        float q1 = QualityAggregator.calculate(s1.first, weights);
        float q2 = QualityAggregator.calculate(s2.first, weights);

        return Float.compare(q1, q2);
    }
}
