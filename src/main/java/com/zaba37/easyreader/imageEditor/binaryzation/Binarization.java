/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zaba37.easyreader.imageEditor.binaryzation;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author zaba37
 */
public class Binarization {

    public BufferedImage manual(BufferedImage image, int threshold) {
        BufferedImage binImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        ExecutorService executor = Executors.newWorkStealingPool();

        for (int i = 0; i < image.getHeight(); i++) {
            int y = i;
            executor.execute(() -> {
                for (int j = 0; j < image.getWidth(); j++) {
                    Color color = new Color(image.getRGB(j, y));

                    int grey = (int) (((double) color.getRed() * 0.299) + ((double) color.getGreen() * 0.587) + ((double) color.getBlue() * 0.114));

                    if (grey >= threshold) {
                        grey = 255;
                    } else {
                        grey = 0;
                    }

                    binImage.setRGB(j, y, new Color(grey, grey, grey).getRGB());
                }
            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (Exception e) {

        }

        return binImage;
    }

    public BufferedImage percentBlackSelection(BufferedImage image, int p) {
        BufferedImage binImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        ExecutorService executor = Executors.newWorkStealingPool();
        int[] histogramTable = new int[256];
        int imageSize = image.getWidth() * image.getHeight();
        int t = (int) (((double) p / 100) * imageSize);

        for (int i = 0; i < image.getHeight(); i++) {
            int y = i;
            executor.execute(() -> {
                for (int j = 0; j < image.getWidth(); j++) {
                    Color color = new Color(image.getRGB(j, y));

                    int grey = (int) (((double) color.getRed() * 0.299) + ((double) color.getGreen() * 0.587) + ((double) color.getBlue() * 0.114));

                    histogramTable[grey]++;

                    binImage.setRGB(j, y, new Color(grey, grey, grey).getRGB());
                }
            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 1; i < 256; i++) {
            histogramTable[i] = histogramTable[i] + histogramTable[i - 1];
        }

        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                Color color = new Color(binImage.getRGB(j, i));

                int grey = color.getRed();

                if (histogramTable[grey] >= t) {
                    grey = 255;
                } else {
                    grey = 0;
                }

                binImage.setRGB(j, i, new Color(grey, grey, grey).getRGB());
            }
        }

        return binImage;
    }

    public BufferedImage meanIterativeSelection(BufferedImage image) {
        BufferedImage binImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        ExecutorService executor = Executors.newWorkStealingPool();
        int[] histogramTable = new int[256];
        int imageSize = image.getWidth() * image.getHeight();

        for (int i = 0; i < image.getHeight(); i++) {
            int y = i;
            executor.execute(() -> {
                for (int j = 0; j < image.getWidth(); j++) {
                    Color color = new Color(image.getRGB(j, y));

                    int grey = (int) (((double) color.getRed() * 0.299) + ((double) color.getGreen() * 0.587) + ((double) color.getBlue() * 0.114));

                    histogramTable[grey]++;

                    binImage.setRGB(j, y, new Color(grey, grey, grey).getRGB());
                }
            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int histogramCenterSum = 0;
        int histogramCenterIndex = 0;
        int leftCenterSum = 0;
        int leftCenterIndex = 0;
        int rightCenterSum = 0;
        int rightCenterIndex = 0;

        for (int i = 0; i < histogramTable.length; i++) {
            if (histogramCenterSum >= (int) ((double) imageSize / 2)) {
                break;
            } else {
                histogramCenterSum += histogramTable[i];
                histogramCenterIndex = i;
            }
        }

        for (int i = 0; i < histogramCenterIndex; i++) {
            if (leftCenterSum >= imageSize / 4) {
                break;
            } else {
                leftCenterSum += histogramTable[i];
                leftCenterIndex = i;
            }
        }

        for (int i = histogramCenterIndex; i < histogramTable.length; i++) {
            if (rightCenterSum >= imageSize / 4) {
                break;
            } else {
                rightCenterSum += histogramTable[i];
                rightCenterIndex = i;
            }
        }

        while (((leftCenterIndex + rightCenterIndex) / 2) != histogramCenterIndex) {
            histogramCenterIndex = (leftCenterIndex + rightCenterIndex) / 2;
            leftCenterIndex = 0;
            rightCenterIndex = 0;
            leftCenterSum = 0;
            rightCenterSum = 0;
            int leftSum = 0;
            int rightSum = 0;

            for (int i = 0; i < histogramCenterIndex; i++) {
                leftSum += histogramTable[i];
            }

            for (int i = histogramCenterIndex; i < histogramTable.length; i++) {
                rightSum += histogramTable[i];
            }

            for (int i = 0; i < histogramCenterIndex; i++) {
                if (leftCenterSum >= leftSum / 2) {
                    break;
                } else {
                    leftCenterSum += histogramTable[i];
                    leftCenterIndex = i;
                }
            }

            for (int i = histogramCenterIndex; i < histogramTable.length; i++) {
                if (rightCenterSum >= rightSum / 2) {
                    break;
                } else {
                    rightCenterSum += histogramTable[i];
                    rightCenterIndex = i;
                }
            }
        }

//        Frame fremeFiltered = new JFrame();
//        fremeFiltered.add(new HistogramPrinter(binImage, "Filtered image"));
//        fremeFiltered.pack();
//        fremeFiltered.setVisible(true);
//        System.out.println(histogramCenterIndex);

        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                Color color = new Color(binImage.getRGB(j, i));

                int grey = color.getRed();

                if (grey >= histogramCenterIndex) {
                    grey = 255;
                } else {
                    grey = 0;
                }

                binImage.setRGB(j, i, new Color(grey, grey, grey).getRGB());
            }
        }

        return binImage;
    }

    public BufferedImage entropySelection(BufferedImage image) {
        BufferedImage binImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        ExecutorService executor = Executors.newWorkStealingPool();
        int[] histogramTable = new int[256];
        int imageSize = image.getWidth() * image.getHeight();
        double[] p = new double[256];
        double[] entropyTable = new double[256];

        for (int i = 0; i < image.getHeight(); i++) {
            int y = i;
            executor.execute(() -> {
                for (int j = 0; j < image.getWidth(); j++) {
                    Color color = new Color(image.getRGB(j, y));

                    int grey = (int) (((double) color.getRed() * 0.299) + ((double) color.getGreen() * 0.587) + ((double) color.getBlue() * 0.114));

                    histogramTable[grey]++;

                    binImage.setRGB(j, y, new Color(grey, grey, grey).getRGB());
                }
            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < p.length; i++) {
            p[i] = (double) histogramTable[i] / (double) imageSize;
        }

        for (int i = 0; i < p.length; i++) {
            entropyTable[i] = p[i] * Math.log10(p[i]);
        }

        double entropyMin = entropyTable[0];
        int index = 0;

        for (int i = 0; i < p.length; i++) {
            if (entropyMin > entropyTable[i]) {
                entropyMin = entropyTable[i];
                index = i;
            }
        }

        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                Color color = new Color(binImage.getRGB(j, i));

                int grey = color.getRed();

                if (grey >= index) {
                    grey = 255;
                } else {
                    grey = 0;
                }

                binImage.setRGB(j, i, new Color(grey, grey, grey).getRGB());
            }
        }

        return binImage;
    }

    public static int[] imageHistogram(BufferedImage input) {
        int[] histogram = new int[256];

        for(int i=0; i<histogram.length; i++) histogram[i] = 0;

        for(int i=0; i<input.getWidth(); i++) {

             for (int j = 0; j < input.getHeight(); j++) {
                    int red = new Color(input.getRGB(i, j)).getRed();
                    histogram[red]++;
                }
            }

        return histogram;
    }

    public static BufferedImage toGray(BufferedImage original) {
        ExecutorService executor = Executors.newWorkStealingPool();

        BufferedImage lum = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());

        for(int i=0; i<original.getWidth(); i++) {
            int y = i;

            executor.execute(() -> {
                int alpha, red, green, blue;
                int newPixel;

                for (int j = 0; j < original.getHeight(); j++) {

                    alpha = new Color(original.getRGB(y, j)).getAlpha();
                    red = new Color(original.getRGB(y, j)).getRed();
                    green = new Color(original.getRGB(y, j)).getGreen();
                    blue = new Color(original.getRGB(y, j)).getBlue();

                    red = (int) (0.21 * red + 0.71 * green + 0.07 * blue);
                    newPixel = colorToRGB(alpha, red, red, red);

                    lum.setRGB(y, j, newPixel);
                }
            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return lum;

    }

    public static int otsuTreshold(BufferedImage original) {

        int[] histogram = imageHistogram(original);
        int total = original.getHeight() * original.getWidth();

        float sum = 0;
        for(int i=0; i<256; i++) sum += i * histogram[i];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        int threshold = 0;

        for(int i=0 ; i<256 ; i++) {
            wB += histogram[i];
            if(wB == 0) continue;
            wF = total - wB;

            if(wF == 0) break;

            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if(varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }

        return threshold;

    }

    public static File binarize(File image) {
        String binFilePath = image.getPath() + ".bin.png";
        File binImageFile = new File(binFilePath);
        ExecutorService executor = Executors.newWorkStealingPool();
        BufferedImage original = null;

        try {
            original = ImageIO.read(image);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int threshold = otsuTreshold(original);

        BufferedImage binarized = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());

        original = toGray(original);

        for(int i=0; i<original.getWidth(); i++) {
            int y = i;
            BufferedImage finalOriginal = original;
            executor.execute(() -> {
                for (int j = 0; j < finalOriginal.getHeight(); j++) {
                    int red;
                    int newPixel;

                    red = new Color(finalOriginal.getRGB(y, j)).getRed();
                    int alpha = new Color(finalOriginal.getRGB(y, j)).getAlpha();
                    if (red > threshold) {
                        newPixel = 255;
                    } else {
                        newPixel = 0;
                    }
                    newPixel = colorToRGB(alpha, newPixel, newPixel, newPixel);
                    binarized.setRGB(y, j, newPixel);

                }
            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ImageIO.write(binarized, "png", binImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return binImageFile;
    }

    public static int colorToRGB(int alpha, int red, int green, int blue) {

        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red; newPixel = newPixel << 8;
        newPixel += green; newPixel = newPixel << 8;
        newPixel += blue;

        return newPixel;
    }

}
