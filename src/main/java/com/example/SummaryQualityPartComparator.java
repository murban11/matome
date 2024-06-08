package com.example;

import java.util.Comparator;
import java.util.List;

public class SummaryQualityPartComparator
    implements Comparator<Pair<List<Float>, String>> {

    private int n = 1;

    public SummaryQualityPartComparator(int n) throws Exception {
        if (n < 1 || n > 11) {
            throw new Exception("Invalid quality index");
        }
        this.n = n;
    }

    @Override
    public int compare(
        Pair<List<Float>, String> s1,
        Pair<List<Float>, String> s2
    ) {
        float q1 = (s1.first.size() >= n) ? s1.first.get(n) : 0.0f;
        float q2 = (s2.first.size() >= n) ? s2.first.get(n) : 0.0f;

        return Float.compare(q1, q2);
    }
    
}
