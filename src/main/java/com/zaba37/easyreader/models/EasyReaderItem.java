/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zaba37.easyreader.models;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.textEditor.ParStyle;
import com.zaba37.easyreader.textEditor.TextStyle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.print.Paper;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.StyledTextArea;

import javax.swing.text.StyledDocument;

/**
 *
 * @author Krystian
 */
public class EasyReaderItem {

    private final File imageFile;
    private Image image;
    private final String name;
    private ArrayList<StyledTextArea<ParStyle, TextStyle>> pagesList;

    public EasyReaderItem(File file) {
        this.imageFile = file;
        image = new Image(file.toURI().toString());
        name = file.getName();
        pagesList = new ArrayList();
        addPage();
    }

    public void rotatedImage(double angle) {
        BufferedImage bufferedImage = new BufferedImage((int) image.getWidth(), (int) image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        SwingFXUtils.fromFXImage(image, bufferedImage);

        AffineTransform affineTransform = new AffineTransform();

        if (angle < 0) {
            affineTransform.rotate(Math.toRadians(angle), image.getWidth() / 2, image.getWidth() / 2);
        } else {
            affineTransform.rotate(Math.toRadians(angle), image.getHeight() / 2, image.getHeight() / 2);
        }

        AffineTransformOp op = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BICUBIC);

        BufferedImage newImage = new BufferedImage(bufferedImage.getHeight(), bufferedImage.getWidth(), bufferedImage.getType());

        op.filter(bufferedImage, newImage);

        WritableImage writableImage = new WritableImage(newImage.getWidth(), newImage.getHeight());

        SwingFXUtils.toFXImage(newImage, writableImage);

        this.image = writableImage;
    }

    public String getName() {
        return name;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public File getFile() {
        return imageFile;
    }

    public void addPage() {
                StyledTextArea<ParStyle, TextStyle> area = new StyledTextArea<>(
                ParStyle.EMPTY, (paragraph, style) -> paragraph.setStyle(style.toCss()),
                TextStyle.EMPTY.updateFontSize(12).updateFontFamily("Serif").updateTextColor(Color.BLACK),
                (text, style) -> text.setStyle(style.toCss()));

        area.setWrapText(true);
        area.setStyleCodecs(ParStyle.CODEC, TextStyle.CODEC);                        // area.();


        area.setMinHeight(Paper.A4.getHeight());
        area.setMaxHeight(Paper.A4.getHeight());
        area.setMinWidth(Paper.A4.getWidth());
        area.setMaxWidth(Paper.A4.getWidth());

        area.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
               // Utils.getMainWindowController().getCyrrentFocusTextArea().textProperty().removeListener(Utils.areaOverflowDetector);
                Utils.getMainWindowController().setCurrentFocusTextArea(area);
               // Utils.getMainWindowController().getCyrrentFocusTextArea().textProperty().addListener(Utils.areaOverflowDetector);
            }
        });

//        area.textProperty().addListener((observable, oldValue, newValue) -> {
//            StyledTextArea<ParStyle, TextStyle> currentArea = Utils.getMainWindowController().getCyrrentFocusTextArea();
//            EasyReaderItem currentItem = Utils.getMainWindowController().getCurrentEasyReaderItem();
//            ArrayList<StyledTextArea<ParStyle, TextStyle>> currentPagesList = currentItem.getPagesList();
//
//            observable.getValue();
//
//            if(currentArea.getTotalHeightEstimate() > Paper.A4.getHeight()){
//                System.out.println("new page");
//
//                //HERE ADD NIE PAGE TO LIST IN CURRENT SELECTED ITEM
//                currentItem.addPage();
//
//                //REFRESH PAGES LIST
//                currentPagesList = currentItem.getPagesList();
//
//                StyledTextArea<ParStyle, TextStyle>  newArea = currentPagesList.get(currentPagesList.size() - 1);
//
//                while(currentArea.getTotalHeightEstimate() > Paper.A4.getHeight()){
//                    try {
//                        org.fxmisc.richtext.model.StyledDocument areasdasd = currentArea.getDocument().subDocument(currentArea.getParagraphs().size() - 2);
//                        newArea.append(areasdasd);
//
//                        String asdasd = newArea.getText();
//
//                        System.out.println(currentArea.getParagraph(currentArea.getParagraphs().size() - 2).length());
//                        currentArea.deleteText(currentArea.getParagraphs().size() - 2, 0, currentArea.getParagraphs().size() - 1, currentArea.getParagraph(currentArea.getParagraphs().size() - 1).length());
//                    }catch (Exception e){
//                        e.getMessage();
//                    }
//                }
//
//                Utils.getMainWindowController().refreshTextEditorPane();
//            }
//
//            if(currentArea.getText().isEmpty() && currentPagesList.indexOf(currentArea) != 0){
//                System.out.println("remove page");
//                //HERE REMOVE PAGE FROM LIST IN CURRENT SELECTED ITEM
//                Utils.getMainWindowController().refreshTextEditorPane();
//            }
//        });

        Utils.getMainWindowController().addListenersForArea(area);

        pagesList.add(area);
    }

    public ArrayList<StyledTextArea<ParStyle, TextStyle>> getPagesList() {
        return pagesList;
    }

    public ChangeListener<String> areaOverflowDetector = (observable, oldValue, newValue) -> {

        StyledTextArea<ParStyle, TextStyle> currentArea = Utils.getMainWindowController().getCyrrentFocusTextArea();
        EasyReaderItem currentItem = Utils.getMainWindowController().getCurrentEasyReaderItem();
        ArrayList<StyledTextArea<ParStyle, TextStyle>> currentPagesList = currentItem.getPagesList();

        if(currentArea.getTotalHeightEstimate() > Paper.A4.getHeight()){
            System.out.println("new page");

            //HERE ADD NIE PAGE TO LIST IN CURRENT SELECTED ITEM
            currentItem.addPage();

            //REFRESH PAGES LIST
            currentPagesList = currentItem.getPagesList();

            StyledTextArea<ParStyle, TextStyle>  newArea = currentPagesList.get(currentPagesList.size() - 1);

            while(currentArea.getTotalHeightEstimate() > Paper.A4.getHeight()){
                try {
                    org.fxmisc.richtext.model.StyledDocument areasdasd = currentArea.getDocument().subDocument(currentArea.getParagraphs().size() - 2);
                    newArea.append(areasdasd);

                    String asdasd = newArea.getText();

                    System.out.println(currentArea.getParagraph(currentArea.getParagraphs().size() - 2).length());
                    currentArea.deleteText(currentArea.getParagraphs().size() - 2, 0, currentArea.getParagraphs().size() - 1, currentArea.getParagraph(currentArea.getParagraphs().size() - 1).length());
                }catch (Exception e){
                    e.getMessage();
                }
            }

            Utils.getMainWindowController().refreshTextEditorPane();
        }

        if(currentArea.getText().isEmpty() && currentPagesList.indexOf(currentArea) != 0){
            System.out.println("remove page");
            //HERE REMOVE PAGE FROM LIST IN CURRENT SELECTED ITEM
            Utils.getMainWindowController().refreshTextEditorPane();
        }
    };
}
