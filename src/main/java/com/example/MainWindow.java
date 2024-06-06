package com.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
    private final int choiceBoxPrefWidth = 320;
    private final int choiceBoxItemFontSize = 20;

    private List<RelativeQuantifier> relativeQuantifiers;
    private List<AbsoluteQuantifier> absoluteQuantifiers;
    private List<QualifierSummarizer> qualifierSummarizers;
    private List<Subject> subjects;
    private final List<Feature> features = Arrays.asList(Feature.values());

    private List<RelativeQuantifier> selectedRelativeQuantifiers;
    private List<AbsoluteQuantifier> selectedAbsoluteQuantifiers;
    private List<Feature> selectedFeatures;

    private List<ChoiceBox<String>> quantitiesCBes = new ArrayList<>();
    private List<ChoiceBox<String>> featuresCBes = new ArrayList<>();
    private ScrollPane summariesSP = new ScrollPane();
    private VBox summariesVB = new VBox();

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

        Label quantitiesL = new Label("Quantities:");
        quantitiesL.setFont(new Font(fontName, fontSize));

        VBox quantitiesVB = new VBox();
        quantitiesVB.setAlignment(Pos.CENTER);
        quantitiesVB.getChildren().add(quantitiesL);

        for (int n = 0; n < selectionItemsCount; ++n) {
            Label nQuantitiesL = new Label((n + 1) + ". ");
            nQuantitiesL.setFont(new Font(fontName, fontSize));
            ChoiceBox<String> quantitiesCB = new ChoiceBox<>();
            quantitiesCB
                .setStyle("-fx-font-size: " + choiceBoxItemFontSize + "px;");
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
        featuresL.setFont(new Font(fontName, fontSize));

        VBox featuresVB = new VBox();
        featuresVB.setAlignment(Pos.CENTER);
        featuresVB.getChildren().add(featuresL);

        for (int n = 0; n < selectionItemsCount; ++n) {
            Label nFeaturesL = new Label((n + 1) + ". ");
            nFeaturesL.setFont(new Font(fontName, fontSize));
            ChoiceBox<String> featuresCB = new ChoiceBox<>();
            featuresCB
                .setStyle("-fx-font-size: " + choiceBoxItemFontSize + "px;");
            featuresCB.setPrefWidth(choiceBoxPrefWidth);
            for (var feature : features) {
                featuresCB.getItems().add(feature.name);
            }
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

        Region spacerLeft = new Region();
        Region spacerMiddle = new Region();
        Region spacerRight = new Region();

        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        HBox.setHgrow(spacerMiddle, Priority.ALWAYS);
        HBox.setHgrow(spacerRight, Priority.ALWAYS);

        HBox quantitiesAndFeautersHB = new HBox();
        quantitiesAndFeautersHB.setAlignment(Pos.CENTER);
        quantitiesAndFeautersHB
            .getChildren()
            .addAll(
                spacerLeft,
                quantitiesVB,
                spacerMiddle,
                featuresVB,
                spacerRight
            );

        Button generateBtn = new Button("Generate");
        generateBtn.setFont(new Font(fontName, fontSize));
        generateBtn.setOnAction(event -> {
            List<QualifierSummarizer> selectedQualifierSummarizers
                = new ArrayList<>();
            for (QualifierSummarizer qs : qualifierSummarizers) {
                if (selectedFeatures.contains(qs.getFeature())) {
                    selectedQualifierSummarizers.add(qs);
                }
            }

            try {
                SummaryGenerator generator = new SummaryGenerator(
                    selectedRelativeQuantifiers,
                    selectedAbsoluteQuantifiers,
                    selectedQualifierSummarizers,
                    weights,
                    subjects
                );

                short type = (short)0b0000000000111111;
                List<Pair<Float, String>> summaries
                    = generator.generate(type);

                summariesVB.getChildren().clear();
                for (var summary : summaries) {
                    Label summaryL = new Label(summary.second);
                    summaryL.setFont(new Font(fontName, fontSize));
                    summariesVB.getChildren().add(summaryL);
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
