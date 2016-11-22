package com.zaba37.easyreader.asyncTasks;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.models.EasyReaderItem;
import com.zaba37.easyreader.ocr.OcrEngine;
import io.github.karols.hocr4j.Bounds;
import io.github.karols.hocr4j.Page;
import io.github.karols.hocr4j.Word;
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

import java.util.*;
import java.util.prefs.Preferences;

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
    private static  HashMap<Integer, ArrayList<Word>> linesMap;
    private static ArrayList<ArrayList<Word>> linesList;

    @Override
    public void onPreExecute() {
        progress = ((double) 100 / list.size()) /100;
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
                ArrayList<Word> wordsList = new ArrayList<>();

                i.sethOCR(result);

                for(Page p : page){
                    wordsList.addAll(p.getAllWords());
                }

                HashMap<Bounds, Word> wordsMap = new HashMap<>();
                linesMap = new HashMap<>();

                Platform.runLater(() -> i.getTextArea().clear());

                if(!Preferences.userRoot().node(Utils.KEY_PREFERENCES).getBoolean(Utils.KEY_OCR_RESULT_WITHOUT_FORMATING, false)) {
                    for (Word word : wordsList) {
                        if (word.getBounds() != null) {
                            wordsMap.put(word.getBounds(), word);
                        }
                    }

                    int lineNumber = 0;

                    //segregate words by lines
                    for (Bounds bounds : wordsMap.keySet()) {
                        ArrayList<Word> newWodsInLineList = new ArrayList<>();

                        if (linesMap.size() == 0) {
                            newWodsInLineList.add(wordsMap.get(bounds));
                            linesMap.put(lineNumber, newWodsInLineList);
                            lineNumber += 1;
                        } else {
                            boolean addedWordToLine = false;

                            for (Integer line : linesMap.keySet()) {
                                for (Word word : linesMap.get(line)) {
                                    if (word.getBounds().inTheSameRowAs(bounds)) {
                                        ArrayList<Word> wordArrayListFromMap = linesMap.get(line);
                                        wordArrayListFromMap.add(wordsMap.get(bounds));

                                        linesMap.put(line, wordArrayListFromMap);

                                        addedWordToLine = true;
                                    }

                                    if (addedWordToLine) {
                                        break;
                                    }
                                }

                                if (addedWordToLine) {
                                    break;
                                }
                            }

                            if (!addedWordToLine) {
                                newWodsInLineList.add(wordsMap.get(bounds));
                                linesMap.put(lineNumber, newWodsInLineList);
                                lineNumber += 1;
                            }
                        }
                    }

                    for (Integer line : linesMap.keySet()) {
                        quickSortWordsInLine(linesMap.get(line), 0, linesMap.get(line).size() - 1);
                    }

                    linesList = new ArrayList<>();

                    for (Integer line : linesMap.keySet()) {
                        linesList.add(linesMap.get(line));
                    }

                    quicksortPageByLines(linesList, 0, linesList.size() - 1);

                    for (ArrayList<Word> words : linesList) {
                        String line = "";

                        for (int x = 0; x < words.size(); x++) {
                            if ((words.size() - 1) >= (x + 1)) {
                                int distans = words.get(x).getBounds().distance(words.get(x + 1).getBounds());

                                int spacesNumber = distans / 100;

                                line += words.get(x).getText();

                                for (int z = 0; z <= spacesNumber; z++) {
                                    line += " ";
                                }

                                System.out.println(distans);
                            } else {
                                line += words.get(x).getText();
                            }
                        }

                        textLines.add(line);
                    }
                }else {
                    pages.addAll(HocrParser.parse(result));

                    for(Page p : pages){
                        textLines.addAll(p.getAllLinesAsStrings());
                    }
                }

                for (String line : textLines) {

                    Platform.runLater(() -> i.getTextArea().appendText(line + "\n"));
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

    private static void quickSortWordsInLine(ArrayList<Word> wordsLine, int x, int y){
        int i, j;
        Word v, tmp;

        i = x;
        j = y;
        v = wordsLine.get((x + y) / 2);

        do {
            while(v.getBounds().isToTheRight(wordsLine.get(i).getBounds())){
                i++;
            }

            while(v.getBounds().isToTheLeft(wordsLine.get(j).getBounds())){
                j--;
            }

            if( i <= j){
                tmp = wordsLine.get(i);
                wordsLine.set(i, wordsLine.get(j));
                wordsLine.set(j, tmp);

                i++;
                j--;
            }
        } while(i <= j);

        if(x < j){
            quickSortWordsInLine(wordsLine, x, j);
        }

        if(i < y){
            quickSortWordsInLine(wordsLine, i, y);
        }
    }

    private static void quicksortPageByLines(ArrayList<ArrayList<Word>> linesList, int x, int y){
        int i, j;
        ArrayList<Word> v, tmp;

        i = x;
        j = y;
        v = linesList.get((x + y) / 2);

        do {
            while(v.get(0).getBounds().isBelow(linesList.get(i).get(0).getBounds())){
                i++;
            }

            while(v.get(0).getBounds().isAbove(linesList.get(j).get(0).getBounds())){
                j--;
            }

            if( i <= j){
                tmp = linesList.get(i);
                linesList.set(i, linesList.get(j));
                linesList.set(j, tmp);

                i++;
                j--;
            }
        } while(i <= j);

        if(x < j){
            quicksortPageByLines(linesList, x, j);
        }

        if(i < y){
            quicksortPageByLines(linesList, i, y);
        }
    }
}
