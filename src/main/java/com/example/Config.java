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

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Config {

    @JsonProperty(value = "relativeQuantifiers")
    public List<RelativeQuantifier> relativeQuantifiers;

    @JsonProperty(value = "absoluteQuantifiers")
    public List<AbsoluteQuantifier> absoluteQuantifiers;

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

        module.addSerializer(
            QualifierSummarizer.class,
            new QualifierSummarizerSerializer()
        );
        module.addSerializer(
            RelativeQuantifier.class,
            new RelativeQuantifierSerializer()
        );
        module.addSerializer(
            AbsoluteQuantifier.class,
            new AbsoluteQuantifierSerializer()
        );

        module.addDeserializer(
            QualifierSummarizer.class,
            new QualifierSummarizerDeserializer()
        );
        module.addDeserializer(
            AbsoluteQuantifier.class,
            new AbsoluteQuantifierDeserializer(subjectCount)
        );
        module.addDeserializer(
            RelativeQuantifier.class,
            new RelativeQuantifierDeserializer()
        );

        mapper.registerModule(module);

        return mapper;
    }
}
