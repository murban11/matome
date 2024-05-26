package com.example;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionLikeType;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Config {

    @JsonProperty(value = "relativeQuantifiers")
    public List<Quantifier<Float>> relativeQuantifiers;

    @JsonProperty(value = "absoluteQuantifiers")
    public List<Quantifier<Integer>> absoluteQuantifiers;

    @JsonProperty(value = "qualifiers")
    public List<QualifierSummarizer> qualifierSummarizers;

    public void save(
        String filename
    ) throws StreamWriteException, DatabindException, IOException {
        ObjectMapper mapper = configureMapper(0);
        mapper.writeValue(new File(filename), this);
    }

    public static Config load(
        String filename,
        int subjectCount
    ) throws StreamReadException, DatabindException, IOException {
        ObjectMapper mapper = configureMapper(subjectCount);
        return mapper.readValue(new File(filename), Config.class);
    }

    private static ObjectMapper configureMapper(int subjectCount) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        CollectionLikeType relativeQuantifierType = mapper
            .getTypeFactory()
            .constructCollectionLikeType(Quantifier.class, Float.class);
        CollectionLikeType absoluteQuantifierType = mapper
            .getTypeFactory()
            .constructCollectionLikeType(Quantifier.class, Integer.class);

        module.addSerializer(
            QualifierSummarizer.class,
            new QualifierSummarizerSerializer()
        );
        module.addSerializer(
            new QuantifierSerializer<>(relativeQuantifierType)
        );
        module.addSerializer(
            new QuantifierSerializer<>(absoluteQuantifierType)
        );

        module.addDeserializer(
            QualifierSummarizer.class,
            new QualifierSummarizerDeserializer()
        );
        module.addDeserializer(
            Quantifier.class,
            new QuantifierDeserializer<Quantifier<Integer>>(subjectCount)
        );
        module.addDeserializer(
            Quantifier.class,
            new QuantifierDeserializer<Quantifier<Float>>(subjectCount)
        );

        mapper.registerModule(module);

        return mapper;
    }
}
