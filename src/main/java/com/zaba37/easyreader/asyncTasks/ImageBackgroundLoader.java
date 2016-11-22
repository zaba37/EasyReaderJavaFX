/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zaba37.easyreader.asyncTasks;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.imageEditor.binaryzation.Binarization;
import com.zaba37.easyreader.controllers.LoadingWindowSceneController;
import com.zaba37.easyreader.models.EasyReaderItem;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import org.apache.commons.io.FileUtils;
import org.ghost4j.document.DocumentException;
import org.ghost4j.document.PDFDocument;
import org.ghost4j.renderer.RendererException;
import org.ghost4j.renderer.SimpleRenderer;

/**
 *
 * @author zaba37
 */
public class ImageBackgroundLoader extends AsyncTask {

    private boolean loading;
    private final ArrayList<EasyReaderItem> list;
    private int itemCounter;
    private final String tmpDirPath;
    private final LoadingWindowSceneController controller;

    public ImageBackgroundLoader(LoadingWindowSceneController controller) throws IOException {
        this.controller = controller;
        list = new ArrayList();
        loading = true;
        itemCounter = 0;
        tmpDirPath = createTmpDirectory();
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void doInBackground() {
        int fileCounter = 0;
        this.setDaemon(true);

        while (loading) {
            if (fileCounter != this.controller.getFilesList().size()) {

                if (isPDFFile(this.controller.getFilesList().get(fileCounter))) {
                    list.addAll(binarizationImageListChecker(pdfToEasyReaderItems(this.controller.getFilesList().get(fileCounter))));
                } else {
                    list.add(binarizationImageChecker(copyImageToTmpFolder(this.controller.getFilesList().get(fileCounter))));
                    this.publishProgress((Object) null);
                    itemCounter++;
                }

                fileCounter++;
            }

            if (list.size() == this.controller.getFilesNumber()) {
                loading = false;
            }
        }
    }

    private EasyReaderItem binarizationImageChecker(EasyReaderItem item){
        if(Preferences.userRoot().node(Utils.KEY_PREFERENCES).getBoolean(Utils.KEY_USE_BINARIZATION_WHEN_LOADING, false)) {
            item.setUseBinImage(true);
            item.getFile();
        }

        return item;
    }

    private List<EasyReaderItem> binarizationImageListChecker(List<EasyReaderItem> itemsList){
        if(Preferences.userRoot().node(Utils.KEY_PREFERENCES).getBoolean(Utils.KEY_USE_BINARIZATION_WHEN_LOADING, false)) {
            for (EasyReaderItem item : itemsList) {
                item.setUseBinImage(true);
                item.getFile();
            }
        }

        return itemsList;
    }

    @Override
    public void onPostExecute() {
        controller.closeLoadingWindow();

        if (!controller.isCancel()) {
            Utils.getMainWindowController().initImageListView(list);
        } else {
            Utils.getMainWindowController().initImageListView(new ArrayList());
        }
    }

    @Override
    public void progressCallback(Object... params) {
        this.controller.updateProgressIndycator();
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    private boolean isPDFFile(File file) {
        String extension = "";
        String fileName = file.getName();

        int i = fileName.lastIndexOf('.');

        if (i > 0) {
            extension = fileName.substring(i + 1);
        }

        if ("pdf".equals(extension)) {
            return true;
        } else {
            return false;
        }
    }

    private List<EasyReaderItem> pdfToEasyReaderItems(File pdfFile) {
        PDFDocument document = new PDFDocument();
        ArrayList<EasyReaderItem> pdfItems = new ArrayList();
        EasyReaderItem[] itemsTable;
        ExecutorService executor = Executors.newWorkStealingPool();

        try {
            document.load(pdfFile);
        } catch (IOException ex) {
            Logger.getLogger(ImageBackgroundLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        SimpleRenderer renderer = new SimpleRenderer();
        renderer.setResolution(300);

        List<Image> images = new ArrayList();

        try {
            images.addAll(renderer.render(document));
        } catch (IOException | RendererException | DocumentException ex) {
            Logger.getLogger(ImageBackgroundLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        itemsTable = new EasyReaderItem[images.size()];

        for (int i = 0; i < images.size(); i++) {
            int y = i;

            executor.execute(() -> {
                try {
                    if (loading) {
                        String filePath = tmpDirPath + File.separator + pdfFile.getName() + (y + 1) + ".png";
                        File imageFile = new File(filePath);

                        ImageIO.write((RenderedImage) images.get(y), "png", imageFile);

                        //pdfItems.add(new EasyReaderItem(imageFile));
                        itemsTable[y] = new EasyReaderItem(imageFile);

                        this.publishProgress((Object) null);
                        itemCounter++;
                    } else {
                        //break;
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ImageBackgroundLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(int i = 0; i < itemsTable.length; i++){
            pdfItems.add(itemsTable[i]);
        }

        return pdfItems;
    }

    private EasyReaderItem copyImageToTmpFolder(File imageFileToCopy){
        javafx.scene.image.Image image = new javafx.scene.image.Image(imageFileToCopy.toURI().toString());

        String filePath = tmpDirPath + File.separator + imageFileToCopy.getName() + ".png";
        File imageFile = new File(filePath);

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new EasyReaderItem(imageFile);
    }

    private String createTmpDirectory() {
        String OSType = OSValidation();
        String path = "";

        try {
            path = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException ex) {
            Logger.getLogger(ImageBackgroundLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        switch (OSType) {
            case "win":
                return createWindowsDirecotry(path);
            case "mac":
                return createUnixOrMacDirectory(path);
            case "unix":
                return createUnixOrMacDirectory(path);
            default:
                return "";
        }
    }

    private String createWindowsDirecotry(String mainPath) {
        mainPath = mainPath.substring(0, mainPath.lastIndexOf("/"));
        mainPath = mainPath + "/TmpEasyReaderImageDirectory";
        File file = new File(mainPath);

        try {
            if (file.exists()) {
                FileUtils.forceDelete(file);
            }
            
            FileUtils.forceMkdir(file);
            
            Files.setAttribute(file.toPath(), "dos:hidden", true);

        } catch (IOException ex) {
            Logger.getLogger(ImageBackgroundLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return file.getPath();
    }

    private String createUnixOrMacDirectory(String mainPath) {
        mainPath = mainPath.substring(0, mainPath.lastIndexOf(File.separator));
        mainPath = mainPath + File.separator + ".TmpEasyReaderImageDirectory";
        File file = new File(mainPath);

        try {
            if (file.exists()) {
                FileUtils.forceDelete(file);
            }

            FileUtils.forceMkdir(file);
        } catch (IOException ex) {
            Logger.getLogger(ImageBackgroundLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return file.getPath();
    }

    private String OSValidation() {
        String OS = System.getProperty("os.name").toLowerCase();

        if (OS.indexOf("win") >= 0) {
            System.out.println("Windows");
            return "win";
        } else if (OS.indexOf("mac") >= 0) {
            System.out.println("MaxOS");
            return "mac";
        } else {
            System.out.println("Unix");
            return "unix";
        }
    }
}
