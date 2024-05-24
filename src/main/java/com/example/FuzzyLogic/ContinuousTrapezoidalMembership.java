package com.example.FuzzyLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ContinuousTrapezoidalMembership
    extends TrapezoidalMembership<Float> {

    private Float a, b, c, d;

    public ContinuousTrapezoidalMembership(
        Float a, Float b, Float c, Float d
    ) {
        // Make the following relation to be always fullfilled:
        // a <= b <= c <= d
        List<Float> points = new ArrayList<>();
        Collections.addAll(points, a, b, c, d);
        Collections.sort(points);
        Iterator<Float> iter = points.iterator();
        this.a = iter.next();
        this.b = iter.next();
        this.c = iter.next();
        this.d = iter.next();
    }

    @Override
    public float grade(Float x) {
        if (x >= a && x < b) {
            return (x - a) / (b - a);
        } else if (x >= b && x < c) {
            return 1;
        } else if (x >= c && x < d) {
            return (d - x) / (d - c);
        } else {
            return 0;
        }
    }

    @Override
    public Float getA() {
        return a;
    }

    @Override
    public Float getB() {
        return b;
    }

    @Override
    public Float getC() {
        return c;
    }

    @Override
    public Float getD() {
        return d;
    }
}
