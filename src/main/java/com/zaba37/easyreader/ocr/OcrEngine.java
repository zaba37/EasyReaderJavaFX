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

        setupTesseractVariables();

        if (!preferences.get(Utils.KEY_LANGUAGE_DATA_PATH, "").isEmpty() && !preferences.get(Utils.KEY_SELECTED_OCR_LANGUAGE_KEY_NAME, "").isEmpty()) {
            
            setDataPath(preferences.get(Utils.KEY_LANGUAGE_DATA_PATH, ""));
            setLanguage(preferences.get(Utils.KEY_SELECTED_OCR_LANGUAGE_KEY_NAME, ""));
            
            try {
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

    private void setupTesseractVariables(){
        tesseract.setHocr(true);
        tesseract.setTessVariable("hocr_font_info", "1");

        switch (preferences.getInt(Utils.KEY_OCR_ENGINE_MODE, 0)){
            case 0:
                tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_TESSERACT_ONLY);
                break;
            case 1:
                tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_CUBE_ONLY);
                break;
            case 2:
                tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_TESSERACT_CUBE_COMBINED);
                break;
            case 3:
                tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_DEFAULT);
                break;
        }

        switch (preferences.getInt(Utils.KEY_OCR_PAGE_SEGMENTATION_MODE, 0)){
            case 0:
                tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_OSD_ONLY);
                break;
            case 1:
                tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO_OSD);
                break;
            case 2:
                tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO_ONLY);
                break;
            case 3:
                tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO);
                break;
            case 4:
                tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_COLUMN);
                break;
            case 5:
                tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_BLOCK_VERT_TEXT);
                break;
            case 6:
                tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_BLOCK);
                break;
            case 7:
                tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_LINE);
                break;
            case 8:
                tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_WORD);
                break;
            case 9:
                tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_CIRCLE_WORD);
                break;
            case 10:
                tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_CHAR);
                break;
            case 11:
                tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SPARSE_TEXT);
                break;
            case 12:
                tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SPARSE_TEXT_OSD);
                break;
            case 13:
                tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_COUNT);
                break;
        }

        tesseract.setTessVariable("language_model_ngram_scale_factor", String.valueOf(preferences.getDouble(Utils.KEY_NGRAM, 0.03)));
        tesseract.setTessVariable("language_model_penalty_non_dict_word", String.valueOf(preferences.getDouble(Utils.KEY_NON_DICT_WORDS, 0.15)));
        tesseract.setTessVariable("language_model_penalty_punc", String.valueOf(preferences.getDouble(Utils.KEY_PUNCTUATION, 0.2)));
        tesseract.setTessVariable("language_model_penalty_case", String.valueOf(preferences.getDouble(Utils.KEY_CASE, 0.1)));
        tesseract.setTessVariable("language_model_penalty_chartype", String.valueOf(preferences.getDouble(Utils.KEY_CHARACTER_TYPE, 0.3)));
        tesseract.setTessVariable("language_model_penalty_font", String.valueOf(preferences.getDouble(Utils.KEY_FONT, 0.0)));
        tesseract.setTessVariable("language_model_penalty_spacing", String.valueOf(preferences.getDouble(Utils.KEY_SPACING, 0.05)));

    }
}
