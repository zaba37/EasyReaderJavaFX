/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zaba37.easyreader;

import com.zaba37.easyreader.controllers.MainWindowController;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.zaba37.easyreader.models.EasyReaderItem;
import com.zaba37.easyreader.textEditor.ParStyle;
import com.zaba37.easyreader.textEditor.TextStyle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.print.Paper;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.fxmisc.richtext.StyledTextArea;

/**
 *
 * @author zaba3
 */
public class Utils {

    private static Scene mainScene;
    private static Stage mainStage;
    private static MainWindowController mainController;
    private static Window mainWindow;

    public static final String KEY_LANGUAGE_DATA_PATH = "KEY_LANGUAGE_DATA_PATH";
    public static final String KEY_SELECTED_OCR_LANGUAGE_NAME = "KEY_SELECTED_OCR_LANGUAGE_NAME";
    public static final String KEY_SELECTED_OCR_LANGUAGE_KEY_NAME = "KEY_SELECTED_OCR_LANGUAGE_KEY_NAME";      
    public static final String KEY_PREFERENCES = "EasyReaderOCR";
    public static final String KEY_SHOW_INFORMATION_CROP_FUNCTION = "KEY_SHOW_INFORMATION_CROP_FUNCTION";

    public static final Map<String, String> languageDictionary;

    static {
        HashMap<String, String> tmpDictionary = new HashMap();
        
        tmpDictionary.put("afr", "Afrikaans");
        tmpDictionary.put("amh", "Amharic");
        tmpDictionary.put("ara", "Arabic");
        tmpDictionary.put("asm", "Assamese");
        tmpDictionary.put("aze", "Azerbaijani");
        tmpDictionary.put("aze_cyrl", "Azerbaijani – Cyrilic");
        tmpDictionary.put("bel", "Belarusian");
        tmpDictionary.put("ben", "Bengali");
        tmpDictionary.put("bod", "Tibetan");
        tmpDictionary.put("bos", "Bosnian");
        tmpDictionary.put("bul", "Bulgarian");
        tmpDictionary.put("cat", "Catalan");
        tmpDictionary.put("ceb", "Cebuano");
        tmpDictionary.put("ces", "Czech");
        tmpDictionary.put("chi_sim", "Chinese – Simplified");
        tmpDictionary.put("chi_tra", "Chinese – Traditional");
        tmpDictionary.put("chr", "Cherokee");
        tmpDictionary.put("cym", "Welsh");
        tmpDictionary.put("dan", "Danish");
        tmpDictionary.put("dan_frak", "Danish – Fraktur");
        tmpDictionary.put("deu", "German");
        tmpDictionary.put("deu_frak", "German – Fraktur");
        tmpDictionary.put("dzo", "Dzongkha");
        tmpDictionary.put("ell", "Greek");
        tmpDictionary.put("eng", "English");
        tmpDictionary.put("enm", "English, Middle (1100-1500)");
        tmpDictionary.put("epo", "Esperanto");
        tmpDictionary.put("equ", "Math");
        tmpDictionary.put("est", "Estonian");
        tmpDictionary.put("eus", "Basque");
        tmpDictionary.put("fas", "Persian");
        tmpDictionary.put("fin", "Finnish");
        tmpDictionary.put("fra", "French");
        tmpDictionary.put("frk", "Frankish");
        tmpDictionary.put("frm", "French, Middle (ca.1400-1600)");
        tmpDictionary.put("gle", "Irish");
        tmpDictionary.put("glg", "Galician");
        tmpDictionary.put("grc", "Greek");
        tmpDictionary.put("guj", "Gujarati");
        tmpDictionary.put("hat", "Haitian");
        tmpDictionary.put("heb", "Hebrew");
        tmpDictionary.put("hin", "Hindi");
        tmpDictionary.put("hrv", "Croatian");
        tmpDictionary.put("hun", "Hungarian");
        tmpDictionary.put("iku", "Inuktitut");
        tmpDictionary.put("ind", "Indonesian");
        tmpDictionary.put("isl", "Icelandic");
        tmpDictionary.put("ita", "Italian");
        tmpDictionary.put("ita_old", "Italian – Old");
        tmpDictionary.put("jav", "Javanese");
        tmpDictionary.put("jpn", "Japanese");
        tmpDictionary.put("kan", "Kannada");
        tmpDictionary.put("kat", "Georgian");
        tmpDictionary.put("kat_old", "Georgian – Old");
        tmpDictionary.put("kaz", "Kazakh");
        tmpDictionary.put("khm", "Central Khmer");
        tmpDictionary.put("kir", "Kirghiz");
        tmpDictionary.put("kor", "Korean");
        tmpDictionary.put("kur", "Kurdish");
        tmpDictionary.put("lao", "Lao");
        tmpDictionary.put("lat", "Latin");
        tmpDictionary.put("lav", "Latvian");
        tmpDictionary.put("lit", "Lithuanian");
        tmpDictionary.put("mal", "Malayalam");
        tmpDictionary.put("mar", "Marathi");
        tmpDictionary.put("mkd", "Macedonian");
        tmpDictionary.put("mlt", "Maltese");
        tmpDictionary.put("msa", "Malay");
        tmpDictionary.put("mya", "Burmese");
        tmpDictionary.put("nep", "Nepali");
        tmpDictionary.put("nld", "Dutch");
        tmpDictionary.put("nor", "Norwegian");
        tmpDictionary.put("ori", "Oriya");
        tmpDictionary.put("osd", "Orientation and script detection module");
        tmpDictionary.put("pan", "Panjabi");
        tmpDictionary.put("pol", "Polish");
        tmpDictionary.put("por", "Portuguese");
        tmpDictionary.put("pus", "Pushto");
        tmpDictionary.put("ron", "Romanian");
        tmpDictionary.put("rus", "Russian");
        tmpDictionary.put("san", "Sanskrit");
        tmpDictionary.put("sin", "Sinhala");
        tmpDictionary.put("slk", "Slovak");
        tmpDictionary.put("slk_frak", "Slovak – Fraktur");
        tmpDictionary.put("slv", "Slovenian");
        tmpDictionary.put("spa", "Spanish");
        tmpDictionary.put("spa_old", "Spanish – Old");
        tmpDictionary.put("sqi", "Albanian");
        tmpDictionary.put("srp", "Serbian");
        tmpDictionary.put("srp_latn", "Serbian – Latin");
        tmpDictionary.put("swa", "Swahili");
        tmpDictionary.put("swe", "Swedish");
        tmpDictionary.put("syr", "Syriac");
        tmpDictionary.put("tam", "Tamil");
        tmpDictionary.put("tel", "Telugu");
        tmpDictionary.put("tgk", "Tajik");
        tmpDictionary.put("tgl", "Tagalog");
        tmpDictionary.put("tha", "Thai");
        tmpDictionary.put("tir", "Tigrinya");
        tmpDictionary.put("tur", "Turkish");
        tmpDictionary.put("uig", "Uighur");
        tmpDictionary.put("ukr", "Ukrainian");
        tmpDictionary.put("urd", "Urdu");
        tmpDictionary.put("uzb", "Uzbek");
        tmpDictionary.put("uzb_cyrl", "Uzbek – Cyrilic");
        tmpDictionary.put("vie", "Vietnamese");
        tmpDictionary.put("yid", "Yiddish");

        languageDictionary = Collections.unmodifiableMap(tmpDictionary);
    }

    public static void setMainStage(Stage stage) {
        Utils.mainStage = stage;
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void setMainScene(Scene scene) {
        Utils.mainScene = scene;
    }

    public static Scene getMainScene() {
        return mainScene;
    }
    
    public static void setMainWindow(Window window){
        Utils.mainWindow = window;
    }
    
    public static Window getMainWindow(){
        return mainWindow;
    }
    
    public static void setMainWindowController(MainWindowController mainController){
        Utils.mainController = mainController;
    }
    
    public static MainWindowController getMainWindowController(){
        return mainController;
    }
    
    public static ArrayList<String> createLanguageList(ArrayList<String> fileList){
        ArrayList<String> languageList = new ArrayList();
        
        for(String langFileName : fileList){
            if(languageDictionary.containsKey(langFileName)){
                if(languageList.indexOf(languageDictionary.get(langFileName)) == -1){
                    languageList.add(languageDictionary.get(langFileName));
                }
            }
        }
        
        return languageList;
    }
    
    public static String getLanguageName(String keyName){
        return languageDictionary.get(keyName);
    }
    
    public static String getLanguageKeyName(String languageName){
        for(String key : languageDictionary.keySet()){
            if(languageDictionary.get(key).equals(languageName)){
                return key;
            }
        }

        return null;
    }
}
