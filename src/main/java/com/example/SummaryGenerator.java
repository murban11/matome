package com.example;

import java.util.ArrayList;
import java.util.List;

import com.example.MultiSubjectSummary.FORM;

public class SummaryGenerator {
    private static String SUBJECT_NAME = "people";

    private List<RelativeQuantifier> relativeQuantifiers = null;
    private List<AbsoluteQuantifier> absoluteQuantifiers = null;
    private List<QualifierSummarizer> qualifierSummarizers;
    private float[] qualityWeights;
    private List<Subject> subjects;

    private List<Subject> males;
    private List<Subject> females;

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

        this.males = new ArrayList<>();
        this.females = new ArrayList<>();
        for (var s : subjects) {
            if (s.getGender() == Subject.Gender.MALE) {
                this.males.add(s);
            } else {
                this.females.add(s);
            }
        }
    }

    public List<Pair<Float, String>> generate() throws Exception {
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

    private void generate(
        List<Pair<Float, String>> summaries,
        Quantifier<?> quantifier
    ) throws Exception {
        QualifierSummarizerSubsetGenerator generator
            = new QualifierSummarizerSubsetGenerator(qualifierSummarizers);

        while (generator.hasNextSummarizers()) {
            List<QualifierSummarizer> summarizers
                = generator.nextSummarizers();

            if (quantifier instanceof RelativeQuantifier) {
                Summary summary = new Summary(
                    (RelativeQuantifier)quantifier,
                    summarizers,
                    qualityWeights
                );
                summaries.add(new Pair<Float, String>(
                    summary.getQuality(subjects),
                    summary.toString(SUBJECT_NAME)
                ));
                MultiSubjectSummary msSummary = new MultiSubjectSummary(
                    (RelativeQuantifier)quantifier,
                    summarizers
                );
                summaries.add(new Pair<Float, String>(
                    msSummary.getQuality(males, females),
                    msSummary.toString("males", "females")
                ));
                summaries.add(new Pair<Float, String>(
                    msSummary.getQuality(females, males),
                    msSummary.toString("females", "males")
                ));
            } else if (quantifier instanceof AbsoluteQuantifier) {
                Summary summary = new Summary(
                    (AbsoluteQuantifier)quantifier,
                    summarizers,
                    qualityWeights
                );
                summaries.add(new Pair<Float, String>(
                    summary.getQuality(subjects),
                    summary.toString(SUBJECT_NAME)
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
                        summarizers,
                        qualityWeights
                    );
                    summaries.add(new Pair<Float, String>(
                        summary.getQuality(subjects),
                        summary.toString(SUBJECT_NAME)
                    ));
                    MultiSubjectSummary msSummary = new MultiSubjectSummary(
                        (RelativeQuantifier)quantifier,
                        qualifiers,
                        summarizers,
                        FORM.F2
                    );
                    summaries.add(new Pair<Float, String>(
                        msSummary.getQuality(males, females),
                        msSummary.toString("males", "females")
                    ));
                    summaries.add(new Pair<Float, String>(
                        msSummary.getQuality(females, males),
                        msSummary.toString("females", "males")
                    ));
                }
            }
        }
    }
}
