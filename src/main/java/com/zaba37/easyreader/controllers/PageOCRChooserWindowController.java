package com.zaba37.easyreader.controllers;

import com.zaba37.easyreader.models.EasyReaderItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by zaba37 on 23.10.2016.
 */
public class PageOCRChooserWindowController implements Initializable {

    @FXML
    private ListView listView;

    private ArrayList<EasyReaderItem> loadedItemList;
    private final ObservableList observableList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setLoadedItemList(ArrayList<EasyReaderItem> items){
        this.loadedItemList = items;
        initListView();
    }

    private void initListView(){
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

}
