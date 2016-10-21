package com.zaba37.easyreader.controllers;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.actions.menuBar.OpenAction;
import com.zaba37.easyreader.imageEditor.RectangleSelection;
import com.zaba37.easyreader.models.EasyReaderItem;
import com.zaba37.easyreader.ocr.OcrEngine;
import com.zaba37.easyreader.textEditor.*;
import io.github.karols.hocr4j.Line;
import io.github.karols.hocr4j.Page;
import io.github.karols.hocr4j.Word;
import io.github.karols.hocr4j.dom.HocrElement;
import io.github.karols.hocr4j.dom.HocrParser;

import java.io.*;

import java.util.*;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.html.HTMLEditorKit;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javafx.scene.text.*;

import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyledDocument;
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

    @FXML
    private Button boldTextButton;

    @FXML
    private Button italicTextButton;

    @FXML
    private Button underlineTextButton;

    @FXML
    private Button strikethroughTextButton;

    @FXML
    private ToggleButton alginLeftTextButton;

    @FXML
    private ToggleButton alginCenterTextButton;

    @FXML
    private ToggleButton alginRightTextButton;

    @FXML
    private ToggleButton alginJustifyTextButton;

    @FXML
    private ToggleGroup alginGroup;

    @FXML
    private Group imageGroup;

    private DoubleProperty zoomProperty;
    private Group textEditorScrollGroup;
    private Parent textZoomPane;
    private OpenAction openAction;
    private ArrayList<EasyReaderItem> loadedItemList;
    private final ObservableList observableList = FXCollections.observableArrayList();
    private int currentSelectedItemIndex;
    private final SuspendableNo updatingToolbar = new SuspendableNo();
    private StyledTextArea<ParStyle, TextStyle> cyrrentFocusTextArea;
    private RectangleSelection rectangleSelection;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        zoomProperty = new SimpleDoubleProperty(100);
        imageView.preserveRatioProperty().set(true);

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

                if(rectangleSelection != null){
                    System.out.println("Image fit height: " + imageView.getFitHeight());
                    System.out.println("Image fit width: " + imageView.getFitWidth());
                    System.out.println("Image fit height prop: " + imageView.fitHeightProperty());
                    System.out.println("Image fit width prop: " + imageView.fitWidthProperty());
                    System.out.println("Image width: " + (int)imageView.getBoundsInParent().getWidth());
                    System.out.println("Image height: " + (int)imageView.getBoundsInParent().getHeight());

                    rectangleSelection.setZoomValue(zoomProperty.getValue());
                }
            }
        });

        imageScrollPane.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                imageView.setFitWidth(newSceneWidth.doubleValue());
                zoomProperty.set(100);

                if(rectangleSelection != null){
                    rectangleSelection.setZoomValue(zoomProperty.getValue());
                }
            }
        });

        imageScrollPane.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                imageView.setFitHeight(newSceneHeight.doubleValue());
                zoomProperty.set(100);

                if(rectangleSelection != null){
                    rectangleSelection.setZoomValue(zoomProperty.getValue());
                }
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
        showImagesListButton.toFront();

        //    textArea.setWrapText(true);

        //   ArrayList<TextArea> list = new ArrayList();

        //           textEditorScrollGroup = new Group();
        //          VBox v = new VBox();

        //   for (int i = 0; i < 10; i++) {
        //       TextArea a = new TextArea();
        //       a.setMinHeight(Paper.A4.getHeight());
        //       a.setMaxHeight(Paper.A4.getHeight());
        //       a.setMinWidth(Paper.A4.getWidth());
        //      a.setMaxWidth(Paper.A4.getWidth());
        //      list.add(a);
        //     v.getChildren().add(a);

        // }

        //  textEditorScrollGroup.getChildren().add(v);

        // textZoomPane = createZoomPane(textEditorScrollGroup);
        // textVBox.getChildren().add(textZoomPane);

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
                SaveManager.getInstance().saveToDOCX(loadedItemList.get(0).getPagesList().get(0).getPage().getDocument());
                break;
            case "SaveAsPdfMenuItem":
                System.out.print("PDF");
                SaveManager.getInstance().saveToPDF(loadedItemList.get(0).getPagesList().get(0).getPage().getDocument());
                break;
            case "ExitMenuItem":
                System.out.print("EXIT");
                if(rectangleSelection != null) {
                    rectangleSelection.crop();
                }
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
               List<HocrElement> list = HocrParser.createAst(result);
                List<Page> pages = HocrParser.parse(result);

                System.out.println("");
                List<Line> p = pages.get(0).getAllLines();
                List<Word> a = p.get(0).getWords();
                List<String> textLines = pages.get(0).getAllLinesAsStrings();

                for (String string : textLines) {
                    System.out.print(string);

                    loadedItemList.get(currentSelectedItemIndex).getPagesList().get(0).getPage().appendText(string);
                }

                String string = loadedItemList.get(currentSelectedItemIndex).getPagesList().get(0).getPage().getText();

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
            case "cropImageButton":
                rectangleSelection = new RectangleSelection(imageGroup);
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
        showImagesListButton.toFront();
    }

    @FXML
    private void handleHideImagesListButton(ActionEvent event) {
        borderPaneLeft.setMaxWidth(4);
        borderPaneLeft.setMinWidth(4);

        hideImagesListButton.setVisible(false);
        showImagesListButton.setVisible(true);
        showImagesListButton.toFront();
    }

    @FXML
    private void handleListMouseClick(MouseEvent arg) {
        imageView.setImage(loadedItemList.get(imagesListView.getSelectionModel().getSelectedIndex()).getImage());
        currentSelectedItemIndex = imagesListView.getSelectionModel().getSelectedIndex();
        imageSizeLabel.setText("" + loadedItemList.get(imagesListView.getSelectionModel().getSelectedIndex()).getImage().getHeight() + " x " + loadedItemList.get(imagesListView.getSelectionModel().getSelectedIndex()).getImage().getWidth());
        refreshTextEditorPane();
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

    public void refreshTextEditorPane() {
        textVBox.getChildren().remove(textZoomPane);

        textEditorScrollGroup = new Group();
        VBox v = new VBox();
        v.setSpacing(5);

        for (int i = 0; i < loadedItemList.get(currentSelectedItemIndex).getPagesList().size(); i++) {
            v.getChildren().add(loadedItemList.get(currentSelectedItemIndex).getPagesList().get(i).getPage());
        }

        textEditorScrollGroup.getChildren().add(v);
        textZoomPane = createZoomPane(textEditorScrollGroup);

        textVBox.getChildren().add(textZoomPane);
    }

    public void setCurrentFocusTextArea(StyledTextArea<ParStyle, TextStyle> area) {
        this.cyrrentFocusTextArea = area;
    }

    public StyledTextArea<ParStyle, TextStyle> getCyrrentFocusTextArea() {
        return this.cyrrentFocusTextArea;
    }

    public EasyReaderItem getCurrentEasyReaderItem() {
        return this.loadedItemList.get(currentSelectedItemIndex);
    }

    public void toggleBold() {
        updateStyleInSelection(spans -> TextStyle.bold(!spans.styleStream().allMatch(style -> style.bold.orElse(false))));
        System.out.println(Paper.A4.getHeight());
        System.out.println(cyrrentFocusTextArea.getTotalHeightEstimate());
        System.out.println(cyrrentFocusTextArea.getParagraph(cyrrentFocusTextArea.getParagraphs().size() - 1).getText());

    }

    public void toggleItalic() {
        updateStyleInSelection(spans -> TextStyle.italic(!spans.styleStream().allMatch(style -> style.italic.orElse(false))));
    }

    public void toggleUnderline() {
        updateStyleInSelection(spans -> TextStyle.underline(!spans.styleStream().allMatch(style -> style.underline.orElse(false))));
    }

    public void toggleStrikethrough() {
        updateStyleInSelection(spans -> TextStyle.strikethrough(!spans.styleStream().allMatch(style -> style.strikethrough.orElse(false))));
    }

    public void alignLeft() {
        updateParagraphStyleInSelection(ParStyle.alignLeft());
    }

    public void alignCenter() {
        updateParagraphStyleInSelection(ParStyle.alignCenter());
    }

    public void alignRight() {
        updateParagraphStyleInSelection(ParStyle.alignRight());
    }

    public void alignJustify() {
        updateParagraphStyleInSelection(ParStyle.alignJustify());
    }

    private void updateStyleInSelection(Function<StyleSpans<TextStyle>, TextStyle> mixinGetter) {
        IndexRange selection = cyrrentFocusTextArea.getSelection();
        if (selection.getLength() != 0) {
            StyleSpans<TextStyle> styles = cyrrentFocusTextArea.getStyleSpans(selection);
            TextStyle mixin = mixinGetter.apply(styles);
            StyleSpans<TextStyle> newStyles = styles.mapStyles(style -> style.updateWith(mixin));
            cyrrentFocusTextArea.setStyleSpans(selection.getStart(), newStyles);
        }
    }

    private void updateStyleInSelection(TextStyle mixin) {
        IndexRange selection = cyrrentFocusTextArea.getSelection();
        if (selection.getLength() != 0) {
            StyleSpans<TextStyle> styles = cyrrentFocusTextArea.getStyleSpans(selection);
            StyleSpans<TextStyle> newStyles = styles.mapStyles(style -> style.updateWith(mixin));
            cyrrentFocusTextArea.setStyleSpans(selection.getStart(), newStyles);
        }
    }

    private void updateParagraphStyleInSelection(Function<ParStyle, ParStyle> updater) {
        IndexRange selection = cyrrentFocusTextArea.getSelection();
        int startPar = cyrrentFocusTextArea.offsetToPosition(selection.getStart(), Forward).getMajor();
        int endPar = cyrrentFocusTextArea.offsetToPosition(selection.getEnd(), Backward).getMajor();
        for (int i = startPar; i <= endPar; ++i) {
            Paragraph<ParStyle, TextStyle> paragraph = cyrrentFocusTextArea.getParagraph(i);
            cyrrentFocusTextArea.setParagraphStyle(i, updater.apply(paragraph.getParagraphStyle()));
        }
    }

    private void updateParagraphStyleInSelection(ParStyle mixin) {
        updateParagraphStyleInSelection(style -> style.updateWith(mixin));
    }

    private void updateFontSize(Integer size) {
        if (!updatingToolbar.get()) {
            updateStyleInSelection(TextStyle.fontSize(size));
        }
    }

    private void updateFontFamily(String family) {
        if (!updatingToolbar.get()) {
            updateStyleInSelection(TextStyle.fontFamily(family));
        }
    }

    private void updateTextColor(Color color) {
        if (!updatingToolbar.get()) {
            updateStyleInSelection(TextStyle.textColor(color));
        }
    }

    private void updateBackgroundColor(Color color) {
        if (!updatingToolbar.get()) {
            updateStyleInSelection(TextStyle.backgroundColor(color));
        }
    }

    public void addListenersForArea(StyledTextArea<ParStyle, TextStyle> area) {
        area.beingUpdatedProperty().addListener((o, old, beingUpdated) -> {
            if (!beingUpdated) {
                boolean bold, italic, underline, strike;
                Integer fontSize;
                String fontFamily;
                Color textColor;
                Color backgroundColor;

                IndexRange selection = area.getSelection();
                if (selection.getLength() != 0) {
                    StyleSpans<TextStyle> styles = area.getStyleSpans(selection);
                    bold = styles.styleStream().anyMatch(s -> s.bold.orElse(false));
                    italic = styles.styleStream().anyMatch(s -> s.italic.orElse(false));
                    underline = styles.styleStream().anyMatch(s -> s.underline.orElse(false));
                    strike = styles.styleStream().anyMatch(s -> s.strikethrough.orElse(false));
                    int[] sizes = styles.styleStream().mapToInt(s -> s.fontSize.orElse(-1)).distinct().toArray();
                    fontSize = sizes.length == 1 ? sizes[0] : -1;
                    String[] families = styles.styleStream().map(s -> s.fontFamily.orElse(null)).distinct().toArray(String[]::new);
                    fontFamily = families.length == 1 ? families[0] : null;
                    Color[] colors = styles.styleStream().map(s -> s.textColor.orElse(null)).distinct().toArray(Color[]::new);
                    textColor = colors.length == 1 ? colors[0] : null;
                    Color[] backgrounds = styles.styleStream().map(s -> s.backgroundColor.orElse(null)).distinct().toArray(i -> new Color[i]);
                    backgroundColor = backgrounds.length == 1 ? backgrounds[0] : null;
                } else {
                    int p = area.getCurrentParagraph();
                    int col = area.getCaretColumn();
                    TextStyle style = area.getStyleAtPosition(p, col);
                    bold = style.bold.orElse(false);
                    italic = style.italic.orElse(false);
                    underline = style.underline.orElse(false);
                    strike = style.strikethrough.orElse(false);
                    fontSize = style.fontSize.orElse(-1);
                    fontFamily = style.fontFamily.orElse(null);
                    textColor = style.textColor.orElse(null);
                    backgroundColor = style.backgroundColor.orElse(null);
                }

                int startPar = area.offsetToPosition(selection.getStart(), Forward).getMajor();
                int endPar = area.offsetToPosition(selection.getEnd(), Backward).getMajor();
                List<Paragraph<ParStyle, TextStyle>> pars = area.getParagraphs().subList(startPar, endPar + 1);

                @SuppressWarnings("unchecked")
                Optional<TextAlignment>[] alignments = pars.stream().map(p -> p.getParagraphStyle().alignment).distinct().toArray(Optional[]::new);
                Optional<TextAlignment> alignment = alignments.length == 1 ? alignments[0] : Optional.empty();

                @SuppressWarnings("unchecked")
                Optional<Color>[] paragraphBackgrounds = pars.stream().map(p -> p.getParagraphStyle().backgroundColor).distinct().toArray(Optional[]::new);
                Optional<Color> paragraphBackground = paragraphBackgrounds.length == 1 ? paragraphBackgrounds[0] : Optional.empty();

                updatingToolbar.suspendWhile(() -> {
                    if (bold) {
                        if (!boldTextButton.getStyleClass().contains("pressed")) {
                            boldTextButton.getStyleClass().add("pressed");
                        }
                    } else {
                        boldTextButton.getStyleClass().remove("pressed");
                    }

                    if (italic) {
                        if (!italicTextButton.getStyleClass().contains("pressed")) {
                            italicTextButton.getStyleClass().add("pressed");
                        }
                    } else {
                        italicTextButton.getStyleClass().remove("pressed");
                    }

                    if (underline) {
                        if (!underlineTextButton.getStyleClass().contains("pressed")) {
                            underlineTextButton.getStyleClass().add("pressed");
                        }
                    } else {
                        underlineTextButton.getStyleClass().remove("pressed");
                    }

                    if (strike) {
                        if (!strikethroughTextButton.getStyleClass().contains("pressed")) {
                            strikethroughTextButton.getStyleClass().add("pressed");
                        }
                    } else {
                        strikethroughTextButton.getStyleClass().remove("pressed");
                    }

                    if (alignment.isPresent()) {
                        TextAlignment al = alignment.get();
                        switch (al) {
                            case LEFT:
                                alginGroup.selectToggle(alginLeftTextButton);
                                break;
                            case CENTER:
                                alginGroup.selectToggle(alginCenterTextButton);
                                break;
                            case RIGHT:
                                alginGroup.selectToggle(alginRightTextButton);
                                break;
                            case JUSTIFY:
                                alginGroup.selectToggle(alginJustifyTextButton);
                                break;
                        }
                    } else {
                        alginGroup.selectToggle(null);
                    }

                    //paragraphBackgroundPicker.setValue(paragraphBackground.orElse(null));

                    if (fontSize != -1) {
                        textSizeComboBox.getSelectionModel().select(fontSize);
                    } else {
                        textSizeComboBox.getSelectionModel().clearSelection();
                    }

                    if (fontFamily != null) {
                        textFontComboBox.getSelectionModel().select(fontFamily);
                    } else {
                        textFontComboBox.getSelectionModel().clearSelection();
                    }

                    if (textColor != null) {
                        textColorColorPicker.setValue(textColor);
                    }

                    textBackgroundColorColorPicker.setValue(backgroundColor);
                });
            }
        });
    }
}
