package com.zaba37.easyreader.controllers;

import com.zaba37.easyreader.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Created by zaba3 on 19.11.2016.
 */
public class SpellCheckerLanguageController implements Initializable {

    @FXML private ComboBox languageComboBox;
    @FXML private Button okButton;
    @FXML private Button cacnelButton;

    private ArrayList<Language> languageList;
    private ArrayList<String> languageNamesList;
    private Preferences preferences;
    private SpellCheckerController spellCheckerController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        preferences = Preferences.userRoot().node(Utils.KEY_PREFERENCES);
        languageList = new ArrayList<>();
        languageNamesList = new ArrayList<>();

        languageList.addAll(Languages.get());

        for(Language langauge : languageList){
            languageNamesList.add(langauge.getName());
        }

        ObservableList<String> data = FXCollections.observableArrayList(languageNamesList);
        languageComboBox.setItems(data);
        languageComboBox.setValue(preferences.get(Utils.KEY_SPELL_CHECKER_LANGUAGE_NAME, ""));
    }

    public void setPrevouseController(SpellCheckerController spellCheckerController){
        this.spellCheckerController = spellCheckerController;
    }

    @FXML
    private void okAction(){
        preferences.put(Utils.KEY_SPELL_CHECKER_LANGUAGE_NAME, (String)languageComboBox.getValue());
        spellCheckerController.refreshLanguage();
        cancelAction();
    }

    @FXML
    private void cancelAction(){
        Stage stage = (Stage) okButton.getParent().getScene().getWindow();
        stage.close();
    }
}
