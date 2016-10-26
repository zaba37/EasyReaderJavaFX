package com.zaba37.easyreader.controllers;

import com.zaba37.easyreader.models.EasyReaderItem;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by zaba37 on 23.10.2016.
 */
public class PageOCRChooserWindowController implements Initializable {

    @FXML
    private ListView listView;

    @FXML
    private Button applyButton;

    @FXML
    private Button cancelButton;

    @FXML
    private CheckBox selectAllBox;

    Robot keySimulate ;
    private boolean deselectFlag;
    private ArrayList<EasyReaderItem> loadedItemList;
    private ArrayList<EasyReaderItem> selectedEasyReaderItems;
    private final ObservableList observableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        deselectFlag = false;

        try {
            keySimulate = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        selectAllBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue && !deselectFlag){
                listView.getSelectionModel().selectAll();
            }else{
                listView.getSelectionModel().select(-1);
            }

            deselectFlag = false;
        });

        listView.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue){
                keySimulate.keyPress(KeyEvent.VK_CONTROL);
            }else{
                keySimulate.keyRelease(KeyEvent.VK_CONTROL);
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
    }

    @FXML
    private void handleCancelAction(){
        ((Stage) selectAllBox.getScene().getWindow()).close();
    }

}
