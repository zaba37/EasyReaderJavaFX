/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zaba37.easyreader.ocr;

import com.zaba37.easyreader.Utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import net.sourceforge.tess4j.*;

import javax.imageio.ImageIO;

public class OcrEngine {

    private static OcrEngine instance = null;
    private Tesseract1 tesseract;
    private Preferences preferences;

    private OcrEngine() {
        tesseract = new Tesseract1();
       //tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_TESSERACT_CUBE_COMBINED);

        tesseract.setHocr(true);
        tesseract.setTessVariable("hocr_font_info", "1");
        tesseract.setTessVariable("pitsync_linear_version", "6");
        tesseract.setTessVariable("tessedit_pageseg_mode", "1");

        preferences = Preferences.userRoot().node(Utils.KEY_PREFERENCES);
    }

    public static OcrEngine getInstance() {
        if (instance == null) {
            instance = new OcrEngine();
        }

        return instance;
    }

    public void setDataPath(String path) {
        tesseract.setDatapath(path);
    }

    public void setLanguage(String languageKey) {
        tesseract.setLanguage(languageKey);
    }

    public String getOcrResult(File file) {
        String result = "";

        if (!preferences.get(Utils.KEY_LANGUAGE_DATA_PATH, "").isEmpty() && !preferences.get(Utils.KEY_SELECTED_OCR_LANGUAGE_KEY_NAME, "").isEmpty()) {
            
            setDataPath(preferences.get(Utils.KEY_LANGUAGE_DATA_PATH, ""));
            setLanguage(preferences.get(Utils.KEY_SELECTED_OCR_LANGUAGE_KEY_NAME, ""));
            
            try {

             //   List<Word> li = tesseract.getWords(ImageIO.read(file), 3);

//                HashMap<Integer, ArrayList<Word>> kupalist = new HashMap<>();
//
//                for(Word word : li){
//                    if(kupalist.containsKey(word.getBoundingBox().y)){
//                        kupalist.get(word.getBoundingBox().y).add(word);
//                    }else{
//                        ArrayList<Word> a = new ArrayList<>();
//                        a.add(word);
//                        kupalist.put(word.getBoundingBox().y, a);
//                    }
//                }

                result = tesseract.doOCR(file);
            } catch (TesseractException e) {
                e.printStackTrace();
            }

        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Alert alert = new Alert(AlertType.WARNING);

                    alert.setTitle("Langauge data");
                    alert.setHeaderText("Choose language data and language for ocr.");
                    alert.setContentText("Go to OCR -> OCR Settings -> Language.");
                    alert.showAndWait();
                }
            });
        }

        System.out.println(result);

        return result;
    }
}
