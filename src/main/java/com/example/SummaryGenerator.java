package com.example;

import java.util.ArrayList;
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

    public List<Pair<Float, String>> generate() {
        List<Pair<Float, String>> summaries = new ArrayList<>();

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

        summaries.sort((p1, p2) -> p2.first.compareTo(p1.first));

        return summaries;
    }

    private float calcQuality(Summary summary) {
        float quality = 0.0f;

        for (int i = 0; i < qualityWeights.length; ++i) {
            try {
                quality += qualityWeights[i] * summary.getQuality(i, subjects);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return quality;
    }

    private void generate(
        List<Pair<Float, String>> summaries,
        Quantifier<?> quantifier
    ) {
        QualifierSummarizerSubsetGenerator generator
            = new QualifierSummarizerSubsetGenerator(qualifierSummarizers);

        while (generator.hasNextSummarizers()) {
            List<QualifierSummarizer> summarizers
                = generator.nextSummarizers();

            if (quantifier instanceof RelativeQuantifier) {
                Summary summary = new Summary(
                    (RelativeQuantifier)quantifier,
                    summarizers
                );
                summaries.add(new Pair<Float, String>(
                    calcQuality(summary),
                    summary.toString()
                ));
            } else if (quantifier instanceof AbsoluteQuantifier) {
                Summary summary = new Summary(
                    (AbsoluteQuantifier)quantifier,
                    summarizers
                );
                summaries.add(new Pair<Float, String>(
                    calcQuality(summary),
                    summary.toString()
                ));

                // Do not generate summaries of the second form for absolute
                // qualifiers.
                continue;
            } else {
                assert(false);
            }

            while (generator.hasNextQualifiers()) {
                List<QualifierSummarizer> qualifiers
                    = generator.nextQualifiers();

                if (qualifiers.size() > 0) {
                    Summary summary = new Summary(
                        (RelativeQuantifier)quantifier,
                        qualifiers,
                        summarizers
                    );
                    summaries.add(new Pair<Float, String>(
                        calcQuality(summary),
                        summary.toString()
                    ));
                }
            }
        }
    }
}
