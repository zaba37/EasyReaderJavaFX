package com.zaba37.easyreader.imageEditor;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.asyncTasks.ImageBackgroundCropController;
import com.zaba37.easyreader.controllers.LoadingWindowSceneController;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

/**
 * Created by zaba3 on 19.10.2016.
 */
public class RectangleSelection {

    private final DragContext dragContext = new DragContext();
    private Rectangle rect = new Rectangle();
    private Group group;
    private ImageView imageView;

    public Bounds getBounds() {
        return rect.getBoundsInParent();
    }

    public RectangleSelection(Group group) {

        this.group = group;
        this.imageView = (ImageView) group.getChildren().get(0);
        rect = new Rectangle(0, 0, 0, 0);
        rect.setStroke(Color.BLUE);
        rect.setStrokeWidth(1);
        rect.setStrokeLineCap(StrokeLineCap.ROUND);
        rect.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));

        group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
        group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
        group.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);

        showInformationAlert();
    }

    private void showInformationAlert() {
        Alert alert = createAlertWithOptOut(Alert.AlertType.INFORMATION, "Crop function", null,
                "Are you sure you wish to exit?", "Do not ask again",
                param -> {
                    Preferences.userRoot().node(Utils.KEY_PREFERENCES).put(Utils.KEY_SHOW_INFORMATION_CROP_FUNCTION, param ? "Always" : "Never");
                    return null;
                }, ButtonType.OK);

        if (alert.showAndWait().filter(t -> t == ButtonType.YES).isPresent()) {

        }
    }

    private void showSaveSelectionAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

        alert.setHeaderText("Do you want to cut the selected part of the image and add it to the list of loaded images in the program?");

        if (alert.showAndWait().filter(t -> t == ButtonType.OK).isPresent()) {
            crop();
        }
    }

    private void crop() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ImageBackgroundCropWindowScene.fxml"));

        Parent root = null;
        try {
            root = (Parent) fxmlLoader.load();
            ImageBackgroundCropController cropController = fxmlLoader.<ImageBackgroundCropController>getController();

            cropController.setImageView(imageView);
            cropController.setBounds(getBounds());

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(Utils.getMainWindow());
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

            cropController.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeSelection() {
        rect.setX(0);
        rect.setY(0);
        rect.setWidth(0);
        rect.setHeight(0);

        group.removeEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
        group.removeEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
        group.removeEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);
    }

    EventHandler<MouseEvent> onMousePressedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {

            if (event.isSecondaryButtonDown())
                return;

            if (rect.getWidth() != 0 && rect.getHeight() != 0) {
                if (event.getX() >= rect.getX() && event.getX() <= rect.getX() + rect.getWidth() && event.getY() >= rect.getY() && event.getY() <= rect.getY() + rect.getHeight()) {
                    showSaveSelectionAlert();
                }
            }

            // remove old rect
            rect.setX(0);
            rect.setY(0);
            rect.setWidth(0);
            rect.setHeight(0);

            group.getChildren().remove(rect);

            // prepare new drag operation
            dragContext.mouseAnchorX = event.getX();
            dragContext.mouseAnchorY = event.getY();

            rect.setX(dragContext.mouseAnchorX);
            rect.setY(dragContext.mouseAnchorY);
            rect.setWidth(0);
            rect.setHeight(0);

            group.getChildren().add(rect);

        }
    };

    EventHandler<MouseEvent> onMouseDraggedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {

            if (event.isSecondaryButtonDown())
                return;

            double offsetX = event.getX() - dragContext.mouseAnchorX;
            double offsetY = event.getY() - dragContext.mouseAnchorY;

            if (offsetX > 0)
                rect.setWidth(offsetX);
            else {
                rect.setX(event.getX());
                rect.setWidth(dragContext.mouseAnchorX - rect.getX());
            }

            if (offsetY > 0) {
                rect.setHeight(offsetY);
            } else {
                rect.setY(event.getY());
                rect.setHeight(dragContext.mouseAnchorY - rect.getY());
            }
        }
    };


    EventHandler<MouseEvent> onMouseReleasedEventHandler = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {

            if (event.isSecondaryButtonDown())
                return;
        }
    };

    private static final class DragContext {
        public double mouseAnchorX;
        public double mouseAnchorY;
    }

    private static Alert createAlertWithOptOut(Alert.AlertType type, String title, String headerText,
                                               String message, String optOutMessage, Callback<Boolean, Void> optOutAction,
                                               ButtonType... buttonTypes) {
        Alert alert = new Alert(type);

        alert.getDialogPane().applyCss();
        Node graphic = alert.getDialogPane().getGraphic();

        alert.setDialogPane(new DialogPane() {
            @Override
            protected Node createDetailsButton() {
                CheckBox optOut = new CheckBox();
                optOut.setText(optOutMessage);
                optOut.setOnAction(e -> optOutAction.call(optOut.isSelected()));
                return optOut;
            }
        });

        alert.getDialogPane().getButtonTypes().addAll(buttonTypes);
        alert.getDialogPane().setContentText(message);

        alert.getDialogPane().setExpandableContent(new Group());
        alert.getDialogPane().setExpanded(true);

        alert.getDialogPane().setGraphic(graphic);
        alert.setTitle(title);
        alert.setHeaderText(headerText);

        return alert;
    }
}
