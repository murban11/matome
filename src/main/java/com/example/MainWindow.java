package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.example.Subject.Gender;
import com.example.SummaryGenerator.SummaryType;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainWindow extends Application {

    private final String datasetFile = "dataset.csv";
    private final String configFile = "config.json";

    private final String fontName = "Arial";
    private final int fontSize = 32;
    private final int smallFontSize = 20;
    private final int choiceBoxPrefWidth = 320;

    private final Font normalFont = new Font(fontName, fontSize);
    private final Font smallFont = new Font(fontName, smallFontSize);

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
    private Button saveSummariesBtn = new Button("Save");
    private ChoiceBox<String> sortByCB = new ChoiceBox<>();
    private HBox sortByAndLimitHB = new HBox(16);
    private List<TextField> weightsTF = new ArrayList<>();
    private HBox weightsAndFormsHB = new HBox();
    private List<CheckBox> formCBes = new ArrayList<>();

    private int selectionItemsCount = 3;

    private float[] weights = {
        0.3f,
        0.1f, 0.1f, 0.1f, 0.1f, 0.1f,
        0.04f, 0.04f, 0.04f, 0.04f, 0.04f
    };

    private List<Pair<List<Float>, String>> summaries;
    private short summaryTypes = (short)0b0000000000111111;
    private int summaryLimit = 0; // 0 means no limit

    private enum Mode {
        BASIC,
        ADVANCED,
        CONFIG
    };

    private Mode mode = Mode.BASIC;

    private Stage primaryStage;
    private Stage configStage = new Stage();

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        loadData();

        selectedRelativeQuantifiers = new ArrayList<>();
        selectedAbsoluteQuantifiers = new ArrayList<>();
        selectedFeatures = new ArrayList<>();

        MenuBar menuBar = createMenuBar();

        // ----------------- MULTI-SUBJECT TOGGLE -----------------
        multiSubjectToggle
                .selectedProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        for (int i = 2; i < formCBes.size(); ++i) {
                            formCBes.get(i).setSelected(true);
                        }
                    } else {
                        for (int i = 2; i < formCBes.size(); ++i) {
                            formCBes.get(i).setSelected(false);
                        }
                    }
                });
        // --------------------------------------------------------

        // ---------------------- QUANTITIES ----------------------
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

            // Add empty choice to allow to unselect choicess
            quantitiesCB.getItems().add("");

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
        // --------------------------------------------------------

        // ----------------------- FEATURES -----------------------
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

            // Add empty choice to allow to unselect choicess
            featuresCB.getItems().add("");

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

                            // Add empty choice to allow to unselect choicess
                            qualifiersCBes.get(idx).getItems().add("");

                            qualifiersCBes.get(idx).setDisable(false);
                        } else {
                            qualifiersCBes.get(idx).getItems().clear();
                            qualifiersCBes.get(idx).setDisable(true);
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
        // --------------------------------------------------------

        // ---------------------- QUALIFIERS ----------------------
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
        // --------------------------------------------------------

        // ----------------------- SUBJECTS -----------------------
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
                    for (int i = 2; i < formCBes.size(); ++i) {
                        formCBes.get(i).setSelected(false);
                        formCBes.get(i).setDisable(true);
                    }
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
                    for (int i = 2; i < formCBes.size(); ++i) {
                        formCBes.get(i).setSelected(false);
                        formCBes.get(i).setDisable(true);
                    }
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
                    for (int i = 2; i < formCBes.size(); ++i) {
                        formCBes.get(i).setDisable(false);
                    }
                }
        });

        subjectSelectVB
            .getChildren()
            .addAll(onlyMalesRB, onlyFemalsRB, bothGendersRB);
        // --------------------------------------------------------

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

        // ------------------------ SET WEIGHTS -----------------------
        Pattern pattern = Pattern.compile("^(0(\\.\\d{0,9})?|1(\\.0{0,1})?)?$");

        for (int i = 0; i < weights.length; ++i) {

            TextFormatter<String> textFormatter
                = new TextFormatter<String>(change -> {

                String newText = change.getControlNewText();
                if (pattern.matcher(newText).matches() || newText.isEmpty()) {
                    return change;
                }
                return null;
            });

            TextField wtf = new TextField();
            wtf.setFont(smallFont);
            wtf.setPrefWidth(70);
            wtf.setText(String.valueOf(weights[i]));
            final int idx = i;
            wtf.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) { // The TextField has lost focus
                    weights[idx] = Float.parseFloat(weightsTF.get(idx).getText());
                }
            });
            wtf.setTextFormatter(textFormatter);
            weightsTF.add(wtf);
        }

        int rowSize = 3;
        int hBoxesCount = (weights.length / rowSize)
            + ((weights.length % rowSize != 0) ? 1 : 0);
        List<HBox> setWeightsHBes = new ArrayList<>(hBoxesCount);
        for (int i = 0; i < hBoxesCount; ++i) {
            HBox setWeightsHB = new HBox();
            for (int j = i*rowSize; j < (i+1)*rowSize && j < weights.length; ++j) {
                Label wtfl = new Label("W" + (j + 1));
                wtfl.setFont(smallFont);
                wtfl.setMinWidth(70);
                wtfl.setPadding(new Insets(0, 0, 0, 24));
                setWeightsHB.getChildren().addAll(wtfl, weightsTF.get(j));
            }
            setWeightsHBes.add(setWeightsHB);
        }

        VBox setWeightsVB = new VBox();
        Label setWeightsL = new Label("Weights:");
        setWeightsL.setFont(normalFont);
        setWeightsVB.getChildren().add(setWeightsL);
        for (int i = 0; i < setWeightsHBes.size(); ++i) {
            setWeightsVB.getChildren().add(setWeightsHBes.get(i));
        }
        setWeightsVB.setAlignment(Pos.CENTER);
        // ------------------------------------------------------------

        // ------------------------ SELECT FORM -----------------------
        for (int i = 0; i < SummaryType.values().length; ++i) {
            SummaryType type = SummaryType.values()[i];
            CheckBox formCB = new CheckBox(type.toString());
            formCB.setFont(smallFont);
            formCB.setMinWidth(124);
            final int idx = i;
            formCB
                .selectedProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        summaryTypes |= SummaryType.values()[idx].id;
                    } else {
                        summaryTypes ^= SummaryType.values()[idx].id;
                    }
                });
            formCB.setSelected(true);
            formCBes.add(formCB);
        }

        rowSize = 3;
        int typeCount = SummaryType.values().length;
        hBoxesCount = (typeCount / rowSize)
            + ((typeCount % rowSize != 0) ? 1 : 0);
        List<HBox> selectFormHBes = new ArrayList<>(hBoxesCount);
        for (int i = 0; i < hBoxesCount; ++i) {
            HBox selectFormHB = new HBox();
            for (int j = i*rowSize; j < (i+1)*rowSize && j < typeCount; ++j) {
                selectFormHB.getChildren().add(formCBes.get(j));
            }
            selectFormHBes.add(selectFormHB);
        }

        VBox selectFormVB = new VBox();
        Label selectFormL = new Label("Forms:");
        selectFormL.setFont(normalFont);
        selectFormVB.getChildren().add(selectFormL);
        for (int i = 0; i < selectFormHBes.size(); ++i) {
            selectFormVB.getChildren().add(selectFormHBes.get(i));
            VBox.setMargin(selectFormHBes.get(i), new Insets(0, 0, 0, 254));
        }
        selectFormVB.setAlignment(Pos.CENTER);
        // ------------------------------------------------------------

        weightsAndFormsHB.setAlignment(Pos.CENTER);
        weightsAndFormsHB.getChildren().addAll(setWeightsVB, selectFormVB);
        weightsAndFormsHB.setVisible(false);
        weightsAndFormsHB.setManaged(false);

        // ------------------------- GENERATE -------------------------
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

                if (
                    qualifiersCBes.get(i).getValue() == null
                        || qualifiersCBes.get(i).getValue().isEmpty()
                ) {
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
                float weightsSum = 0.0f;
                for (int i = 0; i < weights.length; ++i) {
                    weightsSum += weights[i];
                }
                if (Math.abs(weightsSum - 1) > 0.001f) {
                    String errorMsg = "Weights does not sum up to 1.0";
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Invalid weights");
                    alert.setHeaderText(null);
                    alert.setContentText(errorMsg);
                    alert.showAndWait();

                    throw new Exception(errorMsg);
                }

                String subjectName = "people";
                List<Subject> filteredSubjects = subjects;
                float min_t1 = 0.3f;

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
                    subjectName,
                    min_t1
                );

                short type = (short)0b0000000000111111;
                if (!multiSubjectToggle.isSelected() && mode == Mode.BASIC) {
                    type ^= SummaryType.MS1.id;
                    type ^= SummaryType.MS2.id;
                    type ^= SummaryType.MS3.id;
                    type ^= SummaryType.MS4.id;
                } else if (mode == Mode.ADVANCED) {
                    type = summaryTypes;
                }

                summaries = generator.generate(type);

                updateSummaryList();

                saveSummariesBtn.setDisable(false);
                sortByCB.setDisable(false);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // ------------------------------------------------------------

        // --------------------------- SAVE ---------------------------
        saveSummariesBtn.setFont(normalFont);
        saveSummariesBtn.setDisable(true);
        saveSummariesBtn.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                List<String> summaries = new ArrayList<>();

                for (Node sl : summariesVB.getChildren()) {
                    if (sl instanceof TextField) {
                        summaries.add(((TextField)sl).getText());
                    }
                }

                try {
                    Files.write(file.toPath(), summaries);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // ------------------------------------------------------------

        HBox buttonsHB = new HBox(16);
        buttonsHB.setAlignment(Pos.CENTER);
        buttonsHB
            .getChildren()
            .addAll(generateBtn, saveSummariesBtn);

        // ------------------------- SORT BY --------------------------
        Label sortByL = new Label("Sort by: ");
        sortByL.setFont(smallFont);

        sortByCB = new ChoiceBox<>();
        sortByCB
            .setStyle("-fx-font-size: " + smallFontSize + "px;");
        sortByCB.setPrefWidth(choiceBoxPrefWidth);
        sortByCB.getItems().add("T");
        for (int i = 1; i <= 11; ++i) {
            sortByCB.getItems().add("T" + i);
        }
        sortByCB.setDisable(true);
        sortByCB
            .getSelectionModel()
            .selectedItemProperty()
            .addListener(new ChangeListener<String>() {

                @Override
                public void changed(
                    ObservableValue<? extends String> observable,
                    String oldValue,
                    String newValue
                ) {
                    if (newValue.equals(oldValue)) return;
                    if (summaries == null) return;

                    if (newValue.equals("T")) {
                        SummaryQualityComparator comparator
                            = new SummaryQualityComparator(weights);
                        Collections.sort(
                            summaries,
                            Collections.reverseOrder(comparator)
                        );
                        updateSummaryList();
                    } else {
                        int n = Integer.parseInt(newValue.substring(1)) - 1;
                        SummaryQualityPartComparator comparator;
                        try {
                            comparator = new SummaryQualityPartComparator(n);
                            Collections.sort(
                                summaries,
                                Collections.reverseOrder(comparator)
                            );
                            updateSummaryList();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        // ------------------------------------------------------------

        // -------------------------- LIMIT ---------------------------
        Label limitL = new Label("Limit: ");
        limitL.setFont(smallFont);

        Pattern limitPattern = Pattern.compile("^([1-9](\\d{0,9})?)$");

        TextFormatter<String> textFormatter
            = new TextFormatter<String>(change -> {

            String newText = change.getControlNewText();
            if (limitPattern.matcher(newText).matches() || newText.isEmpty()) {
                return change;
            }
            return null;
        });

        TextField limitTF = new TextField();
        limitTF.setFont(smallFont);
        limitTF.setPrefWidth(choiceBoxPrefWidth);
        limitTF.setTextFormatter(textFormatter);
        limitTF
            .textProperty()
            .addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    summaryLimit = 0;
                } else {
                    summaryLimit = Integer.parseInt(newValue);
                }
                updateSummaryList();
            });
        // ------------------------------------------------------------

        Region sortByAndLimitSpacer = new Region();
        HBox.setHgrow(sortByAndLimitSpacer, Priority.ALWAYS);

        sortByAndLimitHB.setAlignment(Pos.CENTER_LEFT);
        sortByAndLimitHB
            .getChildren()
            .addAll(sortByL, sortByCB, sortByAndLimitSpacer, limitL, limitTF);
        VBox.setMargin(sortByAndLimitHB, new Insets(0, 16, 0, 16));
        sortByAndLimitHB.setVisible(false);
        sortByAndLimitHB.setManaged(false);

        summariesVB.setAlignment(Pos.CENTER_LEFT);
        summariesVB.setPadding(new Insets(16));
        summariesVB.setSpacing(-16);

        summariesSP.setFitToWidth(true);
        summariesSP.setContent(summariesVB);

        VBox vbox = new VBox(16);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.getChildren()
            .addAll(
                menuBar,
                quantitiesAndFeautersHB,
                weightsAndFormsHB,
                multiSubjectToggle,
                buttonsHB,
                sortByAndLimitHB,
                summariesSP
            );

        stage.setScene(new Scene(vbox));
        stage.setTitle("Matome");
        stage.show();
    }

    private void showConfigStage(Stage configStage) {
        MenuBar menuBar = createMenuBar();

        TextArea code = new TextArea();
        VBox.setVgrow(code, Priority.ALWAYS);
        code.setFont(smallFont);

        Button save = new Button("Save");
        save.setFont(normalFont);
        save.setOnAction(e -> {
            String content = code.getText();
            File file = new File("config.json");
            try {
                Files.writeString(
                    file.toPath(), content, StandardCharsets.UTF_8
                );
                loadData();
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Config saved successfully");
                alert.showAndWait();
            }
        });

        VBox inner = new VBox(16);
        inner.setAlignment(Pos.TOP_CENTER);
        inner.setPadding(new Insets(16));
        inner.getChildren().addAll(code, save);
        VBox.setVgrow(inner, Priority.ALWAYS);

        VBox vbox = new VBox(16);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.getChildren().addAll(menuBar, inner);

        File file = new File("config.json");
        try {
            String content
                = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            code.setText(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        configStage.setScene(new Scene(vbox));
        configStage.setTitle("Matome");
        configStage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu modeMenu = new Menu("Mode");

        RadioMenuItem basicMode = new RadioMenuItem("Basic");
        RadioMenuItem advancedMode = new RadioMenuItem("Advanced");

        ToggleGroup modeGroup = new ToggleGroup();
        basicMode.setToggleGroup(modeGroup);
        advancedMode.setToggleGroup(modeGroup);

        basicMode.setSelected((mode == Mode.BASIC));
        advancedMode.setSelected((mode == Mode.ADVANCED));

        if (mode == Mode.CONFIG) {
            RadioMenuItem configMode = new RadioMenuItem("Config");
            configMode.setToggleGroup(modeGroup);
            configMode.setSelected((mode == Mode.CONFIG));

            modeMenu.getItems().addAll(basicMode, advancedMode, configMode);
        } else {
            MenuItem configMode = new MenuItem("Config");
            configMode.setOnAction(e -> {
                    mode = Mode.CONFIG;
                    showConfigStage(configStage);
                    primaryStage.hide();
                });

            modeMenu.getItems().addAll(basicMode, advancedMode, configMode);
        }

        modeGroup
            .selectedToggleProperty()
            .addListener((observable, oldValue, newValue) -> {
                RadioMenuItem selectedMode = (RadioMenuItem) newValue;

                for (var m : Mode.values()) {
                    String selectedModeStr
                    = selectedMode.getText().toLowerCase();
                    String currentModeStr = m.toString().toLowerCase();
                    if (currentModeStr.equals(selectedModeStr)) {
                        mode = m;
                        break;
                    }
                }

                if (mode == Mode.ADVANCED) {
                    sortByAndLimitHB.setVisible(true);
                    sortByAndLimitHB.setManaged(true);
                    weightsAndFormsHB.setVisible(true);
                    weightsAndFormsHB.setManaged(true);
                    multiSubjectToggle.setVisible(false);
                    multiSubjectToggle.setManaged(false);

                    configStage.hide();
                    primaryStage.show();
                } else if (mode == Mode.BASIC) {
                    sortByAndLimitHB.setVisible(false);
                    sortByAndLimitHB.setManaged(false);
                    weightsAndFormsHB.setVisible(false);
                    weightsAndFormsHB.setManaged(false);
                    multiSubjectToggle.setVisible(true);
                    multiSubjectToggle.setManaged(true);
                    summaryLimit = 0;

                    configStage.hide();
                    primaryStage.show();
                }
            });

        menuBar.getMenus().add(modeMenu);
        return menuBar;
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

    private void updateSummaryList() {
        summariesVB.getChildren().clear();
        int limit = (summaryLimit == 0)
            ? summaries.size() : Math.min(summaries.size(), summaryLimit);
        for (int i = 0; i < limit; ++i) {
            float quality = QualityAggregator
                .calculate(summaries.get(i).first, weights);
            TextField summaryL = new TextField(
                summaries.get(i).second + " ["
                    + String.format("%.2f", quality)
                    + "]"
            );
            summaryL.setFont(normalFont);
            summaryL.setEditable(false);
            summaryL.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            summariesVB.getChildren().add(summaryL);

            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < summaries.get(j).first.size(); ++j) {
                if (j != 0) sb.append(", ");
                sb.append(
                    "T" + (j + 1) + " = "
                        + String.format("%.2f", summaries.get(j).first.get(j))
                );
            }

            Tooltip summaryTT = new Tooltip(sb.toString());
            Tooltip.install(summaryL, summaryTT);
        }
    }
}
