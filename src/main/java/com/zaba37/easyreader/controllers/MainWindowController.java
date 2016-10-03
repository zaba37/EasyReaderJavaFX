package com.zaba37.easyreader.controllers;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.actions.menuBar.OpenAction;
import com.zaba37.easyreader.models.EasyReaderItem;
import com.zaba37.easyreader.ocr.OcrEngine;
import com.zaba37.easyreader.textEditor.*;
import io.github.karols.hocr4j.Page;
import io.github.karols.hocr4j.dom.HocrParser;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.print.Paper;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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

import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpans;
import org.reactfx.SuspendableNo;
import org.w3c.dom.*;

import static org.fxmisc.richtext.model.TwoDimensional.Bias.Backward;
import static org.fxmisc.richtext.model.TwoDimensional.Bias.Forward;

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

    @FXML
    private VBox textVBox;

    @FXML
    private ComboBox<Integer> textSizeComboBox;

    @FXML
    private ComboBox<String> textFontComboBox;

    @FXML
    private ColorPicker textColorColorPicker;

    @FXML
    private ColorPicker textBackgroundColorColorPicker;

    private DoubleProperty zoomProperty;
    private Group textEditorScrollGroup;
    private Parent textZoomPane;
    private OpenAction openAction;
    private ArrayList<EasyReaderItem> loadedItemList;
    private final ObservableList observableList = FXCollections.observableArrayList();
    private int currentSelectedItemIndex;
    private final SuspendableNo updatingToolbar = new SuspendableNo();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        zoomProperty = new SimpleDoubleProperty(200);
        imageView.preserveRatioProperty().set(true);

        TextArea textArea = new TextArea();

        textArea.setMinHeight(Paper.A4.getHeight());
        textArea.setMaxHeight(Paper.A4.getHeight());
        textArea.setMinWidth(Paper.A4.getWidth());
        textArea.setMaxWidth(Paper.A4.getWidth());

        zoomProperty.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable arg0) {
                imageView.setFitWidth(zoomProperty.get() * 4);
                imageView.setFitHeight(zoomProperty.get() * 3);
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
                zoomProperty.set(200);
            }
        });

        imageScrollPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                imageView.setFitHeight(newSceneHeight.doubleValue());
                zoomProperty.set(200);
            }
        });

        borderPaneLeft.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                showImagesListButton.setLayoutY(newSceneHeight.doubleValue() / 2 - showImagesListButton.getHeight() / 2);
                hideImagesListButton.setLayoutY(newSceneHeight.doubleValue() / 2 - hideImagesListButton.getHeight() / 2);
                Utils.setMainWindow(borderPaneLeft.getScene().getWindow());
            }
        });

        borderPaneLeft.setMaxWidth(4);
        borderPaneLeft.setMinWidth(4);

        hideImagesListButton.setVisible(false);
        showImagesListButton.setVisible(true);

        textArea.setWrapText(true);

        ArrayList<TextArea> list = new ArrayList();

                textEditorScrollGroup = new Group();
                VBox v = new VBox();
        
        for (int i = 0; i < 10; i++) {
            TextArea a = new TextArea();
            a.setMinHeight(Paper.A4.getHeight());
            a.setMaxHeight(Paper.A4.getHeight());
            a.setMinWidth(Paper.A4.getWidth());
            a.setMaxWidth(Paper.A4.getWidth());
            list.add(a);
            v.getChildren().add(a);

        }

        textEditorScrollGroup.getChildren().add(v);

        textZoomPane = createZoomPane(textEditorScrollGroup);
        textVBox.getChildren().add(textZoomPane);

        //System

        textSizeComboBox.setItems(FXCollections.observableArrayList(5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 18, 20, 22, 24, 28, 32, 36, 40, 48, 56, 64, 72));
        textSizeComboBox.getSelectionModel().select(Integer.valueOf(12));
        textFontComboBox.setItems(FXCollections.observableList(Font.getFamilies()));
        textFontComboBox.getSelectionModel().select("Serif");

        textColorColorPicker.setTooltip(new Tooltip("Text color"));
        textBackgroundColorColorPicker.setTooltip(new Tooltip("Text background"));

        textSizeComboBox.setOnAction(evt -> updateFontSize(textSizeComboBox.getValue()));
        textFontComboBox.setOnAction(evt -> updateFontFamily(textFontComboBox.getValue()));
        textColorColorPicker.valueProperty().addListener((o, old, color) -> updateTextColor(color));
        textBackgroundColorColorPicker.valueProperty().addListener((o, old, color) -> updateBackgroundColor(color));
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
            
            refreshTextEditorPane();
        } else {
            loadedItemList = new ArrayList();
        }
    }

    private Parent createZoomPane(final Group group) {
        final double SCALE_DELTA = 1.1;
        final StackPane zoomPane = new StackPane();

        zoomPane.getChildren().add(group);

        final ScrollPane scroller = new ScrollPane();
        final Group scrollContent = new Group(zoomPane);
        scroller.setContent(scrollContent);

        scroller.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable,
                    Bounds oldValue, Bounds newValue) {
                zoomPane.setMinSize(newValue.getWidth(), newValue.getHeight());
            }
        });

       // scroller.setPrefViewportWidth();
       // scroller.setPrefViewportHeight();
        zoomPane.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                event.consume();

                if (event.getDeltaY() == 0) {
                    return;
                }

                double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA
                        : 1 / SCALE_DELTA;

                // amount of scrolling in each direction in scrollContent coordinate
                // units
                Point2D scrollOffset = figureScrollOffset(scrollContent, scroller);

                group.setScaleX(group.getScaleX() * scaleFactor);
                group.setScaleY(group.getScaleY() * scaleFactor);

                // move viewport so that old center remains in the center after the
                // scaling
                repositionScroller(scrollContent, scroller, scaleFactor, scrollOffset);

            }
        });

        final ObjectProperty<Point2D> lastMouseCoordinates = new SimpleObjectProperty<Point2D>();
        scrollContent.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                lastMouseCoordinates.set(new Point2D(event.getX(), event.getY()));
            }
        });

        scrollContent.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double deltaX = event.getX() - lastMouseCoordinates.get().getX();
                double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
                double deltaH = deltaX * (scroller.getHmax() - scroller.getHmin()) / extraWidth;
                double desiredH = scroller.getHvalue() - deltaH;
                scroller.setHvalue(Math.max(0, Math.min(scroller.getHmax(), desiredH)));

                double deltaY = event.getY() - lastMouseCoordinates.get().getY();
                double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
                double deltaV = deltaY * (scroller.getHmax() - scroller.getHmin()) / extraHeight;
                double desiredV = scroller.getVvalue() - deltaV;
                scroller.setVvalue(Math.max(0, Math.min(scroller.getVmax(), desiredV)));
            }
        });

        return scroller;
    }

    private Point2D figureScrollOffset(javafx.scene.Node scrollContent, ScrollPane scroller) {
        double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
        double hScrollProportion = (scroller.getHvalue() - scroller.getHmin()) / (scroller.getHmax() - scroller.getHmin());
        double scrollXOffset = hScrollProportion * Math.max(0, extraWidth);
        double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
        double vScrollProportion = (scroller.getVvalue() - scroller.getVmin()) / (scroller.getVmax() - scroller.getVmin());
        double scrollYOffset = vScrollProportion * Math.max(0, extraHeight);
        return new Point2D(scrollXOffset, scrollYOffset);
    }

    private void repositionScroller(javafx.scene.Node scrollContent, ScrollPane scroller, double scaleFactor, Point2D scrollOffset) {
        double scrollXOffset = scrollOffset.getX();
        double scrollYOffset = scrollOffset.getY();
        double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
        if (extraWidth > 0) {
            double halfWidth = scroller.getViewportBounds().getWidth() / 2;
            double newScrollXOffset = (scaleFactor - 1) * halfWidth + scaleFactor * scrollXOffset;
            scroller.setHvalue(scroller.getHmin() + newScrollXOffset * (scroller.getHmax() - scroller.getHmin()) / extraWidth);
        } else {
            scroller.setHvalue(scroller.getHmin());
        }
        double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
        if (extraHeight > 0) {
            double halfHeight = scroller.getViewportBounds().getHeight() / 2;
            double newScrollYOffset = (scaleFactor - 1) * halfHeight + scaleFactor * scrollYOffset;
            scroller.setVvalue(scroller.getVmin() + newScrollYOffset * (scroller.getVmax() - scroller.getVmin()) / extraHeight);
        } else {
            scroller.setHvalue(scroller.getHmin());
        }
    }
    
    private void refreshTextEditorPane(){
        
    }


    private void toggleBold() {
        updateStyleInSelection(spans -> TextStyle.bold(!spans.styleStream().allMatch(style -> style.bold.orElse(false))));
    }

    private void toggleItalic() {
        updateStyleInSelection(spans -> TextStyle.italic(!spans.styleStream().allMatch(style -> style.italic.orElse(false))));
    }

    private void toggleUnderline() {
        updateStyleInSelection(spans -> TextStyle.underline(!spans.styleStream().allMatch(style -> style.underline.orElse(false))));
    }

    private void toggleStrikethrough() {
        updateStyleInSelection(spans -> TextStyle.strikethrough(!spans.styleStream().allMatch(style -> style.strikethrough.orElse(false))));
    }

    private void alignLeft() {
        updateParagraphStyleInSelection(ParStyle.alignLeft());
    }

    private void alignCenter() {
        updateParagraphStyleInSelection(ParStyle.alignCenter());
    }

    private void alignRight() {
        updateParagraphStyleInSelection(ParStyle.alignRight());
    }

    private void alignJustify() {
        updateParagraphStyleInSelection(ParStyle.alignJustify());
    }

    private void updateStyleInSelection(Function<StyleSpans<TextStyle>, TextStyle> mixinGetter) {
        IndexRange selection = area.getSelection();
        if(selection.getLength() != 0) {
            StyleSpans<TextStyle> styles = area.getStyleSpans(selection);
            TextStyle mixin = mixinGetter.apply(styles);
            StyleSpans<TextStyle> newStyles = styles.mapStyles(style -> style.updateWith(mixin));
            area.setStyleSpans(selection.getStart(), newStyles);
        }
    }

    private void updateStyleInSelection(TextStyle mixin) {
        IndexRange selection = area.getSelection();
        if (selection.getLength() != 0) {
            StyleSpans<TextStyle> styles = area.getStyleSpans(selection);
            StyleSpans<TextStyle> newStyles = styles.mapStyles(style -> style.updateWith(mixin));
            area.setStyleSpans(selection.getStart(), newStyles);
        }
    }

    private void updateParagraphStyleInSelection(Function<ParStyle, ParStyle> updater) {
        IndexRange selection = area.getSelection();
        int startPar = area.offsetToPosition(selection.getStart(), Forward).getMajor();
        int endPar = area.offsetToPosition(selection.getEnd(), Backward).getMajor();
        for(int i = startPar; i <= endPar; ++i) {
            Paragraph<ParStyle, TextStyle> paragraph = area.getParagraph(i);
            area.setParagraphStyle(i, updater.apply(paragraph.getParagraphStyle()));
        }
    }

    private void updateParagraphStyleInSelection(ParStyle mixin) {
        updateParagraphStyleInSelection(style -> style.updateWith(mixin));
    }

    private void updateFontSize(Integer size) {
        if(!updatingToolbar.get()) {
            updateStyleInSelection(TextStyle.fontSize(size));
        }
    }

    private void updateFontFamily(String family) {
        if(!updatingToolbar.get()) {
            updateStyleInSelection(TextStyle.fontFamily(family));
        }
    }

    private void updateTextColor(Color color) {
        if(!updatingToolbar.get()) {
            updateStyleInSelection(TextStyle.textColor(color));
        }
    }

    private void updateBackgroundColor(Color color) {
        if(!updatingToolbar.get()) {
            updateStyleInSelection(TextStyle.backgroundColor(color));
        }
    }
}
