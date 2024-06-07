package com.example;

import java.util.ArrayList;
import java.util.List;

import com.example.MultiSubjectSummary.FORM;

public class SummaryGenerator {
    public static enum SummaryType {
        SS1((short)0b0000000000000001),
        SS2((short)0b0000000000000010),
        MS1((short)0b0000000000000100),
        MS2((short)0b0000000000001000),
        MS3((short)0b0000000000010000),
        MS4((short)0b0000000000100000);

        public short id;

        SummaryType(short id) {
            this.id = id;
        }
    };

    private List<RelativeQuantifier> relativeQuantifiers = null;
    private List<AbsoluteQuantifier> absoluteQuantifiers = null;
    private List<QualifierSummarizer> qualifierSummarizers;
    private float[] qualityWeights;
    private List<Subject> subjects;
    private String subjectName;

    private List<Subject> males;
    private List<Subject> females;

    public SummaryGenerator(
        List<RelativeQuantifier> relativeQuantifiers,
        List<AbsoluteQuantifier> absoluteQuantifiers,
        List<QualifierSummarizer> qualifierSummarizers,
        float[] qualityWeights,
        List<Subject> subjects,
        String subjectName
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
        this.subjectName = subjectName;

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

    public List<Pair<Float, String>> generate(
        short type
    ) throws Exception {
        List<Pair<Float, String>> summaries = new ArrayList<>();

        if (relativeQuantifiers != null && includesSingleSubject(type)) {
            for (var quantifier : relativeQuantifiers) {
                generateSingleSubjectSummaries(summaries, quantifier, type);
            }
        }
        if (absoluteQuantifiers != null && (type & SummaryType.SS1.id) > 0) {
            for (var quantifier : absoluteQuantifiers) {
                generateSingleSubjectSummaries(summaries, quantifier, type);
            }
        }
        if (relativeQuantifiers != null && includesMultiSubject(type)) {
            generateMultiSubjectSummaries(summaries, relativeQuantifiers, type);
        }

        summaries.sort((p1, p2) -> p2.first.compareTo(p1.first));

        return summaries;
    }

    private void generateSingleSubjectSummaries(
        List<Pair<Float, String>> summaries,
        Quantifier<?> quantifier,
        short type
    ) throws Exception {
        QualifierSummarizerSubsetGenerator generator
            = new QualifierSummarizerSubsetGenerator(qualifierSummarizers);

        while (generator.hasNextSummarizers()) {
            List<QualifierSummarizer> summarizers
                = generator.nextSummarizers();

            if ((type & SummaryType.SS1.id) > 0) {
                if (quantifier instanceof RelativeQuantifier) {
                    Summary summary = new Summary(
                        (RelativeQuantifier)quantifier,
                        summarizers,
                        qualityWeights
                    );
                    summaries.add(new Pair<Float, String>(
                        summary.getQuality(subjects),
                        summary.toString(subjectName)
                    ));
                } else if (quantifier instanceof AbsoluteQuantifier) {
                    Summary summary = new Summary(
                        (AbsoluteQuantifier)quantifier,
                        summarizers,
                        qualityWeights
                    );
                    summaries.add(new Pair<Float, String>(
                        summary.getQuality(subjects),
                        summary.toString(subjectName)
                    ));
                } else {
                    assert(false);
                }
            }
            if ((type & SummaryType.SS2.id) > 0
                && quantifier instanceof RelativeQuantifier
            ) {
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
                            summary.toString(subjectName)
                        ));
                    }
                }
            }
        }
    }

    private void generateMultiSubjectSummaries(
        List<Pair<Float, String>> summaries,
        List<RelativeQuantifier> quantifiers,
        short type
    ) throws Exception {
        QualifierSummarizerSubsetGenerator generator
            = new QualifierSummarizerSubsetGenerator(qualifierSummarizers);

        while (generator.hasNextSummarizers()) {
            List<QualifierSummarizer> summarizers
                = generator.nextSummarizers();

            if ((type & SummaryType.MS4.id) > 0) {
                MultiSubjectSummary f4summary = new MultiSubjectSummary(
                    summarizers
                );
                summaries.add(new Pair<Float, String>(
                    f4summary.getQuality(males, females),
                    f4summary.toString("males", "females")
                ));
                summaries.add(new Pair<Float, String>(
                    f4summary.getQuality(females, males),
                    f4summary.toString("females", "males")
                ));
            }

            for (var quantifier : quantifiers) {
                if ((type & SummaryType.MS1.id) > 0) {
                    MultiSubjectSummary f1summary = new MultiSubjectSummary(
                        quantifier,
                        summarizers
                    );
                    summaries.add(new Pair<Float, String>(
                        f1summary.getQuality(males, females),
                        f1summary.toString("males", "females")
                    ));
                    summaries.add(new Pair<Float, String>(
                        f1summary.getQuality(females, males),
                        f1summary.toString("females", "males")
                    ));
                }
            }

            while (generator.hasNextQualifiers()) {
                List<QualifierSummarizer> qualifiers
                    = generator.nextQualifiers();

                for (var quantifier : quantifiers) {
                    if ((type & SummaryType.MS2.id) > 0) {
                        MultiSubjectSummary f2summary = new MultiSubjectSummary(
                            quantifier,
                            qualifiers,
                            summarizers,
                            FORM.F2
                        );
                        summaries.add(new Pair<Float, String>(
                            f2summary.getQuality(males, females),
                            f2summary.toString("males", "females")
                        ));
                        summaries.add(new Pair<Float, String>(
                            f2summary.getQuality(females, males),
                            f2summary.toString("females", "males")
                        ));
                    }
                    if ((type & SummaryType.MS3.id) > 0) {
                        MultiSubjectSummary f3summary = new MultiSubjectSummary(
                            quantifier,
                            qualifiers,
                            summarizers,
                            FORM.F3
                        );
                        summaries.add(new Pair<Float, String>(
                            f3summary.getQuality(males, females),
                            f3summary.toString("males", "females")
                        ));
                        summaries.add(new Pair<Float, String>(
                            f3summary.getQuality(females, males),
                            f3summary.toString("females", "males")
                        ));
                    }
                }
            }
        }
    }

    private boolean includesSingleSubject(short type) {
        return (type & SummaryType.SS1.id) > 0
            || (type & SummaryType.SS2.id) > 0;
    }

    private boolean includesMultiSubject(short type) {
        return (type & SummaryType.MS1.id) > 0
            || (type & SummaryType.MS2.id) > 0
            || (type & SummaryType.MS3.id) > 0
            || (type & SummaryType.MS4.id) > 0;
    }
}
