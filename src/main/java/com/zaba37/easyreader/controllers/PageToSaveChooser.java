package com.zaba37.easyreader.controllers;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.asyncTasks.OCRProgressController;
import com.zaba37.easyreader.models.EasyReaderItem;
import com.zaba37.easyreader.textEditor.SaveManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by zaba3 on 20.11.2016.
 */
public class PageToSaveChooser implements Initializable {

    public enum DocumentType {
        TXT,
        PDF,
        DOC,
        DOCX,
        HTML;
    }

    @FXML
    private ListView listView;

    @FXML
    private Button applyButton;

    @FXML
    private Button cancelButton;

    @FXML
    private CheckBox selectAllBox;

    private Robot keySimulate ;
    private boolean deselectFlag;
    private ArrayList<EasyReaderItem> loadedItemList;
    private ArrayList<EasyReaderItem> selectedEasyReaderItems;
    private final ObservableList observableList = FXCollections.observableArrayList();
    private DocumentType documentType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        deselectFlag = false;

        try {
            keySimulate = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        listView.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                keyPress();
                selectAllBox.selectedProperty().removeListener(checkBoxCheackListener);
            }else{
                keyRelese();
                selectAllBox.selectedProperty().addListener(checkBoxCheackListener);
            }
        });


        listView.setOnMouseClicked(event -> {
            ObservableList<String> selectedItems =  listView.getSelectionModel().getSelectedItems();
            deselectFlag = true;

            if(selectedItems.size() == loadedItemList.size()){
                selectAllBox.setSelected(true);
            }else{
                selectAllBox.setSelected(false);
            }
        });
    }

    public void setLoadedItemList(ArrayList<EasyReaderItem> items) {
        this.loadedItemList = items;
        initListView();
    }

    public void setDocumentType(DocumentType type){
        this.documentType = type;
    }

    private void initListView() {
        if (!loadedItemList.isEmpty()) {
            observableList.setAll(loadedItemList);
            listView.setItems(observableList);

            listView.setCellFactory(new Callback<ListView<EasyReaderItem>, ListCell<EasyReaderItem>>() {
                @Override
                public ListCell<EasyReaderItem> call(ListView<EasyReaderItem> listView) {
                    return new PageOCRChooserCellItem();
                }
            });
        }
    }

    @FXML
    private void handleApplyAction(){
        ArrayList<EasyReaderItem> selectedItemsList = new ArrayList<>();
        selectedItemsList.addAll(listView.getSelectionModel().getSelectedItems());

        switch (documentType){
            case TXT:
                SaveManager.getInstance().saveToTXT(selectedItemsList);
                break;
            case DOC:
                SaveManager.getInstance().saveToDOC(selectedItemsList);
                break;
            case DOCX:
                SaveManager.getInstance().saveToDOCX(selectedItemsList);
                break;
            case PDF:
                SaveManager.getInstance().saveToPDF(selectedItemsList);
                break;
            case HTML:
                SaveManager.getInstance().saveToHtml(selectedItemsList);
                break;
        }

        keyRelese();
        ((Stage) selectAllBox.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancelAction(){
        keyRelese();
        ((Stage) selectAllBox.getScene().getWindow()).close();
    }

    private ChangeListener checkBoxCheackListener =  new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if(newValue){
                listView.getSelectionModel().selectAll();
            }else{
                listView.getSelectionModel().select(-1);
            }
        }
    };

    private void keyPress(){
        switch (OSValidation()){
            case "win":
                keySimulate.keyPress(KeyEvent.VK_CONTROL);
                break;
            case "mac":
                keySimulate.keyPress(KeyEvent.VK_META);
                break;
            case "unix":
                keySimulate.keyPress(KeyEvent.VK_CONTROL);
                break;
        }
    }

    private void keyRelese(){
        switch (OSValidation()){
            case "win":
                keySimulate.keyRelease(KeyEvent.VK_CONTROL);
                break;
            case "mac":
                keySimulate.keyRelease(KeyEvent.VK_META);
                break;
            case "unix":
                keySimulate.keyRelease(KeyEvent.VK_CONTROL);
                break;
        }
    }

    private String OSValidation() {
        String OS = System.getProperty("os.name").toLowerCase();

        if (OS.indexOf("win") >= 0) {
            System.out.println("Windows");
            return "win";
        } else if (OS.indexOf("mac") >= 0) {
            System.out.println("MaxOS");
            return "mac";
        } else {
            System.out.println("Unix");
            return "unix";
        }
    }
}
