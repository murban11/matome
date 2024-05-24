package com.example;

import java.util.ArrayList;
import java.util.List;

public class SummaryGenerator<T, S> {
    private List<Quantifier<T>> quantifiers;
    private List<QualifierSummarizer<S>> qualifierSummarizers;
    private float[] qualityWeights;
    private List<S> subjects;

    public SummaryGenerator(
        List<Quantifier<T>> quantifiers,
        List<QualifierSummarizer<S>> qualifierSummarizers,
        float[] qualityWeights,
        List<S> subjects
    ) throws Exception {
        this.quantifiers = new ArrayList<>(quantifiers);
        this.qualifierSummarizers = new ArrayList<>(qualifierSummarizers);

        if (qualityWeights.length != Summary.QUALITIES_COUNT) {
            throw new Exception("Invalid number of quality weights");
        }
        this.qualityWeights = qualityWeights.clone();
        this.subjects = new ArrayList<>(subjects);
    }

    List<Summary<T, S>> generate() {
        List<Summary<T, S>> summaries = new ArrayList<>();

        for (var quantifier : quantifiers) {
            QualifierSummarizerSubsetGenerator<S> qsGen
                = new QualifierSummarizerSubsetGenerator<>(qualifierSummarizers);
            while (qsGen.hasNext()) {
                List<QualifierSummarizer<S>> qs = qsGen.nextSubset();
                if (quantifier instanceof RelativeQuantifier) {
                    summaries.add(new Summary<T, S>(quantifier, qs));
                }

                QualifierSummarizerSubsetGenerator<S> sGen
                    = new QualifierSummarizerSubsetGenerator<>(qs);
                while (sGen.hasNext()) {
                    Pair<List<QualifierSummarizer<S>>, List<QualifierSummarizer<S>>> qsPair = sGen.nextSubsetAndRemainder();
                    List<QualifierSummarizer<S>> summarizers = qsPair.first;
                    var qualifiers = qsPair.second;

                    if (qualifiers.size() > 0) {
                        summaries.add(new Summary<T, S>(quantifier, qualifiers, summarizers));
                    }
                }
            }
        }

        // TODO: Sort summaries based on their quality
        // TODO: Exclude summaries that have qualifiers and summarizers based
        // on the same feature

        return summaries;
    }
}
