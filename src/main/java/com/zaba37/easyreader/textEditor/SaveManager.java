package com.zaba37.easyreader.textEditor;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.models.EasyReaderItem;
import javafx.stage.FileChooser;
import org.apache.poi.xwpf.usermodel.*;
import org.docx4j.Docx4J;
import org.docx4j.model.fields.FieldUpdater;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyledText;

import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.docx4j.convert.out.*;


/**
 * Created by zaba3 on 15.10.2016.
 */
public class SaveManager {
    private static SaveManager instance;

    public static SaveManager getInstance() {

        if (instance == null) {
            instance = new SaveManager();
        }

        return instance;
    }

    private SaveManager() {
    }

    public void saveToTXT(ArrayList<EasyReaderItem> items){
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        File file;

        chooser.getExtensionFilters().add(extFilter);
        file = chooser.showSaveDialog(Utils.getMainStage());

        try {
            FileWriter fileWriter = null;

            fileWriter = new FileWriter(file);

            //fileWriter.write(styledDocument.getText() + "\n");

            for(EasyReaderItem item : items){
                for(Paragraph paragraph : item.getTextArea().getDocument().getParagraphs()){
                    fileWriter.write(paragraph.getText() +"\n");
                }
            }

            fileWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void saveToHtml(ArrayList<EasyReaderItem> items){
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HTML files (*.html)", "*.html");
        File file;

        chooser.getExtensionFilters().add(extFilter);
        file = chooser.showSaveDialog(Utils.getMainStage());

        try {

            FileWriter fileWriter = null;

            fileWriter = new FileWriter(file);

            for(EasyReaderItem item : items){
                fileWriter.write(item.gethOCR());
            }

            fileWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void saveToDOC(ArrayList<EasyReaderItem> items) {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("DOC files (*.doc)", "*.doc");
        File file;
        FileOutputStream out;
        XWPFDocument document = wordFileCreator(items);

        chooser.getExtensionFilters().add(extFilter);
        file = chooser.showSaveDialog(Utils.getMainStage());

        try {
            out = new FileOutputStream(file);
            document.write(out);
            out.close();
            System.out.println(file.getAbsolutePath() + " written successfully");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToDOCX(ArrayList<EasyReaderItem> items) {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("DOCX files (*.docx)", "*.docx");
        File file;
        FileOutputStream out;
        XWPFDocument document = wordFileCreator(items);

        chooser.getExtensionFilters().add(extFilter);
        file = chooser.showSaveDialog(Utils.getMainStage());

        try {
            out = new FileOutputStream(file);
            document.write(out);
            out.close();
            System.out.println(file.getAbsolutePath() + " written successfully");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToPDF(ArrayList<EasyReaderItem> items) {
        FileChooser chooser = new FileChooser();
        File file;
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        FileOutputStream out = null;
        FileInputStream in = null;
        File tmpFile = null;
        XWPFDocument document = wordFileCreator(items);
        String tmpFileName = "tmp.docx";

        chooser.getExtensionFilters().add(extFilter);
        file = chooser.showSaveDialog(Utils.getMainStage());

        try {
            tmpFile = new File(tmpFileName);

            out = new FileOutputStream(tmpFile);

            document.write(out);

            out.close();

            System.out.println(file.getAbsolutePath() + " written successfully");

            in = new FileInputStream(new File(tmpFileName));

            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(in);

            FieldUpdater updater = new FieldUpdater(wordMLPackage);
            updater.update(true);

            out = new FileOutputStream(file);

            if (!Docx4J.pdfViaFO()) {
                System.out.println("Using Plutext's PDF Converter; add docx4j-export-fo if you don't want that");

                Docx4J.toPDF(wordMLPackage, out);
                System.out.println("Saved: " + file.getAbsolutePath());

                out.close();
                tmpFile.delete();
            }

        } catch (java.io.IOException e) {
            e.printStackTrace();
        } catch (Docx4JException e) {
            e.printStackTrace();
        }
    }

    private XWPFDocument wordFileCreator(ArrayList<EasyReaderItem> items) {

        XWPFDocument document = new XWPFDocument();

        for(EasyReaderItem item : items) {
            List<Paragraph> paragraphs = new ArrayList<>();
            paragraphs.addAll(item.getStyledDocument().getParagraphs());

            for (Paragraph paragraph : paragraphs) {
                ParStyle paragraphStyle = (ParStyle) paragraph.getParagraphStyle();
                XWPFParagraph paragraphDocx = document.createParagraph();
                XWPFRun run = paragraphDocx.createRun();

                for (Object object : paragraph.getSegments()) {
                    StyledText segment = (StyledText) object;
                    TextStyle segmentTextStyle = (TextStyle) segment.getStyle();

                    if (segmentTextStyle.bold.toString().contains("Optional[true]")) {
                        run.setBold(true);
                    }

                    if (segmentTextStyle.italic.toString().contains("Optional[true]")) {
                        run.setItalic(true);
                    }

                    if (segmentTextStyle.underline.toString().contains("Optional[true]")) {
                        run.setUnderline(UnderlinePatterns.SINGLE);
                    }

                    if (segmentTextStyle.strikethrough.toString().contains("Optional[true]")) {
                        run.setStrikeThrough(true);
                    }

                    if (!segmentTextStyle.backgroundColor.toString().contains("Optional.empty")) {
                        java.awt.Color awtColor;
                        javafx.scene.paint.Color currentColor = segmentTextStyle.backgroundColor.get();

                        awtColor = new Color((float) currentColor.getRed(), (float) currentColor.getGreen(), (float) currentColor.getBlue(), (float) currentColor.getOpacity());

                    }

                    if (!segmentTextStyle.textColor.toString().contains("Optional.empty")) {
                        java.awt.Color awtColor;
                        javafx.scene.paint.Color currentColor = segmentTextStyle.textColor.get();

                        awtColor = new Color((float) currentColor.getRed(), (float) currentColor.getGreen(), (float) currentColor.getBlue(), (float) currentColor.getOpacity());

                        //run.setColor(String.valueOf(awtColor.getRGB()));
                    }

                    run.setFontFamily(segmentTextStyle.fontFamily.get());
                    run.setFontSize(segmentTextStyle.fontSize.get());

                    run.setText(segment.getText());
                }

                switch (paragraphStyle.alignment.toString()) {
                    case "Optional[CENTER]":
                        paragraphDocx.setAlignment(ParagraphAlignment.CENTER);
                        break;
                    case "Optional[LEFT]":
                        paragraphDocx.setAlignment(ParagraphAlignment.LEFT);
                        break;
                    case "Optional[RIGHT]":
                        paragraphDocx.setAlignment(ParagraphAlignment.RIGHT);
                        break;
                    case "Optional[JUSTIFY]":
                        paragraphDocx.setAlignment(ParagraphAlignment.BOTH);
                        break;
                }
            }
        }
        return document;
    }

    private Style createStyleForChar(ParStyle parStyle, TextStyle textStyle, Style style) {

        switch (parStyle.alignment.toString()) {
            case "Optional[CENTER]":
                style.addAttribute(StyleConstants.ALIGN_CENTER, true);
                break;
            case "Optional[LEFT]":
                style.addAttribute(StyleConstants.ALIGN_LEFT, true);
                break;
            case "Optional[RIGHT]":
                style.addAttribute(StyleConstants.ALIGN_RIGHT, true);
                break;
            case "Optional[JUSTIFY]":
                style.addAttribute(StyleConstants.ALIGN_JUSTIFIED, true);
                break;
        }

        if (textStyle.bold.toString().contains("Optional[true]")) {
            style.addAttribute(StyleConstants.Bold, true);
        }

        if (textStyle.italic.toString().contains("Optional[true]")) {
            style.addAttribute(StyleConstants.Italic, true);
        }

        if (textStyle.underline.toString().contains("Optional[true]")) {
            style.addAttribute(StyleConstants.Underline, true);
        }

        if (textStyle.strikethrough.toString().contains("Optional[true]")) {
            style.addAttribute(StyleConstants.StrikeThrough, true);
        }

        if (!textStyle.backgroundColor.toString().contains("Optional.empty")) {
            java.awt.Color awtColor;
            javafx.scene.paint.Color currentColor = textStyle.backgroundColor.get();

            awtColor = new Color((float) currentColor.getRed(), (float) currentColor.getGreen(), (float) currentColor.getBlue(), (float) currentColor.getOpacity());

            style.addAttribute(StyleConstants.Background, awtColor);
        }

        if (!textStyle.textColor.toString().contains("Optional.empty")) {
            java.awt.Color awtColor;
            javafx.scene.paint.Color currentColor = textStyle.textColor.get();

            awtColor = new Color((float) currentColor.getRed(), (float) currentColor.getGreen(), (float) currentColor.getBlue(), (float) currentColor.getOpacity());

            style.addAttribute(StyleConstants.Foreground, awtColor);
        }

        style.addAttribute(StyleConstants.FontSize, textStyle.fontSize.get());
        style.addAttribute(StyleConstants.FontFamily, textStyle.fontFamily.get());

        return style;
    }
}
