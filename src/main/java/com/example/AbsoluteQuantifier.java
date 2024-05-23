package com.example;

import com.example.FuzzyLogic.DiscreteFuzzySet;
import com.example.FuzzyLogic.DiscreteInterval;
import com.example.FuzzyLogic.Membership;

public class AbsoluteQuantifier extends Quantifier<Integer> {

    private String label;

    public AbsoluteQuantifier(
        String label,
        Membership<Integer> membership,
        DiscreteInterval universum
    ) throws Exception {
        super(new DiscreteFuzzySet(universum, membership));
        this.label = label;
        if (universum.getStart() != 0) {
            throw new Exception("Invalid universum of absolute quantifier: "
                + "Should start at 0");
        }
    }

    @Override
    public String getLabel() {
        return label;
    }
}
