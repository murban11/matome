package com.example;

import java.io.IOException;

import com.example.FuzzyLogic.GaussianMembership;
import com.example.FuzzyLogic.Membership;
import com.example.FuzzyLogic.TrapezoidalMembership;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class QuantifierSerializer<T> extends StdSerializer<Quantifier<T>> {

    public QuantifierSerializer() {
        this(null);
    }

    public QuantifierSerializer(JavaType type) {
        super(type);
    }

    @Override
    public void serialize(
        Quantifier<T> quantifier,
        JsonGenerator generator,
        SerializerProvider serializer
    ) throws IOException {
        generator.writeStartObject();

        generator.writeStringField("label", quantifier.getLabel());

        if (quantifier instanceof RelativeQuantifier) {
            generator.writeStringField("type", "relative");
        } else if (quantifier instanceof AbsoluteQuantifier) {
            generator.writeStringField("type", "absolute");
        }

        Membership<T> membership = quantifier.getMembership();
        if (membership instanceof TrapezoidalMembership) {
            generator.writeStringField("membership", "trapezoidal");
        } else if (membership instanceof GaussianMembership) {
            generator.writeStringField("membership", "gaussian");
        } else {
            assert(false);
        }
        generator.writeFieldName("params");
        generator.writeObject(membership);

        generator.writeEndObject();
    }
}
