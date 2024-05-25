package com.example;

import java.util.ArrayList;
import java.util.List;

public class Summary<S> {
    public static final int QUALITIES_COUNT = 10;

    private RelativeQuantifier relativeQuantifier = null;
    private AbsoluteQuantifier absoluteQuantifier = null;
    private List<QualifierSummarizer<S>> qualifiers = null;
    private List<QualifierSummarizer<S>> summarizers;
    private float[] qualities = new float[QUALITIES_COUNT];
    private boolean qualitiesCalculated = false;

    public Summary(
        RelativeQuantifier quantifier,
        List<QualifierSummarizer<S>> summarizers
    ) {
        this.relativeQuantifier = quantifier;
        this.summarizers = new ArrayList<>(summarizers);
    }

    public Summary(
        AbsoluteQuantifier quantifier,
        List<QualifierSummarizer<S>> summarizers
    ) {
        this.absoluteQuantifier = quantifier;
        this.summarizers = new ArrayList<>(summarizers);
    }

    public Summary(
        RelativeQuantifier quantifier,
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
                    qualities[0] = relativeQuantifier
                        .grade(summarizer_and_qualifier_sigma_count
                            / qualifier_sigma_count);
                } else if (relativeQuantifier != null) {
                    qualities[0] = relativeQuantifier
                        .grade(summarizer_sigma_count / (float)subjects.size());
                } else if (absoluteQuantifier != null) {
                    qualities[0] = absoluteQuantifier
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
                if (!qualifiers.get(i).getFeatureName().isEmpty()) {
                    sb.append(" " + qualifiers.get(i).getFeatureName());
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
            if (!summarizers.get(i).getFeatureName().isEmpty()) {
                sb.append(" " + summarizers.get(i).getFeatureName());
            }
        }

        return sb.toString();
    }
}
