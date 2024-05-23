package com.example;

import com.example.FuzzyLogic.ContinuousFuzzySet;
import com.example.FuzzyLogic.ContinuousInterval;
import com.example.FuzzyLogic.Membership;

public class RelativeQuantifier extends Quantifier<Float> {

    private String label;

    public RelativeQuantifier(
        String label,
        Membership<Float> membership
    ) throws Exception {
        super(new ContinuousFuzzySet(
            new ContinuousInterval(0.0f, 1.0f), membership
        ));
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
