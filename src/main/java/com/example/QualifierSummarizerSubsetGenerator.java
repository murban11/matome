package com.example;

import java.util.ArrayList;
import java.util.List;

public class QualifierSummarizerSubsetGenerator {

    /*
     * This list contains qualifiers/summarizers splitted in classes based
     * on the feature they are referring to.
     */
    private List<List<QualifierSummarizer>> classes;

    /*
     * Contains indexes of summarizers selected in each of the classes.
     * If some element of this list has the value of -1, that means no
     * summarizer is selected in that class.
     */
    private List<Integer> selectedSummarizers;

    /*
     * Contains indexes of qualifiers selected in each of the classes,
     * similar to the `selectedSummarizers` field. A qualifier should not be
     * selected from a class from which a summarizer is already selected.
     */
    private List<Integer> selectedQualifiers;

    public QualifierSummarizerSubsetGenerator(
        List<QualifierSummarizer> qualifiersSummarizers
    ) {
        this.classes = new ArrayList<>();
        for (QualifierSummarizer qs : qualifiersSummarizers) {
            boolean classFound = false;
            for (var c : classes) {
                if (qs.getPostLabelStr().equals(c.get(0).getPostLabelStr())) {
                    c.add(qs);
                    classFound = true;
                    break;
                }
            }
            if (!classFound) {
                List<QualifierSummarizer> newClass = new ArrayList<>();
                newClass.add(qs);
                classes.add(newClass);
            }
        }

        this.selectedSummarizers = new ArrayList<>(classes.size());
        this.selectedQualifiers = new ArrayList<>(classes.size());
        for (int i = 0; i < classes.size(); ++i) {
            this.selectedSummarizers.add(-1);
            this.selectedQualifiers.add(-1);
        }
    }

    public boolean hasNextSummarizers() {
        for (int i = 0; i < classes.size(); ++i) {
            if (selectedSummarizers.get(i) < classes.get(i).size() - 1) {
                return true;
            }
        }
        return false;
    }

    public boolean hasNextQualifiers() {
        for (int i = 0; i < classes.size(); ++i) {
            if (
                selectedSummarizers.get(i) == -1
                    && selectedQualifiers.get(i) < classes.get(i).size() - 1
            ) {
                return true;
            }
        }
        return false;
    }

    public List<QualifierSummarizer> nextSummarizers() {
        updateSelectedSummarizers();

        List<QualifierSummarizer> summarizers
            = new ArrayList<>(classes.size());

        for (int i = 0; i < classes.size(); ++i) {
            int selectedIndex = selectedSummarizers.get(i);
            if (selectedIndex == -1) {
                continue;
            }
            summarizers.add(classes.get(i).get(selectedIndex));
        }

        return summarizers;
    }

    public List<QualifierSummarizer> nextQualifiers() {
        updateSelectedQualifiers();

        List<QualifierSummarizer> qualifiers
            = new ArrayList<>(classes.size());

        for (int i = 0; i < classes.size(); ++i) {
            int selectedIndex = selectedQualifiers.get(i);
            if (selectedIndex == -1) {
                continue;
            }
            qualifiers.add(classes.get(i).get(selectedIndex));
        }

        return qualifiers;
    }

    private void updateSelectedSummarizers() {
        for (int i = 0; i < classes.size(); ++i) {
            int ss = selectedSummarizers.get(i);
            if (ss < classes.get(i).size() - 1) {
                selectedSummarizers.set(i, ss + 1);
                break;
            } else if (i < classes.size() - 1) {
                selectedSummarizers.set(i, -1);
                for (int j = i + 1; j < classes.size(); ++j) {
                    int ssj = selectedSummarizers.get(j);
                    if (ssj < classes.get(j).size() - 1) {
                        selectedSummarizers.set(j, ssj + 1);
                        return;
                    }
                    selectedSummarizers.set(j, -1);
                }
                break;
            }
        }
        resetSelectedQualifiers();
    }

    private void updateSelectedQualifiers() {
        for (int i = 0; i < classes.size(); ++i) {
            int qs = selectedQualifiers.get(i);
            int ss = selectedSummarizers.get(i);
            if (ss == -1 && qs < classes.get(i).size() - 1) {
                selectedQualifiers.set(i, qs + 1);
                break;
            } else if (ss == -1 && i < classes.size() - 1) {
                selectedQualifiers.set(i, -1);
                for (int j = i + 1; j < classes.size(); ++j) {
                    int ssj = selectedSummarizers.get(j);
                    if (ssj != -1) {
                        continue;
                    }
                    int qsj = selectedQualifiers.get(j);
                    if (qsj < classes.get(j).size() - 1) {
                        selectedQualifiers.set(j, qsj + 1);
                        return;
                    }
                    selectedQualifiers.set(j, -1);
                }
            }
        }
    }

    private void resetSelectedQualifiers() {
        for (int i = 0; i < classes.size(); ++i) {
            selectedQualifiers.set(i, -1);
        }
    }
}
