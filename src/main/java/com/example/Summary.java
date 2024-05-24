package com.example;

import java.util.ArrayList;
import java.util.List;

// TODO: Get rid of T
public class Summary<T, S> {
    public static final int QUALITIES_COUNT = 10;

    private Quantifier<T> quantifier;
    private List<QualifierSummarizer<S>> qualifiers;
    private List<QualifierSummarizer<S>> summarizers;
    private float[] qualities = new float[QUALITIES_COUNT];
    private boolean qualitiesCalculated = false;

    public Summary(
        Quantifier<T> quantifier,
        List<QualifierSummarizer<S>> summarizers
    ) {
        this.quantifier = quantifier;
        this.qualifiers = null;
        this.summarizers = new ArrayList<>(summarizers);
    }

    public Summary(
        Quantifier<T> quantifier,
        List<QualifierSummarizer<S>> qualifiers,
        List<QualifierSummarizer<S>> summarizers
    ) {
        this(quantifier, summarizers);
        this.qualifiers = new ArrayList<>(qualifiers);
    }

    public float getQuality(int n, List<S> subjects) throws Exception {
        if (n < 0 || n >= QUALITIES_COUNT) {
            throw new Exception("Invalid index of summary quality");
        }

        // TODO: calculate other quality measures than T1
        if (!qualitiesCalculated) {
            float summarizer_sigma_count = 0.0f;
            float qualifier_sigma_count = 0.0f;
            float summarizer_and_qualifier_sigma_count = 0.0f;

            for (S subject : subjects) {
                float summarizer_grade = 1.0f;
                float summarizer_qualifier_grade = 1.0f;
                for (var summarizer : summarizers) {
                    summarizer_grade = (float)Math.min(
                        summarizer_grade,
                        summarizer.qualify(subject)
                    );

                    if (qualifiers != null) {
                        summarizer_qualifier_grade = (float)Math.min(
                            summarizer_qualifier_grade,
                            summarizer_grade
                        );
                    }
                }
                summarizer_sigma_count += summarizer_grade;

                if (qualifiers != null) {
                    float qualifier_grade = 1.0f;
                    for (var qualifier : qualifiers) {
                        qualifier_grade = (float)Math.min(
                            qualifier_grade,
                            qualifier.qualify(subject)
                        );

                        summarizer_qualifier_grade = (float)Math.min(
                            summarizer_qualifier_grade,
                            qualifier_grade
                        );
                    }
                    qualifier_sigma_count += qualifier_grade;
                }
                summarizer_and_qualifier_sigma_count
                    += summarizer_qualifier_grade;

                if (qualifiers != null) {
                    qualities[0] = ((RelativeQuantifier)quantifier)
                        .grade(summarizer_and_qualifier_sigma_count
                            / qualifier_sigma_count);
                } else if (quantifier instanceof RelativeQuantifier) {
                    qualities[0] = ((RelativeQuantifier)quantifier)
                        .grade(summarizer_sigma_count / (float)subjects.size());
                } else if (quantifier instanceof AbsoluteQuantifier) {
                    qualities[0] = ((AbsoluteQuantifier)quantifier)
                        .grade(Math.round(summarizer_sigma_count));
                } else {
                    assert(false);
                }
            }

            qualitiesCalculated = true;
        }

        return qualities[n];
    }

    public String toString(String subjectsName) {
        StringBuilder sb = new StringBuilder(
            quantifier.getLabel() + " " + subjectsName
        );
        if (qualifiers != null) {
            sb.append(" being/having ");
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
                sb.append(qualifiers.get(i).getLabel());
                // TODO: Add feature name
            }
        }
        sb.append(" are/have ");
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
            sb.append(summarizers.get(i).getLabel());
            // TODO: Add feature name
        }

        return sb.toString();
    }
}
