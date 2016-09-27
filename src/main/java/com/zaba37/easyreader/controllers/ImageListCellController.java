/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zaba37.easyreader.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Krystian
 */
public class ImageListCellController {

    @FXML
    private ImageView imageCell;
    
    @FXML 
    private Label labelCell;
    
    @FXML 
    private VBox vBox;
    
    public ImageListCellController() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ImageListCellItem.fxml"));
        fxmlLoader.setController(this);
        
        try
        {
            fxmlLoader.load();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        imageCell.preserveRatioProperty().set(true);
    }
    
    public VBox getVBox(){
        return vBox;
    }
    
    public void setImage(Image image){
        imageCell.setImage(image);
    }
    
    public void setImageName(String name){
        labelCell.setText(name);
    }
    
}
