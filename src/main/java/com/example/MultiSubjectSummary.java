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
        if (f != FORM.F2 || f != FORM.F3) {
            throw new Exception("Invalid multi subject summary arguments");
        }

        this.quantifier = quantifier;
        this.qualifiers = qualifiers;
        this.summarizers = summarizers;
        this.form = f;
    }
    
    public float getQuality(
        List<Subject> s1, List<Subject> s2
    ) throws Exception {
        if (form != FORM.F1) {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        float s1_summarizer_sigma_count = 0.0f;
        float s2_summarizer_sigma_count = 0.0f;

        for (Subject subject : s1) {
            float summarizer_grade = 1.0f;

            for (int i = 0; i < summarizers.size(); ++i) {
                summarizer_grade = (float)Math.min(
                    summarizer_grade,
                    summarizers.get(i).qualify(subject)
                );
            }

            s1_summarizer_sigma_count += summarizer_grade;
        }

        for (Subject subject : s2) {
            float summarizer_grade = 1.0f;

            for (int i = 0; i < summarizers.size(); ++i) {
                summarizer_grade = (float)Math.min(
                    summarizer_grade,
                    summarizers.get(i).qualify(subject)
                );
            }

            s2_summarizer_sigma_count += summarizer_grade;
        }

        float invM1 = 1 / (float)s1.size();
        float invM2 = 1 / (float)s2.size();

        float quality = quantifier.grade(
            invM1 * s1_summarizer_sigma_count / (
                invM1 * s1_summarizer_sigma_count
                    + invM2 * s2_summarizer_sigma_count
            )
        );

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
