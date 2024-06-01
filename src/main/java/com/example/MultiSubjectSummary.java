package com.example;

import java.util.List;

public class MultiSubjectSummary {

    public static enum FORM { F1, F2, F3, F4 };

    private RelativeQuantifier quantifier = null;
    private List<QualifierSummarizer> qualifiers = null;
    private List<QualifierSummarizer> summarizers;
    private FORM form;

    public MultiSubjectSummary(List<QualifierSummarizer> summarizers) {
        this.summarizers = summarizers;
        this.form = FORM.F4;
    }

    public MultiSubjectSummary(
        RelativeQuantifier quantifier,
        List<QualifierSummarizer> summarizers
    ) {
        this.quantifier = quantifier;
        this.summarizers = summarizers;
        this.form = FORM.F1;
    }

    public MultiSubjectSummary(
        RelativeQuantifier quantifier,
        List<QualifierSummarizer> qualifiers,
        List<QualifierSummarizer> summarizers,
        FORM f
    ) throws Exception {
        if (f != FORM.F2 && f != FORM.F3) {
            throw new Exception("Invalid multi subject summary arguments");
        }

        this.quantifier = quantifier;
        this.qualifiers = qualifiers;
        this.summarizers = summarizers;
        this.form = f;
    }
    
    public float getQuality(
        List<Subject> subjects1, List<Subject> subjects2
    ) throws Exception {
        float s1_summarizer_sigma_count = 0.0f;
        float s2_summarizer_sigma_count = 0.0f;
        float s1_summarizer_and_qualifier_sigma_count = 0.0f;
        float s2_summarizer_and_qualifier_sigma_count = 0.0f;
        float implication_sigma_count = 0.0f;

        for (var s1 : subjects1) {
            float s1_summarizer_grade = 1.0f;
            float summarizer_qualifier_grade = 1.0f;

            for (var summarizer : summarizers) {
                s1_summarizer_grade = (float)Math.min(
                    s1_summarizer_grade,
                    summarizer.qualify(s1)
                );
            }

            if (qualifiers != null) {
                summarizer_qualifier_grade = (float)Math.min(
                    summarizer_qualifier_grade,
                    s1_summarizer_grade
                );
            }

            s1_summarizer_sigma_count += s1_summarizer_grade;
            s1_summarizer_and_qualifier_sigma_count
                += summarizer_qualifier_grade;

            implication_sigma_count += Math.min(
                1,
                1 - s1_summarizer_grade + 0.0
            );
        }

        for (Subject s2 : subjects2) {
            float summarizer_grade = 1.0f;
            float summarizer_qualifier_grade = 1.0f;

            for (int i = 0; i < summarizers.size(); ++i) {
                summarizer_grade = (float)Math.min(
                    summarizer_grade,
                    summarizers.get(i).qualify(s2)
                );
            }

            if (qualifiers != null) {
                summarizer_qualifier_grade = (float)Math.min(
                    summarizer_qualifier_grade,
                    summarizer_grade
                );
            }

            s2_summarizer_sigma_count += summarizer_grade;
            s2_summarizer_and_qualifier_sigma_count
                += summarizer_qualifier_grade;

            implication_sigma_count += Math.min(
                1,
                1 - 0.0 + s2_summarizer_sigma_count
            );
        }

        float invM1 = 1 / (float)subjects1.size();
        float invM2 = 1 / (float)subjects2.size();

        float quality = 0.0f;
        if (form == FORM.F1) {
            quality = quantifier.grade(
                invM1 * s1_summarizer_sigma_count / (
                    invM1 * s1_summarizer_sigma_count
                        + invM2 * s2_summarizer_sigma_count
                )
            );
        } else if (form == FORM.F2) {
            quality = quantifier.grade(
                invM1 * s1_summarizer_sigma_count / (
                    invM1 * s1_summarizer_sigma_count
                        + invM2 * s2_summarizer_and_qualifier_sigma_count
                )
            );
        } else if (form == FORM.F3) {
            quality = quantifier.grade(
                invM1 * s1_summarizer_and_qualifier_sigma_count / (
                    invM1 * s1_summarizer_and_qualifier_sigma_count
                        + invM2 * s2_summarizer_sigma_count
                )
            );
        } else if (form == FORM.F4) {
            quality = 1 - (1 / (float)(subjects1.size() + subjects2.size()))
                * implication_sigma_count;
        }

        return quality;
    }

    public String toString(String s1name, String s2name) {
        StringBuilder sb = new StringBuilder();

        if (form == FORM.F4) {
            sb.append("More " + s1name + " than " + s2name);
        } else {
            sb.append(quantifier.getLabel() + " " + s1name);
        }

        if (form == FORM.F1) {
            sb.append(" compared to " + s2name);
        } else if (form == FORM.F2) {
            sb.append(", compared to " + s2name);
        }

        if (form == FORM.F2 || form == FORM.F3) {
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

            sb.append(",");
        }

        if (form == FORM.F3) {
            sb.append(" compared to " + s2name + ",");
        }

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
