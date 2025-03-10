package com.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private float min_t1;

    private List<Subject> males;
    private List<Subject> females;

    public SummaryGenerator(
        List<RelativeQuantifier> relativeQuantifiers,
        List<AbsoluteQuantifier> absoluteQuantifiers,
        List<QualifierSummarizer> qualifierSummarizers,
        float[] qualityWeights,
        List<Subject> subjects,
        String subjectName,
        float min_t1
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
        this.min_t1 = min_t1;

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

    public List<Pair<List<Float>, String>> generate(
        short type
    ) throws Exception {
        List<Pair<List<Float>, String>> summaries = new ArrayList<>();

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

        SummaryQualityComparator comparator
            = new SummaryQualityComparator(qualityWeights);
        Collections.sort(summaries, Collections.reverseOrder(comparator));

        return summaries;
    }

    private void generateSingleSubjectSummaries(
        List<Pair<List<Float>, String>> summaries,
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
                    if (summary.getQuality(0, subjects) >= min_t1) {
                        summaries.add(new Pair<List<Float>, String>(
                            summary.getQualities(subjects),
                            summary.toString(subjectName)
                        ));
                    }
                } else if (quantifier instanceof AbsoluteQuantifier) {
                    Summary summary = new Summary(
                        (AbsoluteQuantifier)quantifier,
                        summarizers,
                        qualityWeights
                    );
                    if (summary.getQuality(0, subjects) >= min_t1) {
                        summaries.add(new Pair<List<Float>, String>(
                            summary.getQualities(subjects),
                            summary.toString(subjectName)
                        ));
                    }
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
                        if (summary.getQuality(0, subjects) >= min_t1) {
                            summaries.add(new Pair<List<Float>, String>(
                                summary.getQualities(subjects),
                                summary.toString(subjectName)
                            ));
                        }
                    }
                }
            }
        }
    }

    private void generateMultiSubjectSummaries(
        List<Pair<List<Float>, String>> summaries,
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
                if (f4summary.getQuality(males, females) >= min_t1) {
                    summaries.add(new Pair<List<Float>, String>(
                        Arrays.asList(f4summary.getQuality(males, females)),
                        f4summary.toString("males", "females")
                    ));
                }
                if (f4summary.getQuality(females, males) >= min_t1) {
                    summaries.add(new Pair<List<Float>, String>(
                        Arrays.asList(f4summary.getQuality(females, males)),
                        f4summary.toString("females", "males")
                    ));
                }
            }

            for (var quantifier : quantifiers) {
                if ((type & SummaryType.MS1.id) > 0) {
                    MultiSubjectSummary f1summary = new MultiSubjectSummary(
                        quantifier,
                        summarizers
                    );
                    if (f1summary.getQuality(males, females) >= min_t1) {
                        summaries.add(new Pair<List<Float>, String>(
                            Arrays.asList(f1summary.getQuality(males, females)),
                            f1summary.toString("males", "females")
                        ));
                    }
                    if (f1summary.getQuality(females, males) >= min_t1) {
                        summaries.add(new Pair<List<Float>, String>(
                            Arrays.asList(f1summary.getQuality(females, males)),
                            f1summary.toString("females", "males")
                        ));
                    }
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
                        if (f2summary.getQuality(males, females) >= min_t1) {
                            summaries.add(new Pair<List<Float>, String>(
                                Arrays.asList(f2summary.getQuality(males, females)),
                                f2summary.toString("males", "females")
                            ));
                        }
                        if (f2summary.getQuality(females, males) >= min_t1) {
                            summaries.add(new Pair<List<Float>, String>(
                                Arrays.asList(f2summary.getQuality(females, males)),
                                f2summary.toString("females", "males")
                            ));
                        }
                    }
                    if ((type & SummaryType.MS3.id) > 0) {
                        MultiSubjectSummary f3summary = new MultiSubjectSummary(
                            quantifier,
                            qualifiers,
                            summarizers,
                            FORM.F3
                        );
                        if (f3summary.getQuality(males, females) >= min_t1) {
                            summaries.add(new Pair<List<Float>, String>(
                                Arrays.asList(f3summary.getQuality(males, females)),
                                f3summary.toString("males", "females")
                            ));
                        }
                        if (f3summary.getQuality(females, males) >= min_t1) {
                            summaries.add(new Pair<List<Float>, String>(
                                Arrays.asList(f3summary.getQuality(females, males)),
                                f3summary.toString("females", "males")
                            ));
                        }
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
