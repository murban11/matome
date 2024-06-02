package com.example;

import java.io.IOException;

import com.example.FuzzyLogic.GaussianMembership;
import com.example.FuzzyLogic.Membership;
import com.example.FuzzyLogic.TrapezoidalMembership;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class RelativeQuantifierSerializer extends StdSerializer<RelativeQuantifier> {

    public RelativeQuantifierSerializer() {
        this(null);
    }

    public RelativeQuantifierSerializer(Class<RelativeQuantifier> c) {
        super(c);
    }

    @Override
    public void serialize(
        RelativeQuantifier quantifier,
        JsonGenerator generator,
        SerializerProvider serializer
    ) throws IOException {
        generator.writeStartObject();

        generator.writeStringField("label", quantifier.getLabel());

        Membership<Float> membership = quantifier.getMembership();
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
