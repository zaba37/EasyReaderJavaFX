package com.zaba37.easyreader.controllers;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.models.EasyReaderItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by zaba3 on 14.11.2016.
 */
public class ReplaceWordController implements Initializable {

    @FXML private TextField replaceTextField;
    @FXML private TextField forTextField;
    @FXML private Button replaceOnCurrentPageButton;
    @FXML private Button replaceOnAllPagesButton;

    private boolean replaceTextFieldEmpty = true;
    private boolean forTextFieldEmpty = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        replaceTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(replaceTextField.getText().isEmpty()){
                replaceTextFieldEmpty = true;
            }else {
                replaceTextFieldEmpty = false;
            }

            activateButtons();
        });

        forTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(forTextField.getText().isEmpty()){
                forTextFieldEmpty = true;
            }else {
                forTextFieldEmpty = false;
            }

            activateButtons();
        });

        replaceOnCurrentPageButton.setDisable(true);
        replaceOnAllPagesButton.setDisable(true);
    }

    private void activateButtons(){
        if(!forTextFieldEmpty && !replaceTextFieldEmpty){
            replaceOnCurrentPageButton.setDisable(false);
            replaceOnAllPagesButton.setDisable(false);
        }else{
            replaceOnCurrentPageButton.setDisable(true);
            replaceOnAllPagesButton.setDisable(true);
        }
    }

    @FXML
    private void replaceOnCurrentPage(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        int replaceCounter = 0;

            int index = Utils.getMainWindowController().getCurrentEasyReaderItem().getTextArea().getText().indexOf(replaceTextField.getText());

            while(index != -1){
                Utils.getMainWindowController().getCurrentEasyReaderItem().getTextArea().replaceText(index, index + replaceTextField.getText().length(), forTextField.getText());
                replaceCounter++;
                index =  Utils.getMainWindowController().getCurrentEasyReaderItem().getTextArea().getText().indexOf(replaceTextField.getText());
            }


        alert.setTitle("Spell Checker");
        alert.setHeaderText("Replaced the word \"" + replaceTextField.getText() + "\" for word \"" + forTextField.getText() + "\" " + replaceCounter + " times.");
        alert.showAndWait();
    }

    @FXML
    private void replaceOnAllPages(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        int replaceCounter = 0;

        for(EasyReaderItem item : Utils.getMainWindowController().getLoadedItemList()) {
            int index = item.getTextArea().getText().indexOf(replaceTextField.getText());

            while(index != -1){
                item.getTextArea().replaceText(index, index + replaceTextField.getText().length(), forTextField.getText());
                replaceCounter++;
                index = item.getTextArea().getText().indexOf(replaceTextField.getText());
            }
        }

        alert.setTitle("Spell Checker");
        alert.setHeaderText("Replaced the word \"" + replaceTextField.getText() + "\" for word \"" + forTextField.getText() + "\" " + replaceCounter + " times.");
        alert.showAndWait();
    }
}
