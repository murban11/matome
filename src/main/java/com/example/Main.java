package com.example;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.example.Subject.Gender;
import com.example.SummaryGenerator.SummaryType;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

public class Main {
    public static void main(String[] args)
    {
        Options options = new Options();

        Option datasetFileOption = Option.builder("d")
            .longOpt("dataset")
            .argName("dataset")
            .hasArg()
            .desc("The name of the dataset file in CSV format")
            .build();
        options.addOption(datasetFileOption);

        Option configFileOption = Option.builder("c")
            .longOpt("config")
            .argName("config")
            .hasArg()
            .desc("The name of the configuration file in JSON format")
            .build();
        options.addOption(configFileOption);

        Option includeSummaryTypeOption = Option.builder("t")
            .longOpt("include-summary-types")
            .argName("types")
            .hasArg()
            .desc("Generate only summaries included in the given comma "
                + "separated list of summary types. Allowed values are:\n"
                + "ss1, ss2, ms1, ms2, ms3, ms4\n"
                + "where ss stands for single-subject and ms stands for "
                + "multi-subject\n"
                + "By default all possible summary types are generated")
            .build();
        options.addOption(includeSummaryTypeOption);

        Option onlyMalesOption = Option.builder("m")
            .longOpt("only-males")
            .desc("Consider only males in single-subject summaries")
            .build();
        options.addOption(onlyMalesOption);

        Option onlyFemalesOption = Option.builder("f")
            .longOpt("only-females")
            .desc("Consider only females in single-subject summaries")
            .build();
        options.addOption(onlyFemalesOption);

        Option showAllQualitiesOption = Option.builder("a")
            .longOpt("show-all-qualities")
            .desc("Show all qualities from T1 to T11 for single-subject summaries")
            .build();
        options.addOption(showAllQualitiesOption);

        Option help = Option.builder("h")
            .longOpt("help")
            .desc("Display help and exit")
            .build();
        options.addOption(help);

        HelpFormatter formatter = new HelpFormatter();

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            String datasetFile = "dataset.csv";
            if (cmd.hasOption("d")) {
                datasetFile = cmd.getOptionValue("d");
            }

            String configFile = "config.json";
            if (cmd.hasOption("c")) {
                configFile = cmd.getOptionValue("c");
            }

            short type = (short)0b0000000000111111;
            if (cmd.hasOption("t")) {
                type = (short)0b0000000000000000;
                for (String t : cmd.getOptionValue("t").split(",")) {
                    if (t.equals("ss1")) type |= SummaryType.SS1.id;
                    if (t.equals("ss2")) type |= SummaryType.SS2.id;
                    if (t.equals("ms1")) type |= SummaryType.MS1.id;
                    if (t.equals("ms2")) type |= SummaryType.MS2.id;
                    if (t.equals("ms3")) type |= SummaryType.MS3.id;
                    if (t.equals("ms4")) type |= SummaryType.MS4.id;
                }
            }

            if (cmd.hasOption("h")) {
                formatter.printHelp("matome", options);
                return;
            }

            List<Subject> subjects = Subject.loadFromFile(datasetFile);
            Config config = Config.load(configFile, subjects.size());

            float[] weights = {
                0.3f,
                0.1f, 0.1f, 0.1f, 0.1f, 0.1f,
                0.04f, 0.04f, 0.04f, 0.04f, 0.04f
            };

            if (config.weights != null) {
                for (int i = 0; i < config.weights.length && i < weights.length; ++i) {
                    weights[i] = config.weights[i];
                }
            }

            String subjectName = "people";
            if (cmd.hasOption("m")) {
                subjects = Subject.filterByGender(subjects, Gender.MALE);
                subjectName = "males";
            } else if (cmd.hasOption("f")) {
                subjects = Subject.filterByGender(subjects, Gender.FEMALE);
                subjectName = "females";
            }

            SummaryGenerator generator = new SummaryGenerator(
                config.relativeQuantifiers,
                config.absoluteQuantifiers,
                config.qualifierSummarizers,
                weights,
                subjects,
                subjectName
            );

            List<Pair<List<Float>, String>> summaries
                = generator.generate(type);

            for (var summary : summaries) {
                float quality = QualityAggregator
                    .calculate(summary.first, weights);

                StringBuilder sb = new StringBuilder(summary.second);
                sb.append(" [" + String.format("%.3f", quality) + "]");
                if (summary.first.size() > 1 && cmd.hasOption("a")) {
                    for (int i = 0; i < summary.first.size(); ++i) {
                        if (i == 0) {
                            sb.append("; ");
                        } else {
                            sb.append(", ");
                        }
                        sb.append("T" + (i + 1) + " = "
                            + String.format("%.3f", summary.first.get(i))
                        );
                    }
                }
                System.out.println(sb.toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamReadException e) {
            e.printStackTrace();
        } catch (DatabindException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
