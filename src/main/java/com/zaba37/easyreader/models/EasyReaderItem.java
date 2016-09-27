/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zaba37.easyreader.models;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

/**
 *
 * @author Krystian
 */
public class EasyReaderItem {
    
    private final File imageFile;
    private Image image;
    private final String name;
    
    public EasyReaderItem(File file){
        this.imageFile = file;
        image = new Image(file.toURI().toString());
        name = file.getName();
    }
    
    public void rotatedImage(double angle) {
        BufferedImage bufferedImage = new BufferedImage((int)image.getWidth(), (int)image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        SwingFXUtils.fromFXImage(image, bufferedImage);

        AffineTransform affineTransform = new AffineTransform();
        
        if(angle < 0){
            affineTransform.rotate(Math.toRadians(angle), image.getWidth() / 2, image.getWidth() / 2);
        }else{
            affineTransform.rotate(Math.toRadians(angle), image.getHeight() / 2, image.getHeight() / 2);
        }
        
        AffineTransformOp op = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BICUBIC);
        
        BufferedImage newImage = new BufferedImage(bufferedImage.getHeight(), bufferedImage.getWidth(), bufferedImage.getType());
        
        op.filter(bufferedImage, newImage);
        
        WritableImage writableImage = new WritableImage(newImage.getWidth(), newImage.getHeight());
        
        SwingFXUtils.toFXImage(newImage, writableImage);
        
        this.image = writableImage;
    }   
    
    public String getName(){
        return name;
    }
    
    public Image getImage(){
        return image;
    }
    
    public void setImage(Image image){
        this.image = image;
    }
    
    public File getFile(){
        return imageFile;
    }
    
}
