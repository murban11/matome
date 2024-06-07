package com.example;

import java.util.List;

public class QualityAggregator {

    public static float calculate(List<Float> qualities, float[] weights) {
        if (qualities.size() == 1) {
            return qualities.get(0);
        }

        float aggregatedQuality = 0.0f;
        for (int i = 0; i < qualities.size(); i++) {
            aggregatedQuality += qualities.get(i) * weights[i];
        }

        return aggregatedQuality;
    }
}
