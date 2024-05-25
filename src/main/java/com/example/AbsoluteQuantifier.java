package com.example;

import com.example.FuzzyLogic.DiscreteFuzzySet;
import com.example.FuzzyLogic.DiscreteInterval;
import com.example.FuzzyLogic.Membership;

public class AbsoluteQuantifier extends Quantifier<Integer> {

    private String label;

    public AbsoluteQuantifier(
        String label,
        Membership<Integer> membership,
        int cardinality
    ) throws Exception {
        super(new DiscreteFuzzySet(
            new DiscreteInterval(0, cardinality), membership
        ));
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
