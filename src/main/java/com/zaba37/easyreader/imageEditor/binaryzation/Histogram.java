/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zaba37.easyreader.imageEditor.binaryzation;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 *
 * @author zaba37
 */
public class Histogram {

    public BufferedImage linearNormalization(BufferedImage image) {
        BufferedImage filteredImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int[] histogramTableRed = new int[256];
        int[] histogramTableGreen = new int[256];
        int[] histogramTableBlue = new int[256];
        int[] redLUT = new int[256];
        int[] greenLUT = new int[256];
        int[] blueLUT = new int[256];
        int maxRed, maxGreen, maxBlue;
        int minRed, minGreen, minBlue;

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color color = new Color(image.getRGB(i, j));

                histogramTableRed[color.getRed()]++;
                histogramTableGreen[color.getGreen()]++;
                histogramTableBlue[color.getBlue()]++;

            }
        }

        int imageSize = image.getHeight() * image.getWidth();
        int threshold = (int) ((imageSize / 256) * 0.01);
        if (threshold == 0) {
            threshold = 1;
        }

        maxRed = getMaxValue(histogramTableRed, threshold);
        minRed = getMinValue(histogramTableRed, threshold);

        maxGreen = getMaxValue(histogramTableGreen, threshold);
        minGreen = getMinValue(histogramTableGreen, threshold);

        maxBlue = getMaxValue(histogramTableBlue, threshold);
        minBlue = getMinValue(histogramTableBlue, threshold);

        for (int i = 0; i < redLUT.length; i++) {
            redLUT[i] = (int) (((double) (i - minRed) / (double) (maxRed - minRed)) * 255);

            if (redLUT[i] > 255) {
                redLUT[i] = 255;
            } else if (redLUT[i] < 0) {
                redLUT[i] = 0;
            }
        }

        for (int i = 0; i < greenLUT.length; i++) {
            greenLUT[i] = (int) (((double) (i - minGreen) / (double) (maxGreen - minGreen)) * 255);
            if (greenLUT[i] > 255) {
                greenLUT[i] = 255;
            } else if (greenLUT[i] < 0) {
                greenLUT[i] = 0;
            }
        }

        for (int i = 0; i < blueLUT.length; i++) {
            blueLUT[i] = (int) (((double) (i - minBlue) / (double) (maxBlue - minBlue)) * 255);

            if (blueLUT[i] > 255) {
                blueLUT[i] = 255;
            } else if (blueLUT[i] < 0) {
                blueLUT[i] = 0;
            }
        }

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color color = new Color(image.getRGB(i, j));
                int red = redLUT[color.getRed()];
                int green = greenLUT[color.getGreen()];
                int blue = blueLUT[color.getBlue()];

                Color newColor = new Color(red, green, blue);

                filteredImage.setRGB(i, j, newColor.getRGB());
            }
        }

//        JFrame frameCurrent = new JFrame();
//        JFrame fremeFiltered = new JFrame();
//
//        frameCurrent.add(new HistogramPrinter(image, "Oryginal image"));
//        fremeFiltered.add(new HistogramPrinter(filteredImage, "Filtered image"));
//
//        frameCurrent.pack();
//        fremeFiltered.pack();
//
//        frameCurrent.setVisible(true);
//        fremeFiltered.setVisible(true);

        return filteredImage;
    }

    public BufferedImage equalize(BufferedImage image) {
        BufferedImage filteredImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int[] histogramTableRed = new int[256];
        int[] histogramTableGreen = new int[256];
        int[] histogramTableBlue = new int[256];
        double[] redDistr;
        double[] greenDistr;
        double[] blueDistr;
        int[] redLUT = new int[256];
        int[] greenLUT = new int[256];
        int[] blueLUT = new int[256];
        int imageSize = image.getHeight() * image.getWidth();
        double redNominator;
        double redDenominator;
        double greenNominator;
        double greenDenominator;
        double blueNominator;
        double blueDenominator;
        double minRedDistr;
        double minGreenDistr;
        double minBlueDistr;

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color color = new Color(image.getRGB(i, j));

                histogramTableRed[color.getRed()]++;
                histogramTableGreen[color.getGreen()]++;
                histogramTableBlue[color.getBlue()]++;

            }
        }

        redDistr = calculateDistribuant(histogramTableRed, imageSize);
        greenDistr = calculateDistribuant(histogramTableGreen, imageSize);
        blueDistr = calculateDistribuant(histogramTableBlue, imageSize);

        minRedDistr = calculateMinDistrValue(redDistr);
        minGreenDistr = calculateMinDistrValue(greenDistr);
        minBlueDistr = calculateMinDistrValue(blueDistr);

        redNominator = -255 * minRedDistr;
        redDenominator = 1 - minRedDistr;
        greenNominator = -255 * minGreenDistr;
        greenDenominator = 1 - minGreenDistr;
        blueNominator = -255 * minBlueDistr;
        blueDenominator = 1 - minBlueDistr;

        for (int i = 0; i < 256; i++) {
            redLUT[i] = (int) (((255 * redDistr[i]) + redNominator) / redDenominator);
            greenLUT[i] = (int) (((255 * greenDistr[i]) + greenNominator) / greenDenominator);
            blueLUT[i] = (int) (((255 * blueDistr[i]) + blueNominator) / blueDenominator);
        }

        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                Color color = new Color(image.getRGB(j, i));
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();
                red = redLUT[red];
                green = greenLUT[green];
                blue = blueLUT[blue];

                Color newColor = new Color(red, green, blue);
                filteredImage.setRGB(j, i, newColor.getRGB());
            }
        }

//        JFrame frameCurrent = new JFrame();
//        JFrame fremeFiltered = new JFrame();
//
//        frameCurrent.add(new HistogramPrinter(image, "Oryginal image"));
//        fremeFiltered.add(new HistogramPrinter(filteredImage, "Filtered image"));
//
//        frameCurrent.pack();
//        fremeFiltered.pack();
//
//        frameCurrent.setVisible(true);
//        fremeFiltered.setVisible(true);

        return filteredImage;
    }

    private int validateColor(int value) {
        if (value > 255) {
            return 255;
        } else if (value < 0) {
            return 0;
        } else {
            return value;
        }
    }

    private int getMinValue(int[] tab, int threshold) {
        for (int i = 0; i < tab.length; i++) {
            if (tab[i] >= threshold) {
                return i;
            }
        }
        return 0;
    }

    private int getMaxValue(int[] tab, int threshold) {
        for (int i = tab.length - 1; i >= 0; i--) {
            if (tab[i] >= threshold) {
                return i;
            }
        }
        return 255;
    }

    private double calculateMinDistrValue(double[] tab) {
        int n = 0;
        while (tab[n] <= 0) {
            n++;
        }
        return tab[n];
    }

    private double[] calculateDistribuant(int[] tab, int imageSize) {
        double[] distr = new double[256];
        double sum = 0;
        for (int p = 0; p < 256; p++) {
            sum += tab[p];
            distr[p] = sum / imageSize;
        }

        return distr;
    }
}
