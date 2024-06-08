package com.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.Subject.Gender;
import com.example.SummaryGenerator.SummaryType;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MainWindow extends Application {

    private final String datasetFile = "dataset.csv";
    private final String configFile = "config.json";

    private final String fontName = "Arial";
    private final int fontSize = 32;
    private final int smallFontSize = 20;
    private final int choiceBoxPrefWidth = 320;

    private List<RelativeQuantifier> relativeQuantifiers;
    private List<AbsoluteQuantifier> absoluteQuantifiers;
    private List<QualifierSummarizer> qualifierSummarizers;
    private List<Subject> subjects;
    private final List<Feature> features = Arrays.asList(Feature.values());

    private List<RelativeQuantifier> selectedRelativeQuantifiers;
    private List<AbsoluteQuantifier> selectedAbsoluteQuantifiers;
    private List<Feature> selectedFeatures;
    private Gender selectedGender = null; // null means both

    private List<ChoiceBox<String>> quantitiesCBes = new ArrayList<>();
    private List<ChoiceBox<String>> featuresCBes = new ArrayList<>();
    private List<ChoiceBox<String>> qualifiersCBes = new ArrayList<>();
    private ScrollPane summariesSP = new ScrollPane();
    private VBox summariesVB = new VBox();
    private CheckBox multiSubjectToggle = new CheckBox(
        "Enable mutlti-subject summaries"
    );
    private RadioButton onlyMalesRB = new RadioButton("Only males");
    private RadioButton onlyFemalsRB = new RadioButton("Only females");
    private RadioButton bothGendersRB = new RadioButton("Both genders");

    private int selectionItemsCount = 3;

    private float[] weights = {
        0.3f,
        0.1f, 0.1f, 0.1f, 0.1f, 0.1f,
        0.04f, 0.04f, 0.04f, 0.04f, 0.04f
    };

    @Override
    public void start(Stage stage) throws Exception {
        loadData();

        selectedRelativeQuantifiers = new ArrayList<>();
        selectedAbsoluteQuantifiers = new ArrayList<>();
        selectedFeatures = new ArrayList<>();

        Font normalFont = new Font(fontName, fontSize);
        Font smallFont = new Font(fontName, smallFontSize);

        Label quantitiesL = new Label("Quantities:");
        quantitiesL.setFont(normalFont);

        VBox quantitiesVB = new VBox();
        quantitiesVB.setAlignment(Pos.CENTER);
        quantitiesVB.getChildren().add(quantitiesL);

        for (int n = 0; n < selectionItemsCount; ++n) {
            Label nQuantitiesL = new Label((n + 1) + ". ");
            nQuantitiesL.setFont(normalFont);
            ChoiceBox<String> quantitiesCB = new ChoiceBox<>();
            quantitiesCB
                .setStyle("-fx-font-size: " + smallFontSize + "px;");
            for (var quantifier : relativeQuantifiers) {
                quantitiesCB.getItems().add(quantifier.getLabel());
            }
            for (var quantifier : absoluteQuantifiers) {
                quantitiesCB.getItems().add(quantifier.getLabel());
            }
            quantitiesCB.setPrefWidth(choiceBoxPrefWidth);
            quantitiesCB
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(
                        ObservableValue<? extends String> observable,
                        String oldValue,
                        String newValue
                    ) {
                        // Remove the old value from selected
                        RelativeQuantifier oldRQ
                            = relativeQuantifierFromString(oldValue);
                        if (oldRQ != null) {
                            selectedRelativeQuantifiers.remove(oldRQ);
                        }
                        AbsoluteQuantifier oldAQ
                            = absoluteQuantifierFromString(oldValue);
                        if (oldAQ != null) {
                            selectedAbsoluteQuantifiers.remove(oldAQ);
                        }

                        // Add the new value to selected
                        RelativeQuantifier newRQ
                            = relativeQuantifierFromString(newValue);
                        if (
                            newRQ != null
                            && !selectedRelativeQuantifiers.contains(newRQ)
                        ) {
                            selectedRelativeQuantifiers.add(newRQ);
                        }
                        AbsoluteQuantifier newAQ
                            = absoluteQuantifierFromString(newValue);
                        if (
                            newAQ != null
                            && !selectedAbsoluteQuantifiers.contains(newAQ)
                        ) {
                            selectedAbsoluteQuantifiers.add(newAQ);
                        }

                        updateQuantitiesCBes();
                    }
                });
            quantitiesCBes.add(quantitiesCB);
            HBox nQuantitiesHB = new HBox();
            nQuantitiesHB.setAlignment(Pos.CENTER);
            nQuantitiesHB
                .getChildren()
                .addAll(nQuantitiesL, quantitiesCB);
            quantitiesVB.getChildren().add(nQuantitiesHB);
        }

        Label featuresL = new Label("Features:");
        featuresL.setFont(normalFont);

        VBox featuresVB = new VBox();
        featuresVB.setAlignment(Pos.CENTER);
        featuresVB.getChildren().add(featuresL);

        for (int n = 0; n < selectionItemsCount; ++n) {
            Label nFeaturesL = new Label((n + 1) + ". ");
            nFeaturesL.setFont(normalFont);
            ChoiceBox<String> featuresCB = new ChoiceBox<>();
            featuresCB
                .setStyle("-fx-font-size: " + smallFontSize + "px;");
            featuresCB.setPrefWidth(choiceBoxPrefWidth);
            for (var feature : features) {
                featuresCB.getItems().add(feature.name);
            }

            final int idx = n;
            featuresCB
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(
                        ObservableValue<? extends String> observable,
                        String oldValue,
                        String newValue
                    ) {
                        // Remove old value from selected
                        Feature oldFeature = featureFromString(oldValue);
                        if (oldFeature != null) {
                            selectedFeatures.remove(oldFeature);
                        }

                        // Add the new value to selected
                        Feature newFeature = featureFromString(newValue);
                        if (
                            newFeature != null
                            && !selectedFeatures.contains(newFeature)
                        ) {
                            selectedFeatures.add(newFeature);
                        }

                        updateFeaturesCBes();

                        if (newFeature != null) {
                            qualifiersCBes.get(idx).getItems().clear();
                            for (var qs : qualifierSummarizers) {
                                if (qs.getFeature().equals(newFeature)) {
                                    qualifiersCBes
                                        .get(idx)
                                        .getItems()
                                        .add(qs.getLabel());
                                }
                            }
                            qualifiersCBes.get(idx).setDisable(false);
                        }
                    }
                });
            featuresCBes.add(featuresCB);
            HBox nfeaturesHB = new HBox();
            nfeaturesHB.setAlignment(Pos.CENTER);
            nfeaturesHB
                .getChildren()
                .addAll(nFeaturesL, featuresCB);
            featuresVB.getChildren().add(nfeaturesHB);
        }

        Label qualifiersL = new Label("Criteria:");
        qualifiersL.setFont(normalFont);

        VBox qualifiersVB = new VBox();
        qualifiersVB.setAlignment(Pos.CENTER);
        qualifiersVB.getChildren().add(qualifiersL);

        for (int i = 0; i < selectionItemsCount; ++i) {
            ChoiceBox<String> qualifiersCB = new ChoiceBox<>();
            qualifiersCB
                .setStyle("-fx-font-size: " + smallFontSize + "px;");
            qualifiersCB.setPrefWidth(choiceBoxPrefWidth);
            qualifiersCB.setDisable(true);

            qualifiersCBes.add(qualifiersCB);

            HBox nQualifiersHB = new HBox();
            nQualifiersHB.setAlignment(Pos.CENTER);
            nQualifiersHB
                .getChildren()
                .add(qualifiersCB);
            qualifiersVB.getChildren().add(nQualifiersHB);
        }

        Label subjectSelectL = new Label("Subjects:");
        subjectSelectL.setFont(normalFont);

        VBox subjectSelectVB = new VBox(16);
        subjectSelectVB.setAlignment(Pos.CENTER_LEFT);
        subjectSelectVB.getChildren().add(subjectSelectL);

        ToggleGroup subjectSelectTG = new ToggleGroup();

        onlyMalesRB = new RadioButton("Only males");
        onlyMalesRB.setFont(smallFont);
        onlyMalesRB.setToggleGroup(subjectSelectTG);
        onlyMalesRB
            .selectedProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    selectedGender = Gender.MALE;
                    multiSubjectToggle.setDisable(true);
                    multiSubjectToggle.selectedProperty().setValue(false);
                }
        });

        onlyFemalsRB = new RadioButton("Only females");
        onlyFemalsRB.setToggleGroup(subjectSelectTG);
        onlyFemalsRB.setFont(smallFont);
        onlyFemalsRB
            .selectedProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    selectedGender = Gender.FEMALE;
                    multiSubjectToggle.setDisable(true);
                    multiSubjectToggle.selectedProperty().setValue(false);
                }
        });

        bothGendersRB = new RadioButton("Both genders");
        bothGendersRB.setToggleGroup(subjectSelectTG);
        bothGendersRB.setSelected(true);
        bothGendersRB.setFont(smallFont);
        bothGendersRB
            .selectedProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    selectedGender = null;
                    multiSubjectToggle.setDisable(false);
                }
        });

        subjectSelectVB
            .getChildren()
            .addAll(onlyMalesRB, onlyFemalsRB, bothGendersRB);

        Region firstSpacer = new Region();
        Region secondSpacer = new Region();
        Region thirdSpacer = new Region();
        Region fourthSpacer = new Region();

        HBox.setHgrow(firstSpacer, Priority.ALWAYS);
        HBox.setHgrow(secondSpacer, Priority.ALWAYS);
        HBox.setHgrow(thirdSpacer, Priority.ALWAYS);
        HBox.setHgrow(fourthSpacer, Priority.ALWAYS);

        HBox quantitiesAndFeautersHB = new HBox();
        quantitiesAndFeautersHB.setAlignment(Pos.CENTER);
        quantitiesAndFeautersHB
            .getChildren()
            .addAll(
                firstSpacer,
                quantitiesVB,
                secondSpacer,
                featuresVB,
                qualifiersVB,
                thirdSpacer,
                subjectSelectVB,
                fourthSpacer
            );

        multiSubjectToggle.selectedProperty().setValue(false);
        multiSubjectToggle.setFont(smallFont);

        Button generateBtn = new Button("Generate");
        generateBtn.setFont(normalFont);
        generateBtn.setOnAction(event -> {
            List<QualifierSummarizer> selectedQualifierSummarizers
                = new ArrayList<>();
            for (int i = 0; i < featuresCBes.size(); ++i) {
                Feature feature
                    = featureFromString(featuresCBes.get(i).getValue());
                if (feature == null) {
                    continue;
                }

                if (qualifiersCBes.get(i).getValue() == null) {
                    for (var qs : qualifierSummarizers) {
                        if (qs.getFeature().equals(feature)) {
                            selectedQualifierSummarizers.add(qs);
                        }
                    }
                } else {
                    for (var qs : qualifierSummarizers) {
                        if (
                            qs.getFeature().equals(feature)
                            && qs.getLabel().equals(qualifiersCBes.get(i).getValue())
                        ) {
                            selectedQualifierSummarizers.add(qs);
                        }
                    }
                }
            }

            try {
                String subjectName = "people";
                List<Subject> filteredSubjects = subjects;

                if (
                    selectedGender != null
                    && selectedGender.equals(Gender.MALE)
                ) {
                    subjectName = "males";
                    filteredSubjects
                        = Subject.filterByGender(subjects, Gender.MALE);
                } else if (
                    selectedGender != null
                    && selectedGender.equals(Gender.FEMALE)
                ) {
                    subjectName = "females";
                    filteredSubjects
                        = Subject.filterByGender(subjects, Gender.FEMALE);
                }

                SummaryGenerator generator = new SummaryGenerator(
                    selectedRelativeQuantifiers,
                    selectedAbsoluteQuantifiers,
                    selectedQualifierSummarizers,
                    weights,
                    filteredSubjects,
                    subjectName
                );

                short type = (short)0b0000000000111111;
                if (!multiSubjectToggle.isSelected()) {
                    type ^= SummaryType.MS1.id;
                    type ^= SummaryType.MS2.id;
                    type ^= SummaryType.MS3.id;
                    type ^= SummaryType.MS4.id;
                }

                List<Pair<List<Float>, String>> summaries
                    = generator.generate(type);

                summariesVB.getChildren().clear();
                for (var summary : summaries) {
                    float quality = QualityAggregator
                        .calculate(summary.first, weights);
                    Label summaryL = new Label(
                        summary.second + " ["
                            + String.format("%.2f", quality)
                            + "]"
                    );
                    summaryL.setFont(normalFont);
                    summariesVB.getChildren().add(summaryL);

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < summary.first.size(); ++i) {
                        if (i != 0) sb.append(", ");
                        sb.append(
                            "T" + (i + 1) + " = "
                                + String.format("%.2f", summary.first.get(i))
                        );
                    }

					Tooltip summaryTT = new Tooltip(sb.toString());
                    Tooltip.install(summaryL, summaryTT);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        summariesVB.setAlignment(Pos.CENTER_LEFT);
        summariesVB.setPadding(new Insets(16));
        summariesVB.setSpacing(8);

        summariesSP.setFitToWidth(true);
        summariesSP.setContent(summariesVB);

        VBox vbox = new VBox(16);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren()
            .addAll(
                quantitiesAndFeautersHB,
                multiSubjectToggle,
                generateBtn,
                summariesSP
            );

        stage.setScene(new Scene(vbox));
        stage.setTitle("Matome");
        stage.show();
    }

    private void loadData()
            throws StreamReadException, DatabindException, IOException {
        subjects = Subject.loadFromFile(datasetFile);
        Config config = Config.load(configFile, subjects.size());

        if (config.weights != null) {
            for (int i = 0; i < config.weights.length && i < weights.length; ++i) {
                weights[i] = config.weights[i];
            }
        }

        relativeQuantifiers = config.relativeQuantifiers;
        absoluteQuantifiers = config.absoluteQuantifiers;
        qualifierSummarizers = config.qualifierSummarizers;
    }

    private RelativeQuantifier relativeQuantifierFromString(String label) {
        for (var quantifier : relativeQuantifiers) {
            if (quantifier.getLabel().equals(label)) {
                return quantifier;
            }
        }
        return null;
    }

    private AbsoluteQuantifier absoluteQuantifierFromString(String label) {
        for (var quantifier : absoluteQuantifiers) {
            if (quantifier.getLabel().equals(label)) {
                return quantifier;
            }
        }
        return null;
    }

    private Feature featureFromString(String name) {
        for (var feature : Feature.values()) {
            if (feature.name.equals(name)) {
                return feature;
            }
        }
        return null;
    }

    private void updateQuantitiesCBes() {
        for (var qcb : quantitiesCBes) {
            String selected = qcb.getValue();

            for (var rq : relativeQuantifiers) {
                if (selected != null && selected.equals(rq.getLabel())) {
                    continue;
                } else if (
                    qcb.getItems().contains(rq.getLabel())
                    && selectedRelativeQuantifiers.contains(rq)
                ) {
                    qcb.getItems().remove(rq.getLabel());
                } else if (
                    !qcb.getItems().contains(rq.getLabel())
                    && !selectedRelativeQuantifiers.contains(rq)
                ) {
                    qcb.getItems().add(rq.getLabel());
                }
            }
            for (var rq : absoluteQuantifiers) {
                if (selected != null && selected.equals(rq.getLabel())) {
                    continue;
                } else if (
                    qcb.getItems().contains(rq.getLabel())
                    && selectedAbsoluteQuantifiers.contains(rq)
                ) {
                    qcb.getItems().remove(rq.getLabel());
                } else if (
                    !qcb.getItems().contains(rq.getLabel())
                    && !selectedAbsoluteQuantifiers.contains(rq)
                ) {
                    qcb.getItems().add(rq.getLabel());
                }
            }
        }
    }

    private void updateFeaturesCBes() {
        for (var fcb : featuresCBes) {
            String selected = fcb.getValue();

            for (var f : features) {
                if (selected != null && selected.equals(f.name)) {
                    continue;
                } else if (
                    fcb.getItems().contains(f.name)
                    && selectedFeatures.contains(f)
                ) {
                    fcb.getItems().remove(f.name);
                } else if (
                    !fcb.getItems().contains(f.name)
                    && !selectedFeatures.contains(f)
                ) {
                    fcb.getItems().add(f.name);
                }
            }
        }
    }
}
