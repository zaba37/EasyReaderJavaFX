package com.zaba37.easyreader.models;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.textEditor.ParStyle;
import com.zaba37.easyreader.textEditor.TextStyle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.print.Paper;
import javafx.scene.control.IndexRange;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.EditableStyledDocument;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyledDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Created by zaba3 on 05.10.2016.
 */
public class Page {

    private StyledTextArea<ParStyle, TextStyle> page;
    private int indexPage;

//    public Page() {
//        page = new StyledTextArea<>(
//                ParStyle.EMPTY, (paragraph, style) -> paragraph.setStyle(style.toCss()),
//                TextStyle.EMPTY.updateFontSize(12).updateFontFamily("Serif").updateTextColor(Color.BLACK),
//                (text, style) -> text.setStyle(style.toCss()));
//
//        page.setWrapText(true);
//        page.setStyleCodecs(ParStyle.CODEC, TextStyle.CODEC);
//
//     //   page.setMinHeight(Paper.A4.getHeight());
//        //page.setMaxHeight(Paper.A4.getHeight());
//     //  page.setMinWidth(Paper.A4.getWidth());
//     //   page.setMaxWidth(Paper.A4.getWidth());
////        page.setMinHeight(Utils.getMainWindowController().getSceneHeight());
////        page.setMinWidth(Utils.getMainWindowController().getSceneWidth());
//
//        page.focusedProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue) {
//                Utils.getMainWindowController().setCurrentFocusTextArea(page);
//            }
//        });
//
//        page.widthProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
//                page.setMinWidth(newSceneWidth.doubleValue());
//            }
//        });
//
//        page.heightProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
//                page.setMinHeight(newSceneHeight.doubleValue());
//            }
//        });
//
//
//        page.textProperty().addListener((observable, oldValue, newValue) -> {
//            synchronized (page) {
//                StyledTextArea<ParStyle, TextStyle> currentArea = Utils.getMainWindowController().getCyrrentFocusTextArea();
//                EasyReaderItem currentItem = Utils.getMainWindowController().getCurrentEasyReaderItem();
//                ArrayList<Page> currentPagesList = currentItem.getPagesList();
//
//                try {
//                    double a = Paper.A4.getHeight();
//                    double ab = page.getTotalHeightEstimate();
//                    double c = Paper.A4.getWidth();
//                    double d = page.getTotalWidthEstimate();
//                    if (page.getTotalHeightEstimate() > Paper.A4.getHeight() && page.getTotalWidthEstimate() < Paper.A4.getWidth()) {
//                        StyledTextArea<ParStyle, TextStyle> nextArea;
//
//                        if (this.getIndexPage() == currentPagesList.size() - 1) {
//                            System.out.println("new Page");
//
//                            //HERE ADD NEW PAGE TO LIST IN CURRENT SELECTED ITEM
//                            currentItem.addPage();
//
//                            //REFRESH PAGES LIST
//                            currentPagesList = currentItem.getPagesList();
//
//                            Utils.getMainWindowController().refreshTextEditorPane();
//
//                            nextArea = currentPagesList.get(currentPagesList.size() - 1).getPage();
//                        } else {
//                            nextArea = currentPagesList.get(getIndexPage() + 1).getPage();
//                        }
//
//                        if (page.getTotalHeightEstimate() > Paper.A4.getHeight()) {
//                            try {
//                                List<Paragraph<ParStyle, TextStyle>> paragraphsInCurrentPage = page.getDocument().getParagraphs();
//                                List<Paragraph<ParStyle, TextStyle>> paragraphsInNextPage = nextArea.getDocument().getParagraphs();
//
//                                if (paragraphsInCurrentPage.get(paragraphsInCurrentPage.size() - 1).length() != 0) {
//                                    StyledDocument<ParStyle, TextStyle> lastParagraphCurrentPage = page.subDocument(page.getParagraphs().size() - 1);
//                                    StyledDocument<ParStyle, TextStyle> documentFromNextPage = nextArea.getDocument();
//                                    StyledDocument<ParStyle, TextStyle> lastWordFromCurrentPage = lastParagraphCurrentPage.subSequence(new IndexRange(getLastSpaceIndex(lastParagraphCurrentPage.getText()),
//                                            lastParagraphCurrentPage.length()));
//
//                                    int aa = page.getText().length() - lastWordFromCurrentPage.length();
//                                    int bb = page.getText().length();
//
//                                    page.deleteText(page.getText().length() - lastWordFromCurrentPage.length(), page.getText().length());
//
//                                    nextArea.deleteText(1, nextArea.getText().length());
//                                    nextArea.append(lastWordFromCurrentPage);
//                                    nextArea.append(documentFromNextPage);
//                                    nextArea.deleteText(0, 1);
//                                } else {
//                                    page.deleteText(page.getText().length() - 1, page.getText().length());
//                                }
//
//
//                            } catch (Exception e) {
//                                //e.printStackTrace();
//                            }
//                        }
//
//                        Utils.getMainWindowController().refreshTextEditorPane();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
////            if (page.getText().length() == 0 && currentPagesList.indexOf(page) != 0) {
////                System.out.println("remove Page");
////                //HERE REMOVE PAGE FROM LIST IN CURRENT SELECTED ITEM
////                int indexToRemove = 1;
////
////                for (int i = 0; i < currentItem.getPagesList().size(); i++) {
////                    if (currentItem.getPagesList().get(i).getPage().getText().isEmpty()) {
////                        indexToRemove = i;
////                        break;
////                    }
////                }
////
////                currentItem.getPagesList().remove(indexToRemove);
////                Utils.getMainWindowController().refreshTextEditorPane();
////            }
//            }
//        });
//
//        Utils.getMainWindowController().addListenersForArea(page);
//    }
//
//    public void setIndexPage(int index) {
//        this.indexPage = index;
//    }
//
//    public int getIndexPage() {
//        return this.indexPage;
//    }
//
//    public StyledTextArea<ParStyle, TextStyle> getPage() {
//        return page;
//    }
//
//    private int getLastSpaceIndex(String text) {
//        int index;
//
//        index = text.lastIndexOf(" ");
//
//        if (index != -1) {
//            return index;
//        }
//
//        return 0;
//    }
//
//    private boolean checkPageIsEmpty(String text){
//        if(text.length() == 0){
//            return true;
//        }
//
//        return false;
//    }
}
