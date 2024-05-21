package com.example.FuzzyLogic;

public class ContinuousInterval implements Interval<Float> {

    private Float start;
    private Float end;

    public ContinuousInterval(Float start, Float end) throws Exception {
        if (start > end) {
            // TODO: Use custom exception
            throw new Exception(
                "The beginning of the interval should not be greater than " +
                "the end"
            );
        }
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean contains(Float x) {
        return (x >= start && x <= end);
    }

    @Override
    public Float getStart() {
        return start;
    }

    @Override
    public Float getEnd() {
        return end;
    }

    @Override
    public Float size() {
        return end - start;
    }
}
