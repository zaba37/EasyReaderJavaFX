package com.zaba37.easyreader.textEditor;

import com.zaba37.easyreader.Utils;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
import java.sql.Timestamp;

import java.util.List;

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

    public void saveToDOCX(org.fxmisc.richtext.model.StyledDocument styledDocument) {
        FileChooser chooser = new FileChooser();
        File file = chooser.showSaveDialog(Utils.getMainStage());
        FileOutputStream out;
        XWPFDocument document = wordFileCreator(styledDocument);

        try {
            out = new FileOutputStream(file);
            document.write(out);
            out.close();
            System.out.println(file.getAbsolutePath() + " written successfully");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToPDF(org.fxmisc.richtext.model.StyledDocument styledDocument) {
        FileChooser chooser = new FileChooser();
        File file = chooser.showSaveDialog(Utils.getMainStage());
        FileOutputStream out = null;
        FileInputStream in = null;
        File tmpFile = null;
        XWPFDocument document = wordFileCreator(styledDocument);
        String tmpFileName = "tmp.docx";


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

    private XWPFDocument wordFileCreator(org.fxmisc.richtext.model.StyledDocument styledDocument) {
        List<Paragraph> paragraphs = styledDocument.getParagraphs();
        XWPFDocument document = new XWPFDocument();

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
