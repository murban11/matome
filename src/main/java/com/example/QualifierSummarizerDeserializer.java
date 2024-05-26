package com.example;

import java.io.IOException;

import com.example.FeatureExtractors.AgeFeatureExtractor;
import com.example.FeatureExtractors.BmiFeatureExtractor;
import com.example.FeatureExtractors.BroadJumpFeatureExtractor;
import com.example.FeatureExtractors.DiastolicFeatureExtractor;
import com.example.FeatureExtractors.GripForceFeatureExtractor;
import com.example.FeatureExtractors.HeightFeatureExtractor;
import com.example.FeatureExtractors.ModifiedBodyFatFeatureExtractor;
import com.example.FeatureExtractors.SitAndBendForwardFeatureExtractor;
import com.example.FeatureExtractors.SitUpsCountFeatureExtractor;
import com.example.FeatureExtractors.SystolicFeatureExtractor;
import com.example.FuzzyLogic.ContinuousGaussianMembership;
import com.example.FuzzyLogic.ContinuousTrapezoidalMembership;
import com.example.FuzzyLogic.Membership;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class QualifierSummarizerDeserializer extends StdDeserializer<QualifierSummarizer> {

    public QualifierSummarizerDeserializer() {
        this(null);
    }

    public QualifierSummarizerDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public QualifierSummarizer deserialize(
        JsonParser parser,
        DeserializationContext ctxt
    ) {
        String label = "";
        String featureName;
        String membershipType;

        FeatureExtractor<Subject> featureExtractor = null;
        Membership<Float> membership = null;

        ObjectCodec codec = parser.getCodec();
        try {
            JsonNode node = codec.readTree(parser);

            label = node.get("label").asText();

            featureName = node.get("feature").asText();
            if (featureName.equals(Feature.AGE.name)) {
                featureExtractor = new AgeFeatureExtractor();
            } else if (featureName.equals(Feature.BMI.name)) {
                featureExtractor = new BmiFeatureExtractor();
            } else if (featureName.equals(Feature.BROAD_JUMP.name)) {
                featureExtractor = new BroadJumpFeatureExtractor();
            } else if (featureName.equals(Feature.DIASTOLIC.name)) {
                featureExtractor = new DiastolicFeatureExtractor();
            } else if (featureName.equals(Feature.GRIP_FORCE.name)) {
                featureExtractor = new GripForceFeatureExtractor();
            } else if (featureName.equals(Feature.HEIGHT.name)) {
                featureExtractor = new HeightFeatureExtractor();
            } else if (featureName.equals(Feature.MODIFIED_BODY_FAT.name)) {
                featureExtractor = new ModifiedBodyFatFeatureExtractor();
            } else if (featureName.equals(Feature.SIT_AND_BEND_FORWARD.name)) {
                featureExtractor = new SitAndBendForwardFeatureExtractor();
            } else if (featureName.equals(Feature.SIT_UPS_COUNT.name)) {
                featureExtractor = new SitUpsCountFeatureExtractor();
            } else if (featureName.equals(Feature.SYSTOLIC.name)) {
                featureExtractor = new SystolicFeatureExtractor();
            } else {
                assert(false);
            }

            membershipType = node.get("membership").asText();

            JsonNode params = node.get("params");
            if (membershipType.equals("trapezoidal")) {
                membership = new ContinuousTrapezoidalMembership(
                    (float)params.get("a").asDouble(),
                    (float)params.get("b").asDouble(),
                    (float)params.get("c").asDouble(),
                    (float)params.get("d").asDouble()
                );
            } else if (membershipType.equals("gaussian")) {
                membership = new ContinuousGaussianMembership(
                    (float)params.get("mean").asDouble(),
                    (float)params.get("stdDev").asDouble()
                );
            } else {
                assert(false);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return new QualifierSummarizer(label, membership, featureExtractor);
    }
}
