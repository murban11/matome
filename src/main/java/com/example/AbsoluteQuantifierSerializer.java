package com.example;

import java.io.IOException;

import com.example.FuzzyLogic.GaussianMembership;
import com.example.FuzzyLogic.Membership;
import com.example.FuzzyLogic.TrapezoidalMembership;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class AbsoluteQuantifierSerializer extends StdSerializer<AbsoluteQuantifier> {

    public AbsoluteQuantifierSerializer() {
        this(null);
    }

    public AbsoluteQuantifierSerializer(Class<AbsoluteQuantifier> c) {
        super(c);
    }

    @Override
    public void serialize(
        AbsoluteQuantifier quantifier,
        JsonGenerator generator,
        SerializerProvider serializer
    ) throws IOException {
        generator.writeStartObject();

        generator.writeStringField("label", quantifier.getLabel());

        Membership<Integer> membership = quantifier.getMembership();
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
