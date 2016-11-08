package com.zaba37.easyreader.asyncTasks;

import com.zaba37.easyreader.models.EasyReaderItem;
import com.zaba37.easyreader.ocr.OcrEngine;
import io.github.karols.hocr4j.Page;
import io.github.karols.hocr4j.dom.HocrElement;
import io.github.karols.hocr4j.dom.HocrParser;
import io.github.karols.hocr4j.dom.HocrTag;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zaba3 on 06.11.2016.
 */
public class OCRProgressController extends AsyncTask {
    @FXML
    private Button cancelButton;

    @FXML
    private ProgressIndicator progressIndicatior;

    private boolean cancel = false;
    private ArrayList<EasyReaderItem> list;
    private double progress;

    @Override
    public void onPreExecute() {
        progress = (double) list.size() / 100.0;
    }

    @Override
    public void doInBackground() {
        this.setDaemon(true);

        OcrEngine ocrEngine = OcrEngine.getInstance();
        String result;

        for(EasyReaderItem i : list) {
            if (!cancel) {
                result = ocrEngine.getOcrResult(i.getFile());
                ArrayList<Page> pages = new ArrayList<>();
                ArrayList<String> textLines = new ArrayList<>();

                List<HocrElement> a = HocrParser.createAst(result);

                List<Page> page = HocrParser.parse(a);

                Hoc

                pages.addAll(HocrParser.parse(result));
                textLines.addAll(pages.get(0).getAllLinesAsStrings());

         //       Document doc = Jsoup.parse(result);

                for (String line : textLines) {

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            i.getTextArea().appendText(line);
                        }
                    });
                }

                this.publishProgress();
            }
        }
    }

    @Override
    public void onPostExecute() {

        if(cancel){
            for(EasyReaderItem i : list){
                i.getTextArea().clear();
            }
        }

        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    @Override
    public void progressCallback(Object... params) {
        progressIndicatior.setProgress(progressIndicatior.getProgress() + progress);
    }

    public void setLoadedItemList(ArrayList<EasyReaderItem> list){
        this.list = new ArrayList<>();
        this.list.addAll(list);
    }

    @FXML
    private void handleCancelcPressed() {
        cancel = true;

        this.interrupt();

        ((Stage) cancelButton.getScene().getWindow()).close();
    }
}
