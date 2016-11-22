package com.zaba37.easyreader;

import com.cybozu.labs.langdetect.util.Util;
import com.zaba37.easyreader.controllers.MainWindowController;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.sourceforge.tess4j.ITessAPI;

import java.util.prefs.Preferences;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {        
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/MainWindowScene.fxml"));  
        Parent root = (Parent)fxmlLoader.load();
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        stage.setTitle("Easy Reader");

        stage.setScene(scene);
        Utils.setMainWindowController(fxmlLoader.<MainWindowController>getController());
        
        stage.show();

        if(Preferences.userRoot().node(Utils.KEY_PREFERENCES).getInt(Utils.KEY_OCR_ENGINE_MODE, -1) == -1){
            Preferences.userRoot().node(Utils.KEY_PREFERENCES).putInt(Utils.KEY_OCR_ENGINE_MODE, ITessAPI.TessOcrEngineMode.OEM_TESSERACT_ONLY);
        }

        if(Preferences.userRoot().node(Utils.KEY_PREFERENCES).getInt(Utils.KEY_OCR_PAGE_SEGMENTATION_MODE, -1) == -1){
            Preferences.userRoot().node(Utils.KEY_PREFERENCES).putInt(Utils.KEY_OCR_PAGE_SEGMENTATION_MODE, ITessAPI.TessPageSegMode.PSM_AUTO_OSD);
        }

        if(Preferences.userRoot().node(Utils.KEY_PREFERENCES).getDouble(Utils.KEY_NON_DICT_WORDS, -1) == -1){
            Preferences.userRoot().node(Utils.KEY_PREFERENCES).putDouble(Utils.KEY_NON_DICT_WORDS, 0.15);
            Preferences.userRoot().node(Utils.KEY_PREFERENCES).putDouble(Utils.KEY_PUNCTUATION, 0.2);
            Preferences.userRoot().node(Utils.KEY_PREFERENCES).putDouble(Utils.KEY_CASE, 0.1);
            Preferences.userRoot().node(Utils.KEY_PREFERENCES).putDouble(Utils.KEY_CHARACTER_TYPE, 0.3);
            Preferences.userRoot().node(Utils.KEY_PREFERENCES).putDouble(Utils.KEY_FONT, 0);
            Preferences.userRoot().node(Utils.KEY_PREFERENCES).putDouble(Utils.KEY_SPACING, 0.05);
            Preferences.userRoot().node(Utils.KEY_PREFERENCES).putDouble(Utils.KEY_NGRAM, 0.03);
        }

        if(Preferences.userRoot().node(Utils.KEY_PREFERENCES).get(Utils.KEY_SPELL_CHECKER_LANGUAGE_NAME, "").isEmpty()){
            Preferences.userRoot().node(Utils.KEY_PREFERENCES).put(Utils.KEY_SPELL_CHECKER_LANGUAGE_NAME, "English");
        }

        Utils.setMainStage(stage);    
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
