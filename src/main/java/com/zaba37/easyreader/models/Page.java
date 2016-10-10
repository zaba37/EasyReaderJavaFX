package com.zaba37.easyreader.models;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.textEditor.ParStyle;
import com.zaba37.easyreader.textEditor.TextStyle;
import javafx.print.Paper;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.EditableStyledDocument;

import java.util.ArrayList;
import java.util.function.BiConsumer;

/**
 * Created by zaba3 on 05.10.2016.
 */
public class Page {

    private StyledTextArea<ParStyle, TextStyle> page;

    public Page() {
        page = new StyledTextArea<>(
                ParStyle.EMPTY, (paragraph, style) -> paragraph.setStyle(style.toCss()),
                TextStyle.EMPTY.updateFontSize(12).updateFontFamily("Serif").updateTextColor(Color.BLACK),
                (text, style) -> text.setStyle(style.toCss()));

        page.setWrapText(true);
        page.setStyleCodecs(ParStyle.CODEC, TextStyle.CODEC);


        page.setMinHeight(Paper.A4.getHeight());
        page.setMaxHeight(Paper.A4.getHeight());
        page.setMinWidth(Paper.A4.getWidth());
        page.setMaxWidth(Paper.A4.getWidth());

        page.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Utils.getMainWindowController().setCurrentFocusTextArea(page);
            }
        });

        page.textProperty().addListener((observable, oldValue, newValue) -> {
            StyledTextArea<ParStyle, TextStyle> currentArea = Utils.getMainWindowController().getCyrrentFocusTextArea();
            EasyReaderItem currentItem = Utils.getMainWindowController().getCurrentEasyReaderItem();
            ArrayList<Page> currentPagesList = currentItem.getPagesList();

            try {
                if (page.getTotalHeightEstimate() > Paper.A4.getHeight()) {
                    System.out.println("new Page");

                    //HERE ADD NIE PAGE TO LIST IN CURRENT SELECTED ITEM
                    currentItem.addPage();

                    //REFRESH PAGES LIST
                    currentPagesList = currentItem.getPagesList();

                    Utils.getMainWindowController().refreshTextEditorPane();

                    StyledTextArea<ParStyle, TextStyle> newArea = currentPagesList.get(currentPagesList.size() - 1).getPage();

                    while (page.getTotalHeightEstimate() > Paper.A4.getHeight()) {
                        try {
                            String s = page.getParagraph(page.getParagraphs().size() - 2).getText();
                            newArea.appendText(s);

                            String asdasd = newArea.getText();

                            System.out.println(page.getParagraph(page.getParagraphs().size() - 2).length());
                            page.deleteText(page.getParagraphs().size() - 2, 0, page.getParagraphs().size() - 1, page.getParagraph(page.getParagraphs().size() - 1).length());
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }

                    Utils.getMainWindowController().refreshTextEditorPane();
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }

            if (page.getText().isEmpty() && currentPagesList.indexOf(page) != 0) {
                System.out.println("remove Page");
                //HERE REMOVE PAGE FROM LIST IN CURRENT SELECTED ITEM
                int indexToRemove = 1;

                for(int i = 0; i < currentItem.getPagesList().size(); i++){
                    if(currentItem.getPagesList().get(i).getPage().getText().isEmpty()) {
                        indexToRemove = i;
                        break;
                    }
                }

                currentItem.getPagesList().remove(indexToRemove);
                Utils.getMainWindowController().refreshTextEditorPane();
            }
        });

        Utils.getMainWindowController().addListenersForArea(page);
    }

    public StyledTextArea<ParStyle, TextStyle> getPage() {
        return page;
    }
}
