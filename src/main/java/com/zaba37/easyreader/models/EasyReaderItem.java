/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zaba37.easyreader.models;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.textEditor.ParStyle;
import com.zaba37.easyreader.textEditor.TextStyle;

import java.awt.*;
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
    private StyledTextArea<ParStyle, TextStyle> textArea;

    public EasyReaderItem(File file) {
        this.imageFile = file;
        image = new Image(file.toURI().toString());
        name = file.getName();

        textArea = new StyledTextArea<>(
                ParStyle.EMPTY, (paragraph, style) -> paragraph.setStyle(style.toCss()),
                TextStyle.EMPTY.updateFontSize(12).updateFontFamily("Serif").updateTextColor(Color.BLACK),
                (text, style) -> text.setStyle(style.toCss()));

        textArea.setStyleCodecs(ParStyle.CODEC, TextStyle.CODEC);
        Utils.getMainWindowController().addListenersForArea(textArea);

        textArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Utils.getMainWindowController().setCurrentFocusTextArea(textArea);
            }
        });

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

    private BufferedImage scale(BufferedImage source,double ratio) {
        int w = (int) (source.getWidth() * ratio);
        int h = (int) (source.getHeight() * ratio);
        BufferedImage bi = getCompatibleImage(w, h);
        Graphics2D g2d = bi.createGraphics();
        double xScale = (double) w / source.getWidth();
        double yScale = (double) h / source.getHeight();
        AffineTransform at = AffineTransform.getScaleInstance(xScale,yScale);
        g2d.drawRenderedImage(source, at);
        g2d.dispose();

        return bi;
    }

    private BufferedImage getCompatibleImage(int w, int h) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        BufferedImage image = gc.createCompatibleImage(w, h);

        return image;
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

    public org.fxmisc.richtext.model.StyledDocument getStyledDocument(){
        return textArea.getDocument();
    }

    public StyledTextArea<ParStyle, TextStyle> getTextArea(){
        return textArea;
    }

}
