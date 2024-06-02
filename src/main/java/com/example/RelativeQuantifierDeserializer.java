package com.example;

import java.io.IOException;

import com.example.FuzzyLogic.ContinuousGaussianMembership;
import com.example.FuzzyLogic.ContinuousTrapezoidalMembership;
import com.example.FuzzyLogic.Membership;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class RelativeQuantifierDeserializer extends StdDeserializer<RelativeQuantifier> {

    public RelativeQuantifierDeserializer() {
        this(null);
    }

    public RelativeQuantifierDeserializer(Class<RelativeQuantifier> c) {
        super(c);
    }

    @Override
    public RelativeQuantifier deserialize(
        JsonParser parser,
        DeserializationContext ctxt
    ) throws IOException, JacksonException {
        String label = "";
        String membershipType;

        Membership<Float> membership = null;

        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);

        label = node.get("label").asText();

        membershipType = node.get("membership").asText();
        JsonNode params = node.get("params");
        if (membershipType.equals("trapezoidal")) {
            membership = (Membership<Float>) new ContinuousTrapezoidalMembership(
                (float)params.get("a").asDouble(),
                (float)params.get("b").asDouble(),
                (float)params.get("c").asDouble(),
                (float)params.get("d").asDouble()
            );
        } else if (membershipType.equals("gaussian")) {
            membership = (Membership<Float>) new ContinuousGaussianMembership(
                (float)params.get("mean").asDouble(),
                (float)params.get("stdDev").asDouble()
            );
        } else {
            assert(false);
        }

        RelativeQuantifier quantifier = null;
        try {
            quantifier = new RelativeQuantifier(
                label,
                membership
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return quantifier;
    }
    
}
