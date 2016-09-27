/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zaba37.easyreader.actions.menuBar;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.controllers.LoadingWindowSceneController;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author zaba3
 */
public class OpenAction {

    private FileChooser fileChooser;

    public OpenAction() {
        fileChooser = new FileChooser();
        configureFileChooser();
    }

    private void configureFileChooser() {
        fileChooser.setTitle("Open image");

        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.bmp", "*.jpeg", "*.pdf", "*.png", "*.tiff", "*.jpg"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp"),
                new FileChooser.ExtensionFilter("JPEG", "*.jpeg"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("TIFF", "*.tiff"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg")
        );
    }

    public void ShowOpenFilesDialog() throws IOException {
        List<File> filesList = fileChooser.showOpenMultipleDialog(Utils.getMainStage());

        if (filesList != null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/LoadingWindowScene.fxml"));

            Parent root = (Parent) fxmlLoader.load();
            LoadingWindowSceneController controllerLoad = fxmlLoader.<LoadingWindowSceneController>getController();
            controllerLoad.startLoading(filesList);
            Scene scene = new Scene(root);
            Stage stahe = new Stage();
            stahe.initModality(Modality.WINDOW_MODAL);
            stahe.initOwner(Utils.getMainWindow());
            stahe.setScene(scene);
            stahe.setResizable(false);
            stahe.show();
        }
    }
}
