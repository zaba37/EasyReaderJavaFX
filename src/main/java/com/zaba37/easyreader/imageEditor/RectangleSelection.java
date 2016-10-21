package com.zaba37.easyreader.imageEditor;

import com.zaba37.easyreader.Utils;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by zaba3 on 19.10.2016.
 */
public class RectangleSelection {

    private final DragContext dragContext = new DragContext();
    private Rectangle rect = new Rectangle();
    private double zoomValue = 0;
    private Group group;
    private ImageView imageView;

    public Bounds getBounds() {
        return rect.getBoundsInParent();
    }

    public void setZoomValue(double zoomValue){
        System.out.println(zoomValue);
        System.out.print("X: " + rect.getX());
        System.out.print("Y: " + rect.getY());
        System.out.print("Height: " + rect.getHeight());
        System.out.print("Width: " + rect.getWidth());
        System.out.println();
        this.zoomValue = zoomValue;
    }

    public RectangleSelection(Group group) {

        this.group = group;
        this.imageView = (ImageView) group.getChildren().get(0);
        rect = new Rectangle( 0,0,0,0);
        rect.setStroke(Color.BLUE);
        rect.setStrokeWidth(1);
        rect.setStrokeLineCap(StrokeLineCap.ROUND);
        rect.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));

        group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
        group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
        group.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);

    }

    public void crop() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");

        File file = fileChooser.showSaveDialog(Utils.getMainStage());
        if (file == null)
            return;

        int widthFrame = (int) getBounds().getWidth();
        int heightFrame = (int) getBounds().getHeight();

        int widthShowImage = (int)imageView.getBoundsInParent().getWidth();
        int heightShowImage = (int)imageView.getBoundsInParent().getHeight();

        int correctFrameWidth = (int) ((imageView.getImage().getWidth() / widthShowImage) * widthFrame);
        int correctFrameHeight = (int) ((imageView.getImage().getHeight() / heightShowImage) * heightFrame);

        int pointX = 0;
        int pointY = 0;

        if(imageView.getImage().getHeight() >= heightShowImage && imageView.getImage().getWidth() >= widthShowImage){
            pointX = (int) getBounds().getMinX() * 4;
            pointY = (int) (getBounds().getMinY() * 3);
        }else{
            pointX = (int) getBounds().getMinX() / 4;
            pointY = (int) (getBounds().getMinY() / 3);
        }

        PixelReader reader = imageView.getImage().getPixelReader();
        WritableImage newImage = new WritableImage(reader, pointX, pointY, correctFrameWidth, correctFrameHeight);

//        SnapshotParameters parameters = new SnapshotParameters();
//        parameters.setFill(Color.TRANSPARENT);
//        parameters.setViewport(new Rectangle2D( bounds.getMinX(), bounds.getMinY(), width, height));
//
//        WritableImage wi = new WritableImage( width, height);
//        imageView.snapshot(parameters, wi);

        // save image
        // !!! has bug because of transparency (use approach below) !!!
        // --------------------------------
//        try {
//          ImageIO.write(SwingFXUtils.fromFXImage( wi, null), "jpg", file);
//      } catch (IOException e) {
//          e.printStackTrace();
//      }


        // save image (without alpha)
        // --------------------------------
        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(newImage, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(), BufferedImage.OPAQUE);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);

        try {
            ImageIO.write(bufImageRGB, "jpg", file);
            System.out.println( "Image saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        graphics.dispose();

    }

    EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {

            if( event.isSecondaryButtonDown())
                return;


            // remove old rect
            rect.setX(0);
            rect.setY(0);
            rect.setWidth(0);
            rect.setHeight(0);

            group.getChildren().remove( rect);


            // prepare new drag operation
            dragContext.mouseAnchorX = event.getX();
            dragContext.mouseAnchorY = event.getY();

            rect.setX(dragContext.mouseAnchorX);
            rect.setY(dragContext.mouseAnchorY);
            rect.setWidth(0);
            rect.setHeight(0);

            group.getChildren().add( rect);
        }
    };

    EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {

            if( event.isSecondaryButtonDown())
                return;

            double offsetX = event.getX() - dragContext.mouseAnchorX;
            double offsetY = event.getY() - dragContext.mouseAnchorY;

            if( offsetX > 0)
                rect.setWidth( offsetX);
            else {
                rect.setX(event.getX());
                rect.setWidth(dragContext.mouseAnchorX - rect.getX());
            }

            if( offsetY > 0) {
                rect.setHeight( offsetY);
            } else {
                rect.setY(event.getY());
                rect.setHeight(dragContext.mouseAnchorY - rect.getY());
            }
        }
    };


    EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {

            if( event.isSecondaryButtonDown())
                return;

                /*
                rect.setX(0);
                rect.setY(0);
                rect.setWidth(0);
                rect.setHeight(0);

                group.getChildren().remove( rect);
                */

        }
    };

    private static final class DragContext {
        public double mouseAnchorX;
        public double mouseAnchorY;
    }
}
