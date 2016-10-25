package com.zaba37.easyreader.controllers;

import com.zaba37.easyreader.models.EasyReaderItem;
import javafx.scene.control.ListCell;

/**
 * Created by zaba37 on 23.10.2016.
 */
public class PageOCRChooserCellItem extends ListCell<EasyReaderItem> {

    @Override
    public void updateItem(EasyReaderItem item, boolean empty)
    {
        super.updateItem(item, empty);

        if(item != null)
        {
            ImageListCellController data = new ImageListCellController(ImageListCellController.ListType.OCR_PAGE_LIST);
            data.setImage(item.getImage());
            data.setImageName(item.getName());
            setGraphic(data.getVBox());
        }
    }

}
