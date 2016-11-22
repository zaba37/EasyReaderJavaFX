package com.zaba37.easyreader.controllers;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.models.EasyReaderItem;
import com.zaba37.easyreader.textEditor.ParStyle;
import com.zaba37.easyreader.textEditor.TextStyle;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.Polish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * Created by zaba37 on 09.11.2016.
 */
public class SpellCheckerController implements Initializable {

    @FXML private Button ignore;
    @FXML private Button ignoreAll;
    @FXML private Button change;
    @FXML private Button changeAll;
    @FXML private Button accept;
    @FXML private Button cancel;
    @FXML private TextField misspelled;
    @FXML private TextField changeTo;
    @FXML private ListView<String> suggestions;
    @FXML private ListView<String> missWords;
    @FXML private HBox hbox;
    @FXML private AnchorPane anchorPane;
    @FXML private Label languageNameLabel;


    private String replacementText; // the replacement text that will be

    private List<String> misspells; // list of misspelled words
    private ObservableList<String> suggestionsList; // list for suggestions
    private ObservableList<String> missWordsList;

    private List<RuleMatch> matches;      // list of misspellings
    private int currentIndex;       // index for current position in misspells list

    private StyledTextArea<ParStyle, TextStyle> textArena;
    private VirtualizedScrollPane vsPane;
    private JLanguageTool langTool;
    private EasyReaderItem item;

    public void init(EasyReaderItem item)
    {
        this.item = item;

        suggestions.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            changeTo.setText(newValue);
        });

        missWords.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            StyleSpans<TextStyle> styles = textArena.getStyleSpans(matches.get(currentIndex).getFromPos(), matches.get(currentIndex).getToPos());
            StyleSpans<TextStyle> newStyles = styles.mapStyles(style -> style.updateWith(TextStyle.backgroundColor(Color.WHITE)));
            textArena.setStyleSpans(matches.get(currentIndex).getFromPos(), newStyles);

            nextMisspelling(missWords.getSelectionModel().getSelectedIndex());

            styles = textArena.getStyleSpans(matches.get(missWords.getSelectionModel().getSelectedIndex()).getFromPos(), matches.get(missWords.getSelectionModel().getSelectedIndex()).getToPos());
            newStyles = styles.mapStyles(style -> style.updateWith(TextStyle.backgroundColor(Color.YELLOW)));
            textArena.setStyleSpans(matches.get(missWords.getSelectionModel().getSelectedIndex()).getFromPos(), newStyles);

            textArena.positionCaret(matches.get(missWords.getSelectionModel().getSelectedIndex()).getToPos());
        });

        textArena = new StyledTextArea<>(
                ParStyle.EMPTY, (paragraph, style) -> paragraph.setStyle(style.toCss()),
                TextStyle.EMPTY.updateFontSize(12).updateFontFamily("Serif").updateTextColor(Color.BLACK),
                (text, style) -> text.setStyle(style.toCss()));

        vsPane = new VirtualizedScrollPane<>(textArena);

        textArena.setEditable(false);

        vsPane.setMaxHeight(390);
        vsPane.setMinHeight(390);
        vsPane.setMaxWidth(390);
        vsPane.setMinWidth(390);
        vsPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        vsPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        hbox.getChildren().add(vsPane);

        vsPane.setVisible(false);
        vsPane.setMaxWidth(0);
        vsPane.setMinWidth(0);

        refreshLanguage();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {


    }

    @FXML
    private void setLanguageAction(){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/SpellCheckerLanguageController.fxml"));

        Parent root = null;

        try {
            root = (Parent) fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SpellCheckerLanguageController controller = fxmlLoader.<SpellCheckerLanguageController>getController();
        controller.setPrevouseController(this);

        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(Utils.getMainWindow());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void nextMisspelling(int index)
    {
        if(index >= misspells.size())
        {
            disableAndClearControls();
            return;
        }

        currentIndex = index;

        misspelled.setText(misspells.get(index));
        suggestionsList.clear();

        changeTo.setText("");

        if(matches.get(index).getSuggestedReplacements().size() > 0){
            suggestionsList.addAll(matches.get(index).getSuggestedReplacements());
            changeTo.setText(suggestionsList.get(0));
        }
    }

    private void disableAndClearControls()
    {
        misspelled.setText("");
        changeTo.setText("");
        suggestionsList.clear();
        ignore.setDisable(true);
        ignoreAll.setDisable(true);
        change.setDisable(true);
        changeAll.setDisable(true);
    }

    @FXML
    public void changeAction()
    {
        if(!changeTo.getText().isEmpty())
            changeMisspelledWord(changeTo.getText());
    }

    private void changeMisspelledWord(String replacement) {
        item.getTextArea().replaceText(matches.get(currentIndex).getFromPos(), matches.get(currentIndex).getToPos(), replacement);
        refreshLists();
    }

    @FXML
    public void changeOnAllPagesAction()
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        int replaceCounter = 0;

        for(EasyReaderItem item : Utils.getMainWindowController().getLoadedItemList()) {
            int index = item.getTextArea().getText().indexOf(misspelled.getText());

            while(index != -1){
                item.getTextArea().replaceText(index, index + misspelled.getText().length(), changeTo.getText());
                replaceCounter++;
                index = item.getTextArea().getText().indexOf(misspelled.getText());
            }
        }

        alert.setTitle("Spell Checker");
        alert.setHeaderText("Replaced the word " + misspelled.getText() + " for word " + changeTo.getText() + " " + replaceCounter + " times.");
       // alert.setContentText("Replaced the word " + misspelled.getText() + " for word " + changeTo.getText() + " " + replaceCounter + " times.");
        alert.showAndWait();

        refreshLists();
    }

    @FXML
    public void ignoreAction()
    {
        currentIndex += 1;
        nextMisspelling(currentIndex);
    }

    @FXML
    public void ignoreAllAction()
    {
        disableAndClearControls();
    }

    @FXML
    public void acceptAction(ActionEvent event)
    {
        closeWindow((Button)event.getSource());
    }

    private void closeWindow(Button button)
    {
        Stage stage = (Stage) button.getParent().getScene().getWindow();
        stage.close();
    }

    @FXML
    public void cancelAction(ActionEvent event)
    {
        closeWindow((Button)event.getSource());
    }

    @FXML
    public void ShowTextPreview(ActionEvent event){
        if(vsPane.isVisible()){
            anchorPane.setMaxWidth(600);
            anchorPane.setMinWidth(600);

            hbox.setMaxWidth(600);
            hbox.setMinWidth(600);

            cancel.getParent().getScene().getWindow().setWidth(610);

            vsPane.setVisible(false);
            vsPane.setMaxWidth(0);
            vsPane.setMinWidth(0);
        }else{
            anchorPane.setMaxWidth(800);
            anchorPane.setMinWidth(800);

            hbox.setMaxWidth(800);
            hbox.setMinWidth(800);

            cancel.getParent().getScene().getWindow().setWidth(800);

            vsPane.setVisible(true);
            vsPane.setMaxWidth(390);
            vsPane.setMinWidth(390);
        }
    }

    private void refreshLists(){
        currentIndex = -1;

        replacementText = item.getStyledDocument().getText();

        misspells = new ArrayList<>();
        matches = null;
        suggestionsList = FXCollections.observableArrayList();
        missWordsList = FXCollections.observableArrayList();

        suggestions.setItems(suggestionsList);
        missWords.setItems(missWordsList);

        try
        {
            matches = langTool.check(replacementText);
            matches.forEach(match -> {
                int start = match.getFromPos();
                int end = match.getToPos();

                if(replacementText.substring(start, end).trim().length() > 0)
                    misspells.add(replacementText.substring(start, end));
            });

            missWordsList.addAll(misspells);

            nextMisspelling(0);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        textArena.clear();
        textArena.append(item.getStyledDocument());
    }

    public void refreshLanguage(){

        for(Language language : Languages.get()){
            if(language.getName().compareTo(Preferences.userRoot().node(Utils.KEY_PREFERENCES).get(Utils.KEY_SPELL_CHECKER_LANGUAGE_NAME,"")) == 0){
                langTool = new JLanguageTool(language);

                for (Rule rule : langTool.getAllRules()) {
                    if (!rule.isDictionaryBasedSpellingRule()) {
                        langTool.disableRule(rule.getId());
                    }
                }

                languageNameLabel.setText("Language: " + language.getName());
            }
        }

        refreshLists();
    }
}
