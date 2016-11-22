/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zaba37.easyreader.controllers;

import com.zaba37.easyreader.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import net.sourceforge.tess4j.ITessAPI;

/**
 * FXML Controller class
 *
 * @author Krystian
 */
public class OCRSettingsWndowController implements Initializable {

    @FXML private ListView optionListView;
    @FXML private GridPane languageGridPane;
    @FXML private GridPane ocrEngineModeGridPane;
    @FXML private GridPane ocrPageSegmentationModeGridPane;
    @FXML private GridPane languageModelPenalityGridPane;
    @FXML private GridPane otherGridPane;
    @FXML private TextField languageDataPathTextField;
    @FXML private Label checkLanguageDataField;
    @FXML private ComboBox languageSelectorComboBox;
    @FXML private Button applyOcrSettingsButton;
    @FXML private RadioButton cubeCombinedRB;
    @FXML private RadioButton cubeOnlyRB;
    @FXML private RadioButton tesseractOnlyRB;

    @FXML private RadioButton PSM_OSD_ONLY_RB;
    @FXML private RadioButton PSM_AUTO_OSD_RB;
    @FXML private RadioButton PSM_AUTO_ONLY_RB;
    @FXML private RadioButton PSM_AUTO_RB;
    @FXML private RadioButton PSM_SINGLE_COLUMN_RB;
    @FXML private RadioButton PSM_SINGLE_BLOCK_VERT_TEXT_RB;
    @FXML private RadioButton PSM_SINGLE_BLOCK_RB;
    @FXML private RadioButton PSM_SINGLE_LINE_RB;
    @FXML private RadioButton PSM_SINGLE_WORD_RB;
    @FXML private RadioButton PSM_CIRCLE_WORD_RB;
    @FXML private RadioButton PSM_SINGLE_CHAR_RB;
    @FXML private RadioButton PSM_SPARSE_TEXT_RB;
    @FXML private RadioButton PSM_SPARSE_TEXT_OSD_RB;

    @FXML private Slider nonDictionaryWordsSlider;
    @FXML private Slider punctuationSlider;
    @FXML private Slider caseSlider;
    @FXML private Slider characterTypeSlider;
    @FXML private Slider fontSlider;
    @FXML private Slider spacingSlider;
    @FXML private Slider ngramSlider;
    @FXML private TextField nonDictionaryWordsTB;
    @FXML private TextField punctuationTB;
    @FXML private TextField caseTB;
    @FXML private TextField characterTypeTB;
    @FXML private TextField fontTB;
    @FXML private TextField spacingTB;
    @FXML private TextField ngramTB;
    @FXML private Button initialValuesButton;

    @FXML private CheckBox fontDetectionCB;
    @FXML private CheckBox resultAsStringCB;
    @FXML private CheckBox useBinWhenLoadingImageCB;

    private ArrayList<String> optionList;
    private ArrayList<String> languageList;
    private Preferences preferences;
    private DirectoryChooser directoryChooser;
    private ToggleGroup ocrEngineModeGroup;
    private ToggleGroup ocrPageSegmentationModeGroup;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        optionList = new ArrayList();
        languageList = new ArrayList();
        preferences = Preferences.userRoot().node(Utils.KEY_PREFERENCES);

        ocrEngineModeGroup = new ToggleGroup();
        cubeCombinedRB.setToggleGroup(ocrEngineModeGroup);
        cubeOnlyRB.setToggleGroup(ocrEngineModeGroup);
        tesseractOnlyRB.setToggleGroup(ocrEngineModeGroup);
        cubeCombinedRB.setUserData(ITessAPI.TessOcrEngineMode.OEM_TESSERACT_CUBE_COMBINED);
        cubeOnlyRB.setUserData(ITessAPI.TessOcrEngineMode.OEM_CUBE_ONLY);
        tesseractOnlyRB.setUserData(ITessAPI.TessOcrEngineMode.OEM_TESSERACT_ONLY);

        ocrPageSegmentationModeGroup = new ToggleGroup();
        PSM_OSD_ONLY_RB.setToggleGroup(ocrPageSegmentationModeGroup);
        PSM_AUTO_OSD_RB.setToggleGroup(ocrPageSegmentationModeGroup);
        PSM_AUTO_ONLY_RB.setToggleGroup(ocrPageSegmentationModeGroup);
        PSM_AUTO_RB.setToggleGroup(ocrPageSegmentationModeGroup);
        PSM_SINGLE_COLUMN_RB.setToggleGroup(ocrPageSegmentationModeGroup);
        PSM_SINGLE_BLOCK_VERT_TEXT_RB.setToggleGroup(ocrPageSegmentationModeGroup);
        PSM_SINGLE_BLOCK_RB.setToggleGroup(ocrPageSegmentationModeGroup);
        PSM_SINGLE_LINE_RB.setToggleGroup(ocrPageSegmentationModeGroup);
        PSM_SINGLE_WORD_RB.setToggleGroup(ocrPageSegmentationModeGroup);
        PSM_CIRCLE_WORD_RB.setToggleGroup(ocrPageSegmentationModeGroup);
        PSM_SINGLE_CHAR_RB.setToggleGroup(ocrPageSegmentationModeGroup);
        PSM_SPARSE_TEXT_RB.setToggleGroup(ocrPageSegmentationModeGroup);
        PSM_SPARSE_TEXT_OSD_RB.setToggleGroup(ocrPageSegmentationModeGroup);
        PSM_OSD_ONLY_RB.setUserData(ITessAPI.TessPageSegMode.PSM_OSD_ONLY);
        PSM_AUTO_OSD_RB.setUserData(ITessAPI.TessPageSegMode.PSM_AUTO_OSD);
        PSM_AUTO_ONLY_RB.setUserData(ITessAPI.TessPageSegMode.PSM_AUTO_ONLY);
        PSM_AUTO_RB.setUserData(ITessAPI.TessPageSegMode.PSM_AUTO);
        PSM_SINGLE_COLUMN_RB.setUserData(ITessAPI.TessPageSegMode.PSM_SINGLE_COLUMN);
        PSM_SINGLE_BLOCK_VERT_TEXT_RB.setUserData(ITessAPI.TessPageSegMode.PSM_SINGLE_BLOCK_VERT_TEXT);
        PSM_SINGLE_BLOCK_RB.setUserData(ITessAPI.TessPageSegMode.PSM_SINGLE_BLOCK);
        PSM_SINGLE_LINE_RB.setUserData(ITessAPI.TessPageSegMode.PSM_SINGLE_LINE);
        PSM_SINGLE_WORD_RB.setUserData(ITessAPI.TessPageSegMode.PSM_SINGLE_WORD);
        PSM_CIRCLE_WORD_RB.setUserData(ITessAPI.TessPageSegMode.PSM_CIRCLE_WORD);
        PSM_SINGLE_CHAR_RB.setUserData(ITessAPI.TessPageSegMode.PSM_SINGLE_CHAR);
        PSM_SPARSE_TEXT_RB.setUserData(ITessAPI.TessPageSegMode.PSM_SPARSE_TEXT);
        PSM_SPARSE_TEXT_OSD_RB.setUserData(ITessAPI.TessPageSegMode.PSM_SPARSE_TEXT_OSD);

        optionList.add("Language");
        optionList.add("OCR Engine Mode");
        optionList.add("OCR Page Segmentation");
        optionList.add("Language Model Penality");
        optionList.add("Other");

        ObservableList<String> data = FXCollections.observableArrayList(optionList);

        optionListView.setItems(data);
        optionListView.getSelectionModel().select(0);
        languageGridPane.setVisible(true);

        checkSaveStateApplication();

        languageDataPathTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                checkFilePath(t1);
            }
        });

        ocrEngineModeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov,
                                Toggle old_toggle, Toggle new_toggle) {
                if (ocrEngineModeGroup.getSelectedToggle() != null) {
                    System.out.println(ocrEngineModeGroup.getSelectedToggle().getUserData().toString());
                }
            }
        });

        languageSelectorComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            setOcrEngineModesRadioButtons();
        });

        setupLanguageModelPenalityControls();
    }

    @FXML
    private void handleListMouseClick(MouseEvent arg) {
        int selectedIndex = optionListView.getSelectionModel().getSelectedIndex();

        languageGridPane.setVisible(false);
        ocrEngineModeGridPane.setVisible(false);
        ocrPageSegmentationModeGridPane.setVisible(false);
        languageModelPenalityGridPane.setVisible(false);
        otherGridPane.setVisible(false);

        setOcrEngineModesRadioButtons();
        setOcrPageSegmentationModeRadioButtons();
        setOtherCheckBoxes();

        switch (selectedIndex) {
            case 0:
                languageGridPane.setVisible(true);
                break;
            case 1:
                ocrEngineModeGridPane.setVisible(true);
                break;
            case 2:
                ocrPageSegmentationModeGridPane.setVisible(true);
                break;
            case 3:
                languageModelPenalityGridPane.setVisible(true);
                break;
            case 4:
                otherGridPane.setVisible(true);
                break;
        }
    }

    private void checkSaveStateApplication() {
        languageDataPathTextField.setText(preferences.get(Utils.KEY_LANGUAGE_DATA_PATH, ""));

        //CHECK DATA PATH
        if (!checkLanguageDataFilePath()) {
            setLanguageList();
        } else {
            checkFilePath(preferences.get(Utils.KEY_LANGUAGE_DATA_PATH, ""));
            ObservableList<String> data = FXCollections.observableArrayList(languageList);
            languageSelectorComboBox.setItems(data);
            languageSelectorComboBox.setValue(preferences.get(Utils.KEY_SELECTED_OCR_LANGUAGE_NAME, ""));
        }

        //CHECK OCR ENGINE MODE
        if (preferences.getInt(Utils.KEY_OCR_ENGINE_MODE, -1) == -1) {
            preferences.putInt(Utils.KEY_OCR_ENGINE_MODE, ITessAPI.TessOcrEngineMode.OEM_TESSERACT_ONLY);
        }
    }

    private boolean checkLanguageDataFilePath() {
        String path = preferences.get(Utils.KEY_LANGUAGE_DATA_PATH, "");

        if (path.isEmpty()) {
            checkLanguageDataField.setText("LANGUAGE DATA NOT FOUND");
            checkLanguageDataField.setVisible(true);

            return false;
        }

        checkLanguageDataField.setVisible(false);
        languageDataPathTextField.setText(path);

        return true;
    }

    private void setLanguageList() {
        languageList = Utils.createLanguageList(languageList);
        ObservableList<String> data = FXCollections.observableArrayList(languageList);
        languageSelectorComboBox.setItems(data);
    }

    @FXML
    private void handleImageEditorActions(ActionEvent event) {
        directoryChooser = new DirectoryChooser();

        directoryChooser.setTitle("Chose directory");
        directoryChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );

        File file = directoryChooser.showDialog(((Button) event.getSource()).getScene().getWindow());

        languageDataPathTextField.setText(file.getPath());
    }

    @FXML
    private void handleApplyButtonClick(ActionEvent event) {
        if (!checkLanguageDataField.isVisible()) {
            preferences.put(Utils.KEY_LANGUAGE_DATA_PATH, languageDataPathTextField.getText());
            preferences.put(Utils.KEY_SELECTED_OCR_LANGUAGE_KEY_NAME, Utils.getLanguageKeyName((String) languageSelectorComboBox.getValue()));
            preferences.put(Utils.KEY_SELECTED_OCR_LANGUAGE_NAME, Utils.getLanguageName(preferences.get(Utils.KEY_SELECTED_OCR_LANGUAGE_KEY_NAME, "")));
            preferences.putInt(Utils.KEY_OCR_ENGINE_MODE, Integer.valueOf(ocrEngineModeGroup.getSelectedToggle().getUserData().toString()));
            preferences.putInt(Utils.KEY_OCR_PAGE_SEGMENTATION_MODE, Integer.valueOf(ocrPageSegmentationModeGroup.getSelectedToggle().getUserData().toString()));

            preferences.putDouble(Utils.KEY_NON_DICT_WORDS, Double.valueOf(nonDictionaryWordsTB.getText()));
            preferences.putDouble(Utils.KEY_PUNCTUATION, Double.valueOf(punctuationTB.getText()));
            preferences.putDouble(Utils.KEY_CASE, Double.valueOf(caseTB.getText()));
            preferences.putDouble(Utils.KEY_CHARACTER_TYPE, Double.valueOf(characterTypeTB.getText()));
            preferences.putDouble(Utils.KEY_FONT, Double.valueOf(fontTB.getText()));
            preferences.putDouble(Utils.KEY_SPACING, Double.valueOf(spacingTB.getText()));
            preferences.putDouble(Utils.KEY_NGRAM, Double.valueOf(ngramTB.getText()));

            preferences.putBoolean(Utils.KEY_FONT_DETECTION, fontDetectionCB.isSelected());
            preferences.putBoolean(Utils.KEY_OCR_RESULT_WITHOUT_FORMATING, resultAsStringCB.isSelected());
            preferences.putBoolean(Utils.KEY_USE_BINARIZATION_WHEN_LOADING, useBinWhenLoadingImageCB.isSelected());

            ((Stage) applyOcrSettingsButton.getScene().getWindow()).close();
        }
    }

    @FXML
    private void actionSetToInitialModelPenality(){
        nonDictionaryWordsTB.setText("0.15");
        punctuationTB.setText("0.2");
        caseTB.setText("0.1");
        characterTypeTB.setText("0.3");
        fontTB.setText("0");
        spacingTB.setText("0.05");
        ngramTB.setText("0.03");
    }

    @FXML
    private void handleCancelButtonClick(ActionEvent event) {
        ((Stage) applyOcrSettingsButton.getScene().getWindow()).close();
    }

    private void checkFilePath(String path) {
        File file = new File(path);

        if (file.exists() && file.isDirectory()) {
            File[] fileList = file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".traineddata");
                }
            });

            System.out.println(fileList.length);

            if (fileList.length == 0) {
                if (!checkLanguageDataField.isVisible()) {
                    checkLanguageDataField.setVisible(true);
                }

                checkLanguageDataField.setText("LANGUAGAE DATA NOT FOUND IN CHOOSEN FILE");
            } else {
                checkLanguageDataField.setVisible(false);

                for (File f : fileList) {
                    System.out.println(f.getName());
                    languageList.add(f.getName().substring(0, f.getName().lastIndexOf('.')));
                }

                setLanguageList();
            }

        } else {
            if (!checkLanguageDataField.isVisible()) {
                checkLanguageDataField.setVisible(true);
            }

            checkLanguageDataField.setText("DIRECTORY DONT EXIST");
        }
    }

    private void setOcrEngineModesRadioButtons() {
        if (preferences.get(Utils.KEY_SELECTED_OCR_LANGUAGE_KEY_NAME, "").isEmpty()) {
            cubeCombinedRB.setDisable(true);
            cubeOnlyRB.setDisable(true);
            tesseractOnlyRB.setDisable(true);
        } else {
            tesseractOnlyRB.setDisable(false);

            checkCubeDataForChoosenLanguage();

            if(preferences.getInt(Utils.KEY_OCR_ENGINE_MODE, -1) == -1){
                tesseractOnlyRB.setSelected(true);
            }else{
                if(preferences.getInt(Utils.KEY_OCR_ENGINE_MODE, -1) == ITessAPI.TessOcrEngineMode.OEM_TESSERACT_ONLY){
                    tesseractOnlyRB.setSelected(true);
                }else if(preferences.getInt(Utils.KEY_OCR_ENGINE_MODE, -1) == ITessAPI.TessOcrEngineMode.OEM_TESSERACT_CUBE_COMBINED){
                    if(!cubeCombinedRB.isDisable()){
                        cubeCombinedRB.setSelected(true);
                    }else{
                        tesseractOnlyRB.setSelected(true);
                    }
                }else  if(preferences.getInt(Utils.KEY_OCR_ENGINE_MODE, -1) == ITessAPI.TessOcrEngineMode.OEM_CUBE_ONLY){
                    if(!cubeOnlyRB.isDisable()){
                        cubeOnlyRB.setSelected(true);
                    }else {
                        tesseractOnlyRB.setSelected(true);
                    }
                }
            }
        }
    }

    private void setOcrPageSegmentationModeRadioButtons(){
        int selectedValue = preferences.getInt(Utils.KEY_OCR_PAGE_SEGMENTATION_MODE, -1);

        switch (selectedValue){
            case -1:
                PSM_AUTO_OSD_RB.setSelected(true);
                break;
            case 0:
                PSM_OSD_ONLY_RB.setSelected(true);
                break;
            case 1:
                PSM_AUTO_OSD_RB.setSelected(true);
                break;
            case 2:
                PSM_AUTO_ONLY_RB.setSelected(true);
                break;
            case 3:
                PSM_AUTO_RB.setSelected(true);
                break;
            case 4:
                PSM_SINGLE_COLUMN_RB.setSelected(true);
                break;
            case 5:
                PSM_SINGLE_BLOCK_VERT_TEXT_RB.setSelected(true);
                break;
            case 6:
                PSM_SINGLE_BLOCK_RB.setSelected(true);
                break;
            case 7:
                PSM_SINGLE_LINE_RB.setSelected(true);
                break;
            case 8:
                PSM_SINGLE_WORD_RB.setSelected(true);
                break;
            case 9:
                PSM_CIRCLE_WORD_RB.setSelected(true);
                break;
            case 10:
                PSM_SINGLE_CHAR_RB.setSelected(true);
                break;
            case 11:
                PSM_SPARSE_TEXT_RB.setSelected(true);
                break;
            case 12:
                PSM_SPARSE_TEXT_OSD_RB.setSelected(true);
                break;
        }
    }

    private void checkCubeDataForChoosenLanguage() {
        File file = new File(preferences.get(Utils.KEY_LANGUAGE_DATA_PATH, ""));

        File[] fileList = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(Utils.getLanguageKeyName((String) languageSelectorComboBox.getValue()) + ".cube.fold");
            }
        });

        if(fileList.length == 0 ){
            cubeCombinedRB.setDisable(true);
            cubeOnlyRB.setDisable(true);
        }else{
            cubeCombinedRB.setDisable(false);
            cubeOnlyRB.setDisable(false);
        }
    }

    private void setupLanguageModelPenalityControls(){
        Pattern validDoubleText = Pattern.compile("-?((\\d*)|(\\d+\\.\\d*))");

        TextFormatter<Double> nonDictionaryWordsTBtextFormatter = new TextFormatter<Double>(new DoubleStringConverter(), 0.0,
                change -> {
                    String newText = change.getControlNewText() ;
                    if (validDoubleText.matcher(newText).matches()) {
                        return change ;
                    } else return null ;
                });

        TextFormatter<Double> punctuationTBtextFormatter = new TextFormatter<Double>(new DoubleStringConverter(), 0.0,
                change -> {
                    String newText = change.getControlNewText() ;
                    if (validDoubleText.matcher(newText).matches()) {
                        return change ;
                    } else return null ;
                });

        TextFormatter<Double> caseTBtextFormatter = new TextFormatter<Double>(new DoubleStringConverter(), 0.0,
                change -> {
                    String newText = change.getControlNewText() ;
                    if (validDoubleText.matcher(newText).matches()) {
                        return change ;
                    } else return null ;
                });

        TextFormatter<Double> characterTypeTBtextFormatter = new TextFormatter<Double>(new DoubleStringConverter(), 0.0,
                change -> {
                    String newText = change.getControlNewText() ;
                    if (validDoubleText.matcher(newText).matches()) {
                        return change ;
                    } else return null ;
                });

        TextFormatter<Double> fontTBtextFormatter = new TextFormatter<Double>(new DoubleStringConverter(), 0.0,
                change -> {
                    String newText = change.getControlNewText() ;
                    if (validDoubleText.matcher(newText).matches()) {
                        return change ;
                    } else return null ;
                });

        TextFormatter<Double> spacingTBtextFormatter = new TextFormatter<Double>(new DoubleStringConverter(), 0.0,
                change -> {
                    String newText = change.getControlNewText() ;
                    if (validDoubleText.matcher(newText).matches()) {
                        return change ;
                    } else return null ;
                });

        TextFormatter<Double> ngramTBtextFormatter = new TextFormatter<Double>(new DoubleStringConverter(), 0.0,
                change -> {
                    String newText = change.getControlNewText() ;
                    if (validDoubleText.matcher(newText).matches()) {
                        return change ;
                    } else return null ;
                });

        nonDictionaryWordsTB.setTextFormatter(nonDictionaryWordsTBtextFormatter);
        punctuationTB.setTextFormatter(punctuationTBtextFormatter);
        caseTB.setTextFormatter(caseTBtextFormatter);
        characterTypeTB.setTextFormatter(characterTypeTBtextFormatter);
        fontTB.setTextFormatter(fontTBtextFormatter);
        spacingTB.setTextFormatter(spacingTBtextFormatter);
        ngramTB.setTextFormatter(ngramTBtextFormatter);


        nonDictionaryWordsSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            nonDictionaryWordsTB.setText(String.valueOf(Math.round(newValue.floatValue())/ 100.0));
        });

        nonDictionaryWordsTB.textProperty().addListener((observable, oldValue, newValue) -> {
                double value = Double.parseDouble(newValue);

                if(value > 1){
                    nonDictionaryWordsTB.setText(oldValue);
                    nonDictionaryWordsSlider.setValue(Double.parseDouble(oldValue));
                }else{
                    nonDictionaryWordsSlider.setValue(value * 100);
                }
        });

        punctuationSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            punctuationTB.setText(String.valueOf(Math.round(newValue.floatValue())/ 100.0));
        });

        punctuationTB.textProperty().addListener((observable, oldValue, newValue) -> {
            double value = Double.parseDouble(newValue);

            if(value > 1){
                punctuationTB.setText(oldValue);
                punctuationSlider.setValue(Double.parseDouble(oldValue));
            }else{
                punctuationSlider.setValue(value * 100);
            }
        });

        caseSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            caseTB.setText(String.valueOf(Math.round(newValue.floatValue())/ 100.0));
        });

        caseTB.textProperty().addListener((observable, oldValue, newValue) -> {
            double value = Double.parseDouble(newValue);

            if(value > 1){
                caseTB.setText(oldValue);
                caseSlider.setValue(Double.parseDouble(oldValue));
            }else{
                caseSlider.setValue(value * 100);
            }
        });

        characterTypeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            characterTypeTB.setText(String.valueOf(Math.round(newValue.floatValue())/ 100.0));
        });

        characterTypeTB.textProperty().addListener((observable, oldValue, newValue) -> {
            double value = Double.parseDouble(newValue);

            if(value > 1){
                characterTypeTB.setText(oldValue);
                characterTypeSlider.setValue(Double.parseDouble(oldValue));
            }else{
                characterTypeSlider.setValue(value * 100);
            }
        });

        fontSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            fontTB.setText(String.valueOf(Math.round(newValue.floatValue())/ 100.0));
        });

        fontTB.textProperty().addListener((observable, oldValue, newValue) -> {
            double value = Double.parseDouble(newValue);

            if(value > 1){
                fontTB.setText(oldValue);
                fontSlider.setValue(Double.parseDouble(oldValue));
            }else{
                fontSlider.setValue(value * 100);
            }
        });

        spacingSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            spacingTB.setText(String.valueOf(Math.round(newValue.floatValue())/ 100.0));
        });

        spacingTB.textProperty().addListener((observable, oldValue, newValue) -> {
            double value = Double.parseDouble(newValue);

            if(value > 1){
                spacingTB.setText(oldValue);
                spacingSlider.setValue(Double.parseDouble(oldValue));
            }else{
                spacingSlider.setValue(value * 100);
            }
        });

        ngramSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            ngramTB.setText(String.valueOf(Math.round(newValue.floatValue())/ 100.0));
        });

        ngramTB.textProperty().addListener((observable, oldValue, newValue) -> {
            double value = Double.parseDouble(newValue);

            if(value > 1){
                ngramTB.setText(oldValue);
                ngramSlider.setValue(Double.parseDouble(oldValue));
            }else{
                ngramSlider.setValue(value * 100);
            }
        });

        nonDictionaryWordsTB.setText(String.valueOf(preferences.getDouble(Utils.KEY_NON_DICT_WORDS, -1)));
        punctuationTB.setText(String.valueOf(preferences.getDouble(Utils.KEY_PUNCTUATION, -1)));
        caseTB.setText(String.valueOf(preferences.getDouble(Utils.KEY_CASE, -1)));
        characterTypeTB.setText(String.valueOf(preferences.getDouble(Utils.KEY_CHARACTER_TYPE, -1)));
        fontTB.setText(String.valueOf(preferences.getDouble(Utils.KEY_FONT, -1)));
        spacingTB.setText(String.valueOf(preferences.getDouble(Utils.KEY_SPACING, -1)));
        ngramTB.setText(String.valueOf(preferences.getDouble(Utils.KEY_NGRAM, -1)));
    }

    private void setOtherCheckBoxes(){
        fontDetectionCB.setSelected(preferences.getBoolean(Utils.KEY_FONT_DETECTION, false));
        resultAsStringCB.setSelected(preferences.getBoolean(Utils.KEY_OCR_RESULT_WITHOUT_FORMATING, false));
        useBinWhenLoadingImageCB.setSelected(preferences.getBoolean(Utils.KEY_USE_BINARIZATION_WHEN_LOADING, false));
    }
}
