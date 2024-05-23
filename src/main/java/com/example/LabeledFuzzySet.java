package com.example;

import com.example.FuzzyLogic.FuzzySet;

public interface LabeledFuzzySet<T> extends FuzzySet<T> {
    String getLabel();
}
