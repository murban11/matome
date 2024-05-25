package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SummaryGenerator<S> {
    private List<RelativeQuantifier> relativeQuantifiers = null;
    private List<AbsoluteQuantifier> absoluteQuantifiers = null;
    private List<QualifierSummarizer<S>> qualifierSummarizers;
    private float[] qualityWeights;
    private List<S> subjects;

    public SummaryGenerator(
        List<RelativeQuantifier> relativeQuantifiers,
        List<AbsoluteQuantifier> absoluteQuantifiers,
        List<QualifierSummarizer<S>> qualifierSummarizers,
        float[] qualityWeights,
        List<S> subjects
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

    public List<Summary<S>> generate() {
        List<Summary<S>> summaries = new ArrayList<>();

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

        SummaryQualityComparator<S> comparator
            = new SummaryQualityComparator<>(subjects, qualityWeights);
        Collections.sort(summaries, Collections.reverseOrder(comparator));

        return summaries;
    }

    private void generate(
        List<Summary<S>> summaries,
        Quantifier<?> quantifier
    ) {
        QualifierSummarizerSubsetGenerator<S> generator
            = new QualifierSummarizerSubsetGenerator<>(qualifierSummarizers);

        while (generator.hasNextSummarizers()) {
            List<QualifierSummarizer<S>> summarizers
                = generator.nextSummarizers();

            if (quantifier instanceof RelativeQuantifier) {
                summaries.add(
                    new Summary<S>((RelativeQuantifier)quantifier, summarizers)
                );
            } else if (quantifier instanceof AbsoluteQuantifier) {
                summaries.add(
                    new Summary<S>((AbsoluteQuantifier)quantifier, summarizers)
                );
            } else {
                assert(false);
            }

            while (generator.hasNextQualifiers()) {
                List<QualifierSummarizer<S>> qualifiers
                    = generator.nextQualifiers();

                if (qualifiers.size() > 0) {
                    summaries.add(
                        new Summary<S>(
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
