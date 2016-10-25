package com.zaba37.easyreader.controllers;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.asyncTasks.ImageBackgroundLoader;
import com.zaba37.easyreader.models.EasyReaderItem;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import org.ghost4j.document.DocumentException;
import org.ghost4j.document.PDFDocument;

/**
 * FXML Controller class
 *
 * @author Krystian
 */
public class LoadingWindowSceneController implements Initializable {

    @FXML
    private Button cancelButton;

    @FXML
    private ProgressIndicator progressIndicatior;

    private double value;
    private ArrayList<EasyReaderItem> loadedItemList;
    private ImageBackgroundLoader loader;
    private List<File> filesList;
    private int filesNumber;
    private boolean cancelPocess;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void startLoading(List<File> filesList) {
        this.filesList = filesList;
        this.cancelPocess = false;
        
        try {
            loader = new ImageBackgroundLoader(this);
        } catch (IOException ex) {
            Logger.getLogger(LoadingWindowSceneController.class.getName()).log(Level.SEVERE, null, ex);
        }

        initProgressIndycator(filesList);

        loader.execute();

    }

    public void initProgressIndycator(List<File> files) {
        filesNumber = 0;

        for (File file : files) {
            if (isPDFFile(file)) {
                PDFDocument document = new PDFDocument();

                try {
                    document.load(file);
                    filesNumber += document.getPageCount();
                } catch (IOException ex) {
                    Logger.getLogger(LoadingWindowSceneController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (DocumentException ex) {
                    Logger.getLogger(LoadingWindowSceneController.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                filesNumber++;
            }
        }

        value = ((double) 100 / filesNumber)/100;
    }

    public void updateProgressIndycator() {
        progressIndicatior.setProgress(progressIndicatior.getProgress() + value);
    }

    public List<File> getFilesList() {
        return filesList;
    }
    
    public int getFilesNumber(){
        return this.filesNumber;
    }

    private boolean isPDFFile(File file) {
        String extension = "";
        String fileName = file.getName();

        int i = fileName.lastIndexOf('.');

        if (i > 0) {
            extension = fileName.substring(i + 1);
        }

        if ("pdf".equals(extension)) {
            return true;
        } else {
            return false;
        }
    }

    @FXML
    private void handleCancelcPressed() {
        loader.setLoading(false);
        this.cancelPocess = true;
        Utils.getMainWindowController().initImageListView(new ArrayList());
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    public void closeLoadingWindow() {
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    public boolean isCancel() {
        return this.cancelPocess;
    }

}
