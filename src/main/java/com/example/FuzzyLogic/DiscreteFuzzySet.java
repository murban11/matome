package com.example.FuzzyLogic;

public class DiscreteFuzzySet implements FuzzySet<Integer> {

    private DiscreteInterval universum;
    private Membership<Integer> membership;

    public DiscreteFuzzySet(
        DiscreteInterval universum,
        Membership<Integer> membership
    ) {
        this.universum = universum;
        this.membership = membership;
    }

    @Override
    public float grade(Integer x) {
        return membership.grade(x);
    }

    @Override
    public boolean contains(Integer x) {
        return universum.contains(x);
    }

    @Override
    public Float cardinality() {
        float cardinality = 0.0f;

        if (membership instanceof TrapezoidalMembership) {
            TrapezoidalMembership<Integer> tm
                = ((TrapezoidalMembership<Integer>)membership);

            // A -- B
            int start = universum.contains(tm.getA())
                ? tm.getA() : universum.getStart();
            int end = universum.contains(tm.getB())
                ? tm.getB() : universum.getEnd();
            for (int i = start; i <= end; ++i) {
                cardinality += tm.grade(i);
            }

            // B -- C
            start = universum.contains(tm.getB())
                ? tm.getB() : universum.getStart();
            end = universum.contains(tm.getC())
                ? tm.getC() : universum.getEnd();
            cardinality += (end - start);

            // C -- D
            start = universum.contains(tm.getC())
                ? tm.getC() : universum.getStart();
            end = universum.contains(tm.getD())
                ? tm.getD() : universum.getEnd();
            for (int i = start; i <= end; ++i) {
                cardinality += tm.grade(i);
            }

        } else if (membership instanceof GaussianMembership) {
            for (int i = universum.getStart(); i <= universum.getEnd(); ++i) {
                cardinality += membership.grade(i);
            }
        } else {
            assert(false);
        }

        return cardinality;
    }

    @Override
    public float height() {
        float height = 0.0f;

        if (membership instanceof TrapezoidalMembership) {
            TrapezoidalMembership<Integer> tm
                = ((TrapezoidalMembership<Integer>)membership);

            if (
                tm.getC() >= universum.getStart()
                && tm.getB() >= universum.getEnd()
            ) {
                height = 1.0f;
            } else if (
                tm.getB() >= universum.getStart()
                && tm.getA() >= universum.getEnd()
            ) {
                height = grade(universum.getEnd());
            } else if (
                tm.getD() >= universum.getStart()
                && tm.getC() >= universum.getEnd()
            ) {
                height = grade(universum.getStart());
            }
        } else if (membership instanceof GaussianMembership) {
            GaussianMembership<Integer> gm
                = ((GaussianMembership<Integer>)membership);

            if (
                gm.getMean() >= universum.getStart()
                && gm.getMean() <= universum.getEnd()
            ) {
                height = 1.0f;
            } else {
                height = Math.max(grade(universum.getStart()),
                    grade(universum.getEnd()));
            }
        } else {
            assert(false);
        }

        return height;
    }

    @Override
    public float degreeOfImprecision() {
        return support().cardinality() / (float)(universum.getEnd() - universum.getStart());
    }

    @Override
    public Interval<Integer> getUniversum() {
        return universum;
    }

    @Override
    public CrispSet<Integer, Integer> support() {
        int start = -1;
        int end = -1;

        if (membership instanceof TrapezoidalMembership) {
            TrapezoidalMembership<Integer> tm
                = ((TrapezoidalMembership<Integer>)membership);

            start = Math.max(tm.getA() + 1, universum.getStart());
            end = Math.min(tm.getD() + 1, universum.getEnd());
        } else if (membership instanceof GaussianMembership) {
            start = universum.getStart();
            end = universum.getEnd();
        } else {
            assert(false);
        }

        DiscreteCrispSet ret = null;
        try {
            ret = new DiscreteCrispSet(new DiscreteInterval(start, end));
        } catch (Exception e) {
            // TODO
        }

        return ret;
    }

    @Override
    public Membership<Integer> getMembership() {
        return membership;
    }
}
