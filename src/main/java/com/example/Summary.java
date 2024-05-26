package com.example;

import java.util.ArrayList;
import java.util.List;

public class Summary {
    public static final int QUALITIES_COUNT = 10;

    private RelativeQuantifier relativeQuantifier = null;
    private AbsoluteQuantifier absoluteQuantifier = null;
    private List<QualifierSummarizer> qualifiers = null;
    private List<QualifierSummarizer> summarizers;
    private float[] qualities = new float[QUALITIES_COUNT];
    private boolean qualitiesCalculated = false;

    public Summary(
        RelativeQuantifier quantifier,
        List<QualifierSummarizer> summarizers
    ) {
        this.relativeQuantifier = quantifier;
        this.summarizers = new ArrayList<>(summarizers);
    }

    public Summary(
        AbsoluteQuantifier quantifier,
        List<QualifierSummarizer> summarizers
    ) {
        this.absoluteQuantifier = quantifier;
        this.summarizers = new ArrayList<>(summarizers);
    }

    public Summary(
        RelativeQuantifier quantifier,
        List<QualifierSummarizer> qualifiers,
        List<QualifierSummarizer> summarizers
    ) {
        this(quantifier, summarizers);
        this.qualifiers = new ArrayList<>(qualifiers);
    }

    public float getQuality(int n, List<Subject> subjects) throws Exception {
        if (n < 0 || n >= QUALITIES_COUNT) {
            throw new Exception("Invalid index of summary quality");
        }

        if (!qualitiesCalculated) {
            int N = subjects.size();

            float qualifier_sigma_count = 0.0f;
            float qualifier_support_count = 0.0f;
            float[] qualifier_cardinalities;
            if (qualifiers != null) {
                qualifier_cardinalities = new float[qualifiers.size()];
            } else {
                qualifier_cardinalities = new float[1];
            }

            float summarizer_sigma_count = 0.0f;
            float summarizer_and_qualifier_sigma_count = 0.0f;
            float[] summarizer_cardinalities = new float[summarizers.size()];
            int[] summarizer_support_counts = new int[summarizers.size()];

            int quantifier_support_count = 0;
            float quantifier_cardinality = 0.0f;

            int t = 0;
            int h = 0;

            int iter = 0;

            for (Subject subject : subjects) {
                float summarizer_grade = 1.0f;
                float summarizer_qualifier_grade = 1.0f;

                for (int i = 0; i < summarizers.size(); ++i) {
                    float grade = summarizers.get(i).qualify(subject);
                    summarizer_cardinalities[i] += grade;

                    summarizer_grade = (float)Math.min(
                        summarizer_grade,
                        grade
                    );

                    if (grade > 0.0f) {
                        summarizer_support_counts[i] += 1;
                    }

                    if (qualifiers != null) {
                        summarizer_qualifier_grade = (float)Math.min(
                            summarizer_qualifier_grade,
                            summarizer_grade
                        );
                    }
                }
                summarizer_sigma_count += summarizer_grade;

                float qualifier_grade = 1.0f;
                if (qualifiers != null) {
                    for (int i = 0; i < qualifiers.size(); ++i) {
                        float grade = qualifiers.get(i).qualify(subject);
                        qualifier_cardinalities[i] += grade;

                        qualifier_grade = (float)Math.min(
                            qualifier_grade,
                            grade
                        );

                        summarizer_qualifier_grade = (float)Math.min(
                            summarizer_qualifier_grade,
                            qualifier_grade
                        );
                    }
                    qualifier_sigma_count += qualifier_grade;

                    if (qualifier_grade > 0.0) {
                        qualifier_support_count += 1;
                    }
                }
                summarizer_and_qualifier_sigma_count
                    += summarizer_qualifier_grade;

                if ((qualifiers != null
                        && summarizer_grade > 0.0
                        && qualifier_grade > 0.0)
                    || (qualifiers == null && summarizer_grade > 0.0)
                ) {
                    t += 1;
                }

                if ((qualifiers != null && summarizer_grade > 0.0)
                    || qualifiers == null
                ) {
                    h += 1;
                }

                if (qualifiers != null) {
                    qualities[0] = relativeQuantifier
                        .grade(summarizer_and_qualifier_sigma_count
                            / qualifier_sigma_count);
                } else if (relativeQuantifier != null) {
                    qualities[0] = relativeQuantifier
                        .grade(summarizer_sigma_count / (float)N);
                } else if (absoluteQuantifier != null) {
                    qualities[0] = absoluteQuantifier
                        .grade(Math.round(summarizer_sigma_count));
                } else {
                    assert(false);
                }

                float quantifier_grade = 0.0f;
                if (relativeQuantifier != null) {
                    quantifier_grade = relativeQuantifier.grade(
                        iter / (float)N
                    );
                } else if (absoluteQuantifier != null) {
                    quantifier_grade = absoluteQuantifier.grade(iter);
                } else {
                    assert(false);
                }

                if (quantifier_grade > 0.0) {
                    quantifier_support_count += 1;
                }
                quantifier_cardinality += quantifier_grade;

                iter += 1;
            }

            // T2
            assert(summarizer_support_counts.length > 0);
            float in_sj_prod
                = summarizer_support_counts[0] / (float)N;
            float sj_card_prod
                = summarizer_cardinalities[0] / (float)N;
            for (int i = 1; i < summarizers.size(); ++i) {
                in_sj_prod
                    *= summarizer_support_counts[i] / (float)N;
                sj_card_prod
                    *= summarizer_cardinalities[0] / (float)N;
            }
            qualities[1]
                = (float)Math.pow(in_sj_prod, 1 / (float)summarizers.size());
            qualities[1] = 1 - qualities[1];

            qualities[2] = t / (float)h;
            qualities[3] = Math.abs(in_sj_prod - qualities[2]);
            qualities[4] = 2*(float)Math.pow(
                0.5f, summarizers.size()
                    + ((qualifiers != null) ? qualifiers.size() : 0)
            );

            // T6
            if (relativeQuantifier != null) {
                qualities[5]
                    = 1 - quantifier_support_count / (float)N;
            } else if (absoluteQuantifier != null) {
                qualities[5]
                    = quantifier_support_count / (float)N;
            } else {
                assert(false);
            }

            qualities[6] = 1 - quantifier_cardinality / (float)N;
            qualities[7] = 1 - (float)Math.pow(
                sj_card_prod, 1 / (float)summarizers.size()
            );
            qualities[8] = 1 - qualifier_support_count / (float)N;

            // T10
            float wj_card_prod = qualifier_cardinalities[0] / (float)N;
            for (int i = 1; i < qualifier_cardinalities.length; ++i) {
                wj_card_prod *= qualifier_cardinalities[i] / (float)N;
            }
            qualities[9] = 1 - (float)Math.pow(
                wj_card_prod, 1 / (float)qualifier_cardinalities.length);

            qualitiesCalculated = true;
        }

        return qualities[n];
    }

    public String toString(String subjectsName) {
        StringBuilder sb = new StringBuilder(
            ((relativeQuantifier != null)
                ? relativeQuantifier.getLabel()
                    : absoluteQuantifier.getLabel())
            + " " + subjectsName
        );
        // TODO: Refactor the code below to eliminate duplication.
        if (qualifiers != null) {
            // TODO: Group qualifiers based on their `preQualifierVerb` and
            // print all the qualifiers from the first group before printing
            // qualifiers from the second group, so that we do not have
            // sequences like this: ... are ... and being ... and are ...
            assert(qualifiers.size() > 0);
            String preQualifierVerb
                = qualifiers.getFirst().getPreQualifierVerb();
            sb.append(" " + preQualifierVerb + " ");
            for (int i = 0; i < qualifiers.size(); ++i) {
                if (i == 1 && qualifiers.size() == 2) {
                    sb.append(" ");
                }
                else if (i > 0) {
                    sb.append(", ");
                }
                if (i > 0 && i == qualifiers.size() - 1) {
                    sb.append("and ");
                }

                if (i > 0 && qualifiers
                        .get(i)
                        .getPreQualifierVerb()
                        .equals(preQualifierVerb)
                ) {
                    preQualifierVerb = qualifiers.get(i).getPreQualifierVerb();
                    sb.append(preQualifierVerb + " ");
                }

                sb.append(qualifiers.get(i).getLabel());
                if (!qualifiers.get(i).getPostLabelStr().isEmpty()) {
                    sb.append(" " + qualifiers.get(i).getPostLabelStr());
                }
            }
        }
        // TODO: Group summarizers based on their `preSummarizerVerb`,
        // analogiously to the qualifiers.
        assert(summarizers.size() > 0);
        String preSummarizerVerb
            = summarizers.getFirst().getPreSummarizerVerb();
        sb.append(" " + preSummarizerVerb + " ");
        for (int i = 0; i < summarizers.size(); ++i) {
            if (i == 1 && summarizers.size() == 2) {
                sb.append(" ");
            }
            else if (i > 0) {
                sb.append(", ");
            }
            if (i > 0 && i == summarizers.size() - 1) {
                sb.append("and ");
            }

            if (i > 0 && summarizers
                    .get(i)
                    .getPreSummarizerVerb()
                    .equals(preSummarizerVerb)
            ) {
                preSummarizerVerb = summarizers.get(i).getPreSummarizerVerb();
                sb.append(preSummarizerVerb + " ");
            }

            sb.append(summarizers.get(i).getLabel());
            if (!summarizers.get(i).getPostLabelStr().isEmpty()) {
                sb.append(" " + summarizers.get(i).getPostLabelStr());
            }
        }

        return sb.toString();
    }
}
