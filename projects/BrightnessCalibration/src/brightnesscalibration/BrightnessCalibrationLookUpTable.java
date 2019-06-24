package brightnesscalibration;

import core.ArrayUtils;
import core.ImageUtils;
import core.TXT;
import curvefitting.inverserodbard.InverseRodbard;
import curvefitting.inverserodbard.InverseRodbard_Values;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BrightnessCalibrationLookUpTable {


    public static List<List<Integer>> computeLookUpTable(Map<String,BufferedImage> inputImageStack) {
        List<List<Integer>> lookUpTable = new ArrayList<>();
        
        int nCols = ((BufferedImage)(inputImageStack.values().toArray())[0]).getWidth();
        int nRows = ((BufferedImage)(inputImageStack.values().toArray())[0]).getHeight();
        int nSlices = inputImageStack.size();
        
        int minDim = Math.min(nCols, nRows);
        int roiW = (int) Math.round(0.02*minDim);
        int roiH = (int) Math.round(0.02*minDim);
        int roiX = nCols/2 - roiW/2;
        int roiY = nRows/2 - roiH/2;        
        
        // Compute the average spectrum through the image stack within the roi
        double[] measuredValues = new double[nSlices];
        List<String> imgNames = new ArrayList<>(inputImageStack.keySet());
        Collections.sort(imgNames, new Comparator<String>(){
            @Override
            public int compare(String s1, String s2) {
                int int1 = Integer.parseInt(s1);
                int int2 = Integer.parseInt(s2);
                return int1 - int2;
            }
            
        });
        for (int slice_num = 0; slice_num < nSlices; slice_num++) {
            BufferedImage rgbImage = inputImageStack.get(imgNames.get(slice_num));
            
            BufferedImage grayImage = ImageUtils.color2Gray(rgbImage);
            
            byte[] imgData = ((DataBufferByte)grayImage.getRaster().getDataBuffer()).getData();
            double sum = 0;
            for (int y = roiY; y < (roiY + roiH); y++) {
                for (int x = roiX; x < (roiX + roiW); x++) {
                    int i = y*nRows + x;
                    sum += imgData[i]&0xFF;
                }
            }
            measuredValues[slice_num] = sum/(roiW*roiH);
        }
        
        // Get the expected values (programmed values)
        double[] requestedValues =  new double[nSlices];         
        for (int slice_num = 0; slice_num < nSlices; slice_num++) {
            requestedValues[slice_num] = Double.parseDouble(imgNames.get(slice_num));
        }
        
        // Get the nominal values
        List<Integer> nominalValues = new ArrayList<>(256);
        double[] nominalValues_DoubleArray = new double[256];
        for (int i = 0; i < 256; i++) {
            nominalValues.add(i);
            nominalValues_DoubleArray[i] = i;
        }
        
        // TODO debug remove
        TXT.saveVector(ArrayUtils.ArrayToList_Double(measuredValues), Double.class, "C:\\Users\\kfd18\\kfd18_Downloads\\measuredvalues.txt");
        TXT.saveVector(ArrayUtils.ArrayToList_Double(requestedValues), Double.class, "C:\\Users\\kfd18\\kfd18_Downloads\\requestedvalues.txt");
                
        // Build the data for the inverse function (x->y, y->x)
        InverseRodbard fit = new InverseRodbard(measuredValues, requestedValues);
        fit.fit();
        double[] outputValues = InverseRodbard_Values.computeAll(fit.params, nominalValues_DoubleArray);
        
        // compute the fitted values      
        List<Integer> outputValues_Int =  new ArrayList<>(256);       
        for (int i = 0; i < 256; i++) {
            double outputValue = outputValues[i];
            if (outputValue > 255) {
                outputValues_Int.add(255);
            } else if (outputValue < 0) {
                outputValues_Int.add(0);
            } else {
                outputValues_Int.add( (int) outputValues[i]);
            }
            
        }       
        
        // store the look-up-table
        lookUpTable.add(nominalValues);
        lookUpTable.add(outputValues_Int);
        
        return lookUpTable;
    }
    
    public static BufferedImage applyLookUpTable(BufferedImage inputImg, List<List<Integer>> lookUpTable) {
        
        if (inputImg.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            throw new IllegalArgumentException("Image must be of type: TYPE_BYTE_GRAY");
        }
        
        int nRows = inputImg.getHeight();
        int nCols = inputImg.getWidth();
        
        // get the image data
        List<List<Integer>> inputImgData = ImageUtils.GrayImageToList_Integer(inputImg);
        
        // apply the look-up-table
        List<List<Integer>> outputImgData = new ArrayList<>();
        List<Integer> nominalValues = lookUpTable.get(0);
        List<Integer> outputValues = lookUpTable.get(1);
        for (int row_num = 0; row_num < nRows; row_num++) {
            List<Integer> newRow = new ArrayList<>();
            for (int col_num = 0; col_num < nCols; col_num++) {
                int imgValue = inputImgData.get(row_num).get(col_num);
                int indexFound = nominalValues.indexOf(imgValue);
                int outputValue = outputValues.get(indexFound);
                newRow.add(outputValue);
            }
            outputImgData.add(newRow);
        }
        
        BufferedImage outputImage = ImageUtils.ListToGrayImage_Integer(outputImgData);
        
        return outputImage;
    }
}
