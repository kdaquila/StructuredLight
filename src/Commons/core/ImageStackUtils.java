package core;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ImageStackUtils {
    
    
    public static double[] getAvgZProfile(Map<String, double[][]> grayImageStack, int roiX, int roiY, int roiW, int roiH) {
        int nSlices = grayImageStack.size();
        double[] measuredValues = new double[nSlices];
        List<String> imgNames = new ArrayList<>(grayImageStack.keySet());
        Collections.sort(imgNames, new Comparator<String>(){
            @Override
            public int compare(String s1, String s2) {
                int int1 = Integer.parseInt(s1.replaceAll("[^0-9]", ""));
                int int2 = Integer.parseInt(s2.replaceAll("[^0-9]", ""));
                return int1 - int2;
            }
            
        });
        for (int slice_num = 0; slice_num < nSlices; slice_num++) {
            double[][] grayImage = grayImageStack.get(imgNames.get(slice_num));
            
            double sum = 0.0;
            for (int y = roiY; y < (roiY + roiH); y++) {
                for (int x = roiX; x < (roiX + roiW); x++) {                    
                    sum += grayImage[y][x];
                }
            }
            double avgValue = sum/(roiW*roiH);
            double avgValue_norm = avgValue;
            measuredValues[slice_num] = avgValue_norm;
        }
        return measuredValues;
    }

    public static BufferedImage meanStack(Map<String, BufferedImage> imgStack) {
        
        // load all of the buffers
        List<List<Byte>> buffers = new ArrayList<>();
        for (String name: imgStack.keySet()) {
            BufferedImage img = imgStack.get(name);
            byte[] imgData = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
            List<Byte> imgDataList = new ArrayList<>();
            for (int i = 0; i < imgData.length; i++) {
                imgDataList.add(imgData[i]);
            }            
            buffers.add(imgDataList);
        }
        
        int nRows = ((BufferedImage) imgStack.values().toArray()[0]).getHeight();
        int nCols = ((BufferedImage) imgStack.values().toArray()[0]).getWidth();
        int nSlices = imgStack.size();
        
        BufferedImage outputImg = new BufferedImage(nCols, nRows, BufferedImage.TYPE_BYTE_GRAY);
        byte[] outputImgData = ((DataBufferByte)outputImg.getRaster().getDataBuffer()).getData();
        for (int row_num = 0; row_num < nRows; row_num++) {
            for (int col_num = 0; col_num < nCols; col_num++) {
                int index = row_num*nCols + col_num;
                List<Double> lineSlice = new ArrayList<>();
                for (int slice_num = 0; slice_num < nSlices; slice_num++) {
                    int value = buffers.get(slice_num).get(index)&0xFF;
                    lineSlice.add(new Double(value));
                }
                double mean = ArrayUtils.mean_Double1D(lineSlice);
                if (mean > 255 || mean < 0) {
                    throw new RuntimeException("mean is outside the range: " + mean + " at row,col: " + row_num + ", " + col_num);
                }
                byte meanByte = (byte) mean;
                outputImgData[index] = meanByte;
            }
        }
        
        return outputImg;
    }
}
