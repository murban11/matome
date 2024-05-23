package com.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Subject {
    public enum Gender {
        MALE,
        FEMALE
    }

    private float age;
    private Gender gender;
    private float height;
    private float bmi;
    private float modifiedBodyFat;
    private float diastolic;
    private float systolic;
    private float gripForce;
    private float sitAndBendForward;
    private float sitUpCount;
    private float broadJump;

    public static List<Subject> loadFromFile(
        String filename
    ) throws FileNotFoundException {
        List<Subject> subjects = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(filename))) {
            scanner.nextLine(); // Ignore header line
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                try (Scanner rowScanner = new Scanner(line)) {
                    rowScanner.useDelimiter(",");

                    float age = Float.parseFloat(rowScanner.next());
                    Gender gender = (rowScanner.next().equals("M"))
                        ? Gender.MALE : Gender.FEMALE;
                    float height = Float.parseFloat(rowScanner.next());
                    @SuppressWarnings("unused")
                    float weight = Float.parseFloat(rowScanner.next());
                    float bmi = Float.parseFloat(rowScanner.next());
                    @SuppressWarnings("unused")
                    float bodyFat = Float.parseFloat(rowScanner.next());
                    float modifiedBodyFat = Float.parseFloat(rowScanner.next());
                    float diastolic = Float.parseFloat(rowScanner.next());
                    float systolic = Float.parseFloat(rowScanner.next());
                    float gripForce = Float.parseFloat(rowScanner.next());
                    float sitAndBendForward = Float.parseFloat(rowScanner.next());
                    float sitUpCount = Float.parseFloat(rowScanner.next());
                    float broadJump = Float.parseFloat(rowScanner.next());

                    subjects.addLast(new Subject(
                        age,
                        gender,
                        height,
                        bmi,
                        modifiedBodyFat,
                        diastolic,
                        systolic,
                        gripForce,
                        sitAndBendForward,
                        sitUpCount,
                        broadJump
                    ));
                }
            }
        }

        return subjects;
    }
}
