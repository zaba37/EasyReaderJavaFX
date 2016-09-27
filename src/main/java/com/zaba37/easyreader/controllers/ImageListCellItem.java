/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zaba37.easyreader.controllers;

import com.zaba37.easyreader.models.EasyReaderItem;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;

/**
 *
 * @author Krystian
 */
public class ImageListCellItem extends ListCell<EasyReaderItem>{
    
    @Override
    public void updateItem(EasyReaderItem item, boolean empty)
    {
        super.updateItem(item, empty);
        
        if(item != null)
        {
            ImageListCellController data = new ImageListCellController();
            data.setImage(item.getImage());
            data.setImageName(item.getName());
            setGraphic(data.getVBox());
        }
    }
}
