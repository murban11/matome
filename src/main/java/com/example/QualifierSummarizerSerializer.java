package com.example;

import java.io.IOException;

import com.example.FuzzyLogic.GaussianMembership;
import com.example.FuzzyLogic.Membership;
import com.example.FuzzyLogic.TrapezoidalMembership;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class QualifierSummarizerSerializer extends StdSerializer<QualifierSummarizer> {

    public QualifierSummarizerSerializer() {
        this(null);
    }

    public QualifierSummarizerSerializer(Class<QualifierSummarizer> c) {
        super(c);
    }

    @Override
    public void serialize(
        QualifierSummarizer qualifierSummarizer,
        JsonGenerator generator,
        SerializerProvider serializer
    ) throws IOException {
        generator.writeStartObject();

        generator.writeStringField("label", qualifierSummarizer.getLabel());
        generator.writeStringField(
            "feature",
            qualifierSummarizer.getFeature().name
        );

        Membership<Float> membership = qualifierSummarizer.getMembership();
        if (membership instanceof TrapezoidalMembership) {
            generator.writeStringField("membership", "trapezoidal");
            generator.writeFieldName("params");
            generator.writeObject(membership);
        } else if (membership instanceof GaussianMembership) {
            generator.writeStringField("membership", "gaussian");
            generator.writeFieldName("params");
            generator.writeObject(membership);
        } else {
            assert(false);
        }

        generator.writeEndObject();
    }
}
