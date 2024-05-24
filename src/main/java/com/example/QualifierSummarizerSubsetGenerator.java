package com.example;

import java.util.ArrayList;
import java.util.List;

public class QualifierSummarizerSubsetGenerator<S> {
    private int subsetCount;
    private int currSubset = 0;
    private List<QualifierSummarizer<S>> qs;

    public QualifierSummarizerSubsetGenerator(
        List<QualifierSummarizer<S>> qs
    ) {
        this.qs = qs;
        this.subsetCount = (1 << qs.size()) - 1;
    }

    public boolean hasNext() {
        return currSubset < subsetCount;
    }

    public List<QualifierSummarizer<S>> nextSubset() {
        currSubset += 1;
        List<QualifierSummarizer<S>> subset = new ArrayList<>();

        for (int i = 0; i < qs.size(); ++i) {
            if ((currSubset & (1 << i)) > 0) {
                subset.addLast(qs.get(i));
            }
        }

        return subset;
    }

    public Pair<List<QualifierSummarizer<S>>, List<QualifierSummarizer<S>>> nextSubsetAndRemainder() {
        currSubset += 1;
        List<QualifierSummarizer<S>> subset = new ArrayList<>();
        List<QualifierSummarizer<S>> remainder = new ArrayList<>();

        for (int i = 0; i < qs.size(); ++i) {
            if ((currSubset & (1 << i)) > 0) {
                subset.addLast(qs.get(i));
            } else {
                remainder.addLast(qs.get(i));
            }
        }

        return new Pair<>(subset, remainder);
    }
}
