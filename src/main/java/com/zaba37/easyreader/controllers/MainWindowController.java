package com.zaba37.easyreader.controllers;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.actions.menuBar.OpenAction;
import com.zaba37.easyreader.models.EasyReaderItem;
import com.zaba37.easyreader.ocr.OcrEngine;
import io.github.karols.hocr4j.Page;
import io.github.karols.hocr4j.dom.HocrParser;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.print.Paper;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

public class MainWindowController implements Initializable {

    @FXML
    private ScrollPane imageScrollPane;

    @FXML
    private ImageView imageView;

    @FXML
    private ListView imagesListView;

    @FXML
    private AnchorPane borderPaneLeft;

    @FXML
    private Button hideImagesListButton;

    @FXML
    private Button showImagesListButton;

    @FXML
    private Label imageSizeLabel;

    @FXML
    private TextArea textArea;

    @FXML
    private ScrollPane textScrollPane;

    private DoubleProperty zoomProperty;

    private OpenAction openAction;
    private ArrayList<EasyReaderItem> loadedItemList;
    private final ObservableList observableList = FXCollections.observableArrayList();
    private int currentSelectedItemIndex;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        zoomProperty = new SimpleDoubleProperty(200);
        imageView.preserveRatioProperty().set(true);

        zoomProperty.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable arg0) {
                imageView.setFitWidth(zoomProperty.get() * 4);
                imageView.setFitHeight(zoomProperty.get() * 3);
                textArea.setScaleX(zoomProperty.get() * 4);
                textArea.setScaleY(zoomProperty.get() * 3);
            }
        });

        imageScrollPane.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                if (event.getDeltaY() > 0) {
                    zoomProperty.set(zoomProperty.get() * 1.1);
                } else if (event.getDeltaY() < 0) {
                    zoomProperty.set(zoomProperty.get() / 1.1);
                }
            }
        });

        imageScrollPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                imageView.setFitWidth(newSceneWidth.doubleValue());

                textArea.setMinHeight(Paper.A4.getHeight());
                textArea.setMaxHeight(Paper.A4.getHeight());
                textArea.setMinWidth(Paper.A4.getWidth());
                textArea.setMaxWidth(Paper.A4.getWidth());

                zoomProperty.set(200);
            }
        });

        imageScrollPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                imageView.setFitHeight(newSceneHeight.doubleValue());

                textArea.setMinHeight(Paper.A4.getHeight());
                textArea.setMaxHeight(Paper.A4.getHeight());
                textArea.setMinWidth(Paper.A4.getWidth());
                textArea.setMaxWidth(Paper.A4.getWidth());

                zoomProperty.set(200);
            }
        });

        borderPaneLeft.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                showImagesListButton.setLayoutY(newSceneHeight.doubleValue() / 2 - showImagesListButton.getHeight() / 2);
                hideImagesListButton.setLayoutY(newSceneHeight.doubleValue() / 2 - hideImagesListButton.getHeight() / 2);
                Utils.setMainWindow(borderPaneLeft.getScene().getWindow());

                ScrollBar scrollBarv = (ScrollBar) textArea.lookup(".scroll-bar:vertical");
                scrollBarv.setDisable(true);
            }
        });

        borderPaneLeft.setMaxWidth(4);
        borderPaneLeft.setMinWidth(4);

        hideImagesListButton.setVisible(false);
        showImagesListButton.setVisible(true);

        textArea.setMinHeight(Paper.A4.getHeight());
        textArea.setMaxHeight(Paper.A4.getHeight());
        textArea.setMinWidth(Paper.A4.getWidth());
        textArea.setMaxWidth(Paper.A4.getWidth());

    }

    @FXML
    private void handleMenuFileActions(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();

        switch (item.getId()) {
            case "OpenMenuItem":
                System.out.print("Open");
                openAction();
                break;
            case "SaveAsTxtMenuItem":
                System.out.print("TXT");
                break;
            case "SaveAsDocMenuItem":
                System.out.print("DOC");
                break;
            case "SaveAsDocxMenuItem":
                System.out.print("DOCX");
                break;
            case "SaveAsPdfMenuItem":
                System.out.print("PDF");
                break;
            case "ExitMenuItem":
                System.out.print("EXIT");
                break;
        }
    }

    @FXML
    private void handleOCRActions(ActionEvent event) throws IOException {
        MenuItem item = (MenuItem) event.getSource();

        switch (item.getId()) {
            case "StartOcrMenuItem":
                OcrEngine ocrEngine = OcrEngine.getInstance();
                String result = ocrEngine.getOcrResult(loadedItemList.get(currentSelectedItemIndex).getFile());
                List<Page> pages = HocrParser.parse(result);

                System.out.println("");
                List<String> textLines = pages.get(0).getAllLinesAsStrings();
                for (String string : textLines) {
                    System.out.print(string);
                }

                break;
            case "OCRSettingsMenuItem":
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/OCRSettingsScene.fxml"));
                Scene scene = new Scene(root);
                Stage stahe = new Stage();
                stahe.initModality(Modality.WINDOW_MODAL);
                //stahe.initOwner(this.borderPaneLeft.getScene().getWindow());
                stahe.initOwner(Utils.getMainWindow());
                stahe.setScene(scene);
                stahe.setResizable(false);
                stahe.show();

                break;
        }
    }

    @FXML
    private void handleImageEditorActions(ActionEvent event) {
        Button button = (Button) event.getSource();

        switch (button.getId()) {
            case "rotateLeftButton":
                loadedItemList.get(currentSelectedItemIndex).rotatedImage(-90);
                imageView.setImage(loadedItemList.get(currentSelectedItemIndex).getImage());
                imagesListView.refresh();
                break;
            case "rotateRightButton":
                loadedItemList.get(currentSelectedItemIndex).rotatedImage(90);
                imageView.setImage(loadedItemList.get(currentSelectedItemIndex).getImage());
                imagesListView.refresh();
                break;
            case "zoomIncreasingButton":
                break;
            case "zoomDecreasingButton":
                break;
        }
    }

    private void openAction() {
        if (openAction == null) {
            openAction = new OpenAction();
        }

        try {
            openAction.ShowOpenFilesDialog();
        } catch (IOException ex) {
            Logger.getLogger(MainWindowController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleShowImagesListButton(ActionEvent event) {
        borderPaneLeft.setMaxWidth(180);
        borderPaneLeft.setMinWidth(180);

        hideImagesListButton.setVisible(true);
        showImagesListButton.setVisible(false);
    }

    @FXML
    private void handleHideImagesListButton(ActionEvent event) {
        borderPaneLeft.setMaxWidth(4);
        borderPaneLeft.setMinWidth(4);

        hideImagesListButton.setVisible(false);
        showImagesListButton.setVisible(true);
    }

    @FXML
    private void handleListMouseClick(MouseEvent arg) {
        imageView.setImage(loadedItemList.get(imagesListView.getSelectionModel().getSelectedIndex()).getImage());
        currentSelectedItemIndex = imagesListView.getSelectionModel().getSelectedIndex();
        imageSizeLabel.setText("" + loadedItemList.get(imagesListView.getSelectionModel().getSelectedIndex()).getImage().getHeight() + " x " + loadedItemList.get(imagesListView.getSelectionModel().getSelectedIndex()).getImage().getWidth());
    }

    public void initImageListView(ArrayList<EasyReaderItem> list) {
        if (!list.isEmpty()) {
            loadedItemList = new ArrayList();
            loadedItemList.addAll(list);
            imageView.setImage(loadedItemList.get(0).getImage());
            observableList.setAll(loadedItemList);
            imagesListView.setItems(observableList);

            imagesListView.setCellFactory(new Callback<ListView<EasyReaderItem>, javafx.scene.control.ListCell<EasyReaderItem>>() {
                @Override
                public ListCell<EasyReaderItem> call(ListView<EasyReaderItem> listView) {
                    return new ImageListCellItem();
                }
            });

            borderPaneLeft.setMaxWidth(180);
            borderPaneLeft.setMinWidth(180);

            hideImagesListButton.setVisible(true);
            showImagesListButton.setVisible(false);

            currentSelectedItemIndex = 0;
        } else {
            loadedItemList = new ArrayList();
        }
    }
}
