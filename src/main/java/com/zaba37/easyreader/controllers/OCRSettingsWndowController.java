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
    @FXML private TextField languageDataPathTextField;
    @FXML private Button languageDataPathChooserButton;
    @FXML private Label checkLanguageDataField;
    @FXML private ComboBox languageSelectorComboBox;
    @FXML private Label checkSelectedLanguageField;
    @FXML private Button applyOcrSettingsButton;
    @FXML private Button cancelOcrSettingsButton;
    @FXML private RadioButton cubeCombinedRB;
    @FXML private RadioButton cubeOnlyRB;
    @FXML private RadioButton tesseractOnlyRB;

    private ArrayList<String> optionList;
    private ArrayList<String> languageList;
    private Preferences preferences;
    private DirectoryChooser directoryChooser;
    private ToggleGroup ocrEngineModeGroup;

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

        optionList.add("Language");
        optionList.add("OCR Engine Mode");

        ObservableList<String> data = FXCollections.observableArrayList(optionList);

        optionListView.setItems(data);

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
    }

    @FXML
    private void handleListMouseClick(MouseEvent arg) {
        int selectedIndex = optionListView.getSelectionModel().getSelectedIndex();

        languageGridPane.setVisible(false);
        ocrEngineModeGridPane.setVisible(false);

        switch (selectedIndex) {
            case 0:
                languageGridPane.setVisible(true);
                break;
            case 1:
                ocrEngineModeGridPane.setVisible(true);
                setOcrEngineModesRadioButtons();
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
            ((Stage) applyOcrSettingsButton.getScene().getWindow()).close();
        }
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
}
