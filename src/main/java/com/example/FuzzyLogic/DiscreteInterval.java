package com.example.FuzzyLogic;

public class DiscreteInterval implements Interval<Integer> {

    private Integer start;
    private Integer end;

    public DiscreteInterval(Integer start, Integer end) throws Exception {
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
    public boolean contains(Integer x) {
        return (x >= start && x <= end);
    }

    @Override
    public Integer getStart() {
        return start;
    }

    @Override
    public Integer getEnd() {
        return end;
    }

    @Override
    public Integer size() {
        return end - start;
    }
}
