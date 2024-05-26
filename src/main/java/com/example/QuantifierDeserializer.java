package com.example;

import java.io.IOException;

import com.example.FuzzyLogic.ContinuousGaussianMembership;
import com.example.FuzzyLogic.ContinuousTrapezoidalMembership;
import com.example.FuzzyLogic.DiscreteGaussianMembership;
import com.example.FuzzyLogic.DiscreteTrapezoidalMembership;
import com.example.FuzzyLogic.Membership;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class QuantifierDeserializer<T> extends StdDeserializer<Quantifier<T>> {

    private int absoluteQuantifierCardinality;

    public QuantifierDeserializer(int cardinality) {
        this(null, cardinality);
    }

    public QuantifierDeserializer(JavaType type, int cardinality) {
        super(type);
        this.absoluteQuantifierCardinality = cardinality;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Quantifier<T> deserialize(
        JsonParser parser,
        DeserializationContext ctxt
    ) throws IOException, JacksonException {
        String label = "";
        String quantifierType;
        String membershipType;

        Membership<T> membership = null;

        ObjectCodec codec = parser.getCodec();
        JsonNode node = codec.readTree(parser);

        label = node.get("label").asText();
        quantifierType = node.get("type").asText();

        membershipType = node.get("membership").asText();
        JsonNode params = node.get("params");
        if (membershipType.equals("trapezoidal")
                && quantifierType.equals("relative")
        ) {
            membership = (Membership<T>) new ContinuousTrapezoidalMembership(
                (float)params.get("a").asDouble(),
                (float)params.get("b").asDouble(),
                (float)params.get("c").asDouble(),
                (float)params.get("d").asDouble()
            );
        } else if (membershipType.equals("trapezoidal")
            && quantifierType.equals("absolute")
        ) {
                membership = (Membership<T>) new DiscreteTrapezoidalMembership(
                    params.get("a").asInt(),
                    params.get("b").asInt(),
                    params.get("c").asInt(),
                    params.get("d").asInt()
                );
        } else if (membershipType.equals("gaussian")
            && quantifierType.equals("relative")
        ) {
            membership = (Membership<T>) new ContinuousGaussianMembership(
                (float)params.get("mean").asDouble(),
                (float)params.get("stdDev").asDouble()
            );
        } else if (membershipType.equals("gaussian")
            && quantifierType.equals("absolute")
        ) {
            membership = (Membership<T>) new DiscreteGaussianMembership(
                params.get("mean").asInt(),
                params.get("stdDev").asInt()
            );
        } else {
            assert(false);
        }

        Quantifier<T> quantifier = null;
        try {
            if (quantifierType.equals("absolute")) {
                quantifier = (Quantifier<T>) new AbsoluteQuantifier(
                    label,
                    (Membership<Integer>)membership,
                    absoluteQuantifierCardinality
                );
            } else if (quantifierType.equals("relative")) {
                quantifier = (Quantifier<T>) new RelativeQuantifier(
                    label,
                    (Membership<Float>)membership
                );
            } else {
                assert(false);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return quantifier;
    }
}
