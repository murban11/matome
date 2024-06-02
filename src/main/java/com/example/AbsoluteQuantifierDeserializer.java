package com.example;

import java.io.IOException;

import com.example.FuzzyLogic.DiscreteGaussianMembership;
import com.example.FuzzyLogic.DiscreteTrapezoidalMembership;
import com.example.FuzzyLogic.Membership;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class AbsoluteQuantifierDeserializer extends StdDeserializer<AbsoluteQuantifier> {

    private int absoluteQuantifierCardinality;

    public AbsoluteQuantifierDeserializer(int cardinality) {
        this(null, cardinality);
    }

    public AbsoluteQuantifierDeserializer(Class<AbsoluteQuantifier> c, int cardinality) {
        super(c);
        this.absoluteQuantifierCardinality = cardinality;
    }

    @Override
    public AbsoluteQuantifier deserialize(
        JsonParser parser,
        DeserializationContext ctxt
    ) throws IOException, JacksonException {
        String label = "";
        String membershipType;

        Membership<Integer> membership = null;

        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);

        label = node.get("label").asText();

        membershipType = node.get("membership").asText();
        JsonNode params = node.get("params");
        if (membershipType.equals("trapezoidal")) {
            membership = (Membership<Integer>) new DiscreteTrapezoidalMembership(
                params.get("a").asInt(),
                params.get("b").asInt(),
                params.get("c").asInt(),
                params.get("d").asInt()
            );
        } else if (membershipType.equals("gaussian")) {
            membership = (Membership<Integer>) new DiscreteGaussianMembership(
                params.get("mean").asInt(),
                params.get("stdDev").asInt()
            );
        } else {
            assert(false);
        }

        AbsoluteQuantifier quantifier = null;
        try {
            quantifier = new AbsoluteQuantifier(
                label,
                membership,
                absoluteQuantifierCardinality
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return quantifier;
    }
    
}
