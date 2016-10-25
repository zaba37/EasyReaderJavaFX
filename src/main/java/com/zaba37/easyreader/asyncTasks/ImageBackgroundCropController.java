package com.zaba37.easyreader.asyncTasks;

import com.zaba37.easyreader.Utils;
import com.zaba37.easyreader.models.EasyReaderItem;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zaba37 on 22.10.2016.
 */
public class ImageBackgroundCropController extends AsyncTask implements Initializable {

    private ImageView imageView;
    private Bounds bounds;
    private String tmpDirPath;
    private File file;
    private boolean cancel = false;

    @FXML
    private Button cancelButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public void onPreExecute() {
        this.setDaemon(true);
        tmpDirPath = createTmpDirectory();
    }

    @Override
    public void doInBackground() {

        file = new File(tmpDirPath + File.separator + new Timestamp(new Date().getTime()).toString() + ".png");

        int widthFrame = (int) bounds.getWidth();
        int heightFrame = (int) bounds.getHeight();

        int widthShowImage = (int) imageView.getBoundsInParent().getWidth();
        int heightShowImage = (int) imageView.getBoundsInParent().getHeight();

        int correctFrameWidth = (int) ((imageView.getImage().getWidth() / widthShowImage) * widthFrame);
        int correctFrameHeight = (int) ((imageView.getImage().getHeight() / heightShowImage) * heightFrame);

        int pointX = 0;
        int pointY = 0;

        pointX = (int) ((int) bounds.getMinX() * (imageView.getImage().getWidth() / widthShowImage));
        pointY = (int) (bounds.getMinY() * (imageView.getImage().getHeight() / heightShowImage));

        PixelReader reader = imageView.getImage().getPixelReader();
        WritableImage newImage = new WritableImage(reader, pointX, pointY, correctFrameWidth, correctFrameHeight);

        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(newImage, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(), BufferedImage.OPAQUE);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);

        try {
            ImageIO.write(bufImageRGB, "png", file);
            System.out.println("Image saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        graphics.dispose();
    }

    @Override
    public void onPostExecute() {
        if(!cancel){
            Utils.getMainWindowController().addNewEasyReaderItemToList(new EasyReaderItem(file));
        }

        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    @Override
    public void progressCallback(Object... params) {

    }

    public void setImageView(ImageView imageView){
        this.imageView = imageView;
    }

    public void setBounds(Bounds bounds){
        this.bounds = bounds;
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

    @FXML
    private void handleCancelcPressed() {
        cancel = true;
        ((Stage) cancelButton.getScene().getWindow()).close();
    }

    private String createWindowsDirecotry(String mainPath) {
        mainPath = mainPath.substring(0, mainPath.lastIndexOf("/"));
        mainPath = mainPath + "/TmpEasyReaderImageDirectory";
        File file = new File(mainPath);

        try {
            if (file.exists()) {
                return file.getPath();
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
                return file.getPath();
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
