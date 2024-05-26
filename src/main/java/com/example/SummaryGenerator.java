package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SummaryGenerator {
    private List<RelativeQuantifier> relativeQuantifiers = null;
    private List<AbsoluteQuantifier> absoluteQuantifiers = null;
    private List<QualifierSummarizer> qualifierSummarizers;
    private float[] qualityWeights;
    private List<Subject> subjects;

    public SummaryGenerator(
        List<RelativeQuantifier> relativeQuantifiers,
        List<AbsoluteQuantifier> absoluteQuantifiers,
        List<QualifierSummarizer> qualifierSummarizers,
        float[] qualityWeights,
        List<Subject> subjects
    ) throws Exception {
        if (relativeQuantifiers != null) {
            this.relativeQuantifiers = new ArrayList<>(relativeQuantifiers);
        }
        if (absoluteQuantifiers != null) {
            this.absoluteQuantifiers = new ArrayList<>(absoluteQuantifiers);
        }
        this.qualifierSummarizers = new ArrayList<>(qualifierSummarizers);

        if (qualityWeights.length != Summary.QUALITIES_COUNT) {
            throw new Exception("Invalid number of quality weights");
        }
        this.qualityWeights = qualityWeights.clone();
        this.subjects = new ArrayList<>(subjects);
    }

    public List<Summary> generate() {
        List<Summary> summaries = new ArrayList<>();

        if (relativeQuantifiers != null) {
            for (var quantifier : relativeQuantifiers) {
                generate(summaries, quantifier);
            }
        }
        if (absoluteQuantifiers != null) {
            for (var quantifier : absoluteQuantifiers) {
                generate(summaries, quantifier);
            }
        }

        SummaryQualityComparator comparator
            = new SummaryQualityComparator(subjects, qualityWeights);
        Collections.sort(summaries, Collections.reverseOrder(comparator));

        return summaries;
    }

    private void generate(
        List<Summary> summaries,
        Quantifier<?> quantifier
    ) {
        QualifierSummarizerSubsetGenerator generator
            = new QualifierSummarizerSubsetGenerator(qualifierSummarizers);

        while (generator.hasNextSummarizers()) {
            List<QualifierSummarizer> summarizers
                = generator.nextSummarizers();

            if (quantifier instanceof RelativeQuantifier) {
                summaries.add(
                    new Summary((RelativeQuantifier)quantifier, summarizers)
                );
            } else if (quantifier instanceof AbsoluteQuantifier) {
                summaries.add(
                    new Summary((AbsoluteQuantifier)quantifier, summarizers)
                );
            } else {
                assert(false);
            }

            while (generator.hasNextQualifiers()) {
                List<QualifierSummarizer> qualifiers
                    = generator.nextQualifiers();

                if (qualifiers.size() > 0) {
                    summaries.add(
                        new Summary(
                            (RelativeQuantifier)quantifier,
                            qualifiers,
                            summarizers
                        )
                    );
                }
            }
        }
    }
}
