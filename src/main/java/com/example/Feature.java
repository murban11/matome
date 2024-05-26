package com.example;

public enum Feature {
    AGE                     ("age"),
    HEIGHT                  ("height"),
    BMI                     ("bmi"),
    MODIFIED_BODY_FAT       ("modified body fat"),
    DIASTOLIC               ("diastolic"),
    SYSTOLIC                ("systolic"),
    GRIP_FORCE              ("grip force"),
    SIT_AND_BEND_FORWARD    ("sit and bend forward"),
    SIT_UPS_COUNT           ("sit-ups count"),
    BROAD_JUMP              ("broad jump");

    public final String name;

    Feature(String name) {
        this.name = name;
    }
}
