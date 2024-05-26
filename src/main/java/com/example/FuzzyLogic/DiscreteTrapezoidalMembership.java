package com.example.FuzzyLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DiscreteTrapezoidalMembership
    extends TrapezoidalMembership<Integer> {

    private Integer a, b, c, d;

    public DiscreteTrapezoidalMembership(
        Integer a, Integer b, Integer c, Integer d
    ) {
        // Make the following relation to be always fullfilled:
        // a <= b <= c <= d
        List<Integer> points = new ArrayList<>();
        Collections.addAll(points, a, b, c, d);
        Collections.sort(points);
        Iterator<Integer> iter = points.iterator();
        this.a = iter.next();
        this.b = iter.next();
        this.c = iter.next();
        this.d = iter.next();
    }

    @Override
    public float grade(Integer x) {
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
    public Integer getA() {
        return a;
    }

    @Override
    public Integer getB() {
        return b;
    }

    @Override
    public Integer getC() {
        return c;
    }

    @Override
    public Integer getD() {
        return d;
    }
}
