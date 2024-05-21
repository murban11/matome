package com.example.FuzzyLogic;

public class ContinuousFuzzySet implements FuzzySet<Float> {

    private ContinuousInterval universum;
    private Membership<Float> membership;

    public ContinuousFuzzySet(
        ContinuousInterval universum,
        Membership<Float> membership
    ) {
        this.universum = universum;
        this.membership = membership;
    }

    @Override
    public float grade(Float x) {
        return membership.grade(x);
    }

    @Override
    public boolean contains(Float x) {
        return universum.contains(x);
    }

    @Override
    public Float cardinality() {
        float cardinality = 0.0f;

        if (membership instanceof TrapezoidalMembership) {
            TrapezoidalMembership<Float> tm
                = ((TrapezoidalMembership<Float>)membership);

            float a = tm.getA(), b = tm.getB(), c = tm.getC(), d = tm.getD();
            float su = universum.getStart(), eu = universum.getEnd();

            if (b >= su) {
                float t = Math.max(a, su);
                float u = Math.min(b, eu);
                float s = (grade(u) - grade(t)) / (u - t);

                cardinality += s * (u*u / 2.0f - t*t / 2.0f);
            }
            if (c >= su) {
                float t = Math.max(b, su);
                float u = Math.min(d, eu);

                cardinality += u - t;
            }
            if (d >= su) {
                float t = Math.max(c, su);
                float u = Math.min(d, eu);
                float s = (grade(t) - grade(u)) / (u - t);

                cardinality += s * (u*u / 2.0f - t*t / 2.0f);
            }
        } else if (membership instanceof GaussianMembership) {
            throw new UnsupportedOperationException("Unimplemented");
        } else {
            assert(false);
        }

        return cardinality;
    }

    @Override
    public float height() {
        float height = 0.0f;

        if (membership instanceof TrapezoidalMembership) {
            TrapezoidalMembership<Float> tm
                = ((TrapezoidalMembership<Float>)membership);

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
            GaussianMembership<Float> gm
                = ((GaussianMembership<Float>)membership);

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
    public CrispSet<Float, Float> support() {
        float start = -1.0f;
        float end = -1.0f;

        if (membership instanceof TrapezoidalMembership) {
            TrapezoidalMembership<Float> tm
                = ((TrapezoidalMembership<Float>)membership);

            start = Math.max(tm.getA(), universum.getStart());
            end = Math.min(tm.getD(), universum.getEnd());
        } else if (membership instanceof GaussianMembership) {
            start = universum.getStart();
            end = universum.getEnd();
        } else {
            assert(false);
        }

        ContinuousCrispSet ret = null;
        try {
            ret = new ContinuousCrispSet(new ContinuousInterval(start, end));
        } catch (Exception e) {
            // TODO
        }

        return ret;
    }

    @Override
    public float degreeOfImprecision() {
        return support().cardinality() / (universum.getEnd() - universum.getStart());
    }

    @Override
    public Interval<Float> getUniversum() {
        return universum;
    }
}
