package core;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

public class Interpolation {

    public static double interpolate_0D(BufferedImage grayImage, List<Double> xy) {
        // get buffer
        int nCols = grayImage.getWidth();
        byte[] grayData = ((DataBufferByte) grayImage.getData().getDataBuffer()).getData();
        // get center coordinates
        double centerX = xy.get(0);
        double centerY = xy.get(1);
        // get offsets to neighboring point
        int offsetNE = (int) Math.floor(centerY) * nCols + (int) Math.ceil(centerX);
        int offsetNW = (int) Math.floor(centerY) * nCols + (int) Math.floor(centerX);
        int offsetSE = (int) Math.ceil(centerY) * nCols + (int) Math.ceil(centerX);
        int offsetSW = (int) Math.ceil(centerY) * nCols + (int) Math.floor(centerX);
        // get displacement from center
        double xDisp = centerX - Math.floor(centerX);
        double yDisp = centerY - Math.floor(centerY);
        // linear interpolation
        double interpValue = xDisp * (1 - yDisp) * (int) (grayData[offsetNE] & 255) + (1 - xDisp) * (1 - yDisp) * (int) (grayData[offsetNW] & 255) + xDisp * yDisp * (int) (grayData[offsetSE] & 255) + (1 - xDisp) * yDisp * (int) (grayData[offsetSW] & 255);
        return interpValue;
    }

    public static List<Double> interpolate_1D(BufferedImage grayImage, List<List<Double>> points) {
        // get buffer
        List<Double> interpValues = new ArrayList<>(points.size());
        for (List<Double> point : points) {
            interpValues.add(interpolate_0D(grayImage, point));
        }
        return interpValues;
    }

}
