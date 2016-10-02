/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zaba37.easyreader.models;

import com.zaba37.easyreader.textEditor.ParStyle;
import com.zaba37.easyreader.textEditor.TextStyle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.StyledTextArea;

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
        area.setStyleCodecs(ParStyle.CODEC, TextStyle.CODEC);
    }

    public ArrayList<StyledTextArea<ParStyle, TextStyle>> getPagesList() {
        return pagesList;
    }
}
