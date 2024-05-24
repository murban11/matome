package com.example;

import java.util.ArrayList;
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

        for (var quantifier : relativeQuantifiers) {
            generate(summaries, quantifier);
        }

        // TODO: Sort summaries based on their quality
        // TODO: Exclude summaries that have qualifiers and summarizers based
        // on the same feature

        return summaries;
    }

    private void generate(
        List<Summary<S>> summaries,
        Quantifier<?> quantifier
    ) {
        QualifierSummarizerSubsetGenerator<S> qsGen
            = new QualifierSummarizerSubsetGenerator<>(qualifierSummarizers);
        while (qsGen.hasNext()) {
            List<QualifierSummarizer<S>> qs = qsGen.nextSubset();
            if (quantifier instanceof RelativeQuantifier) {
                summaries.add(
                    new Summary<S>((RelativeQuantifier)quantifier, qs)
                );
            } else if (quantifier instanceof AbsoluteQuantifier) {
                summaries.add(
                    new Summary<S>((AbsoluteQuantifier)quantifier, qs)
                );
            } else {
                assert(false);
            }

            QualifierSummarizerSubsetGenerator<S> sGen
                = new QualifierSummarizerSubsetGenerator<>(qs);
            while (sGen.hasNext()) {
                var qsPair = sGen.nextSubsetAndRemainder();
                List<QualifierSummarizer<S>> summarizers = qsPair.first;
                var qualifiers = qsPair.second;

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
