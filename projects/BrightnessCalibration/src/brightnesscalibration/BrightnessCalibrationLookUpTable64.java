package brightnesscalibration;

import curvefitting.inverserodbard.InverseRodbard;
import curvefitting.inverserodbard.InverseRodbard_Values;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class BrightnessCalibrationLookUpTable64 {
    int nRows;
    int nCols;
    int nSlices;
    
    int[] givenInputValues;
    double[] measuredOutputValues;
    int[] nominalOutputValues;
    int[] computedInputValues;    
    
    public BrightnessCalibrationLookUpTable64(Map<String, double[][]> grayImageStack, int[] givenValues) {
        nCols = grayImageStack.values().iterator().next()[0].length;
        nRows = grayImageStack.values().iterator().next().length;
        nSlices = grayImageStack.size();
        givenInputValues = givenValues;
        int roiW = 10;
        int roiH = 10;
        int roiX = nCols/2 - roiW/2;
        int roiY = nRows/2 - roiH/2; 
        measuredOutputValues = getAvgZProfile(grayImageStack, roiX, roiY, roiW, roiH);
        nominalOutputValues = computeNominalValues();
        computedInputValues = computeFittedValues(givenValues, measuredOutputValues, nominalOutputValues);
        
    }
    
    public double[] getAvgZProfile(Map<String, double[][]> grayImageStack, int roiX, int roiY, int roiW, int roiH) {
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
            double avgValue_norm = avgValue/65536 * 255;
            measuredValues[slice_num] = avgValue_norm;
        }
        return measuredValues;
    }
    
    public int[] computeNominalValues() {
        // Get the nominal values
        int[] nominalValues = new int[256];
        for (int i = 0; i < 256; i++) {
            nominalValues[i] = i;
        }
        return nominalValues;
    }
    
    public int[] computeFittedValues(int[] givenValues, double[] measuredValues, int[] nominalValues) {
        // cast given Values from int to double
        double[] givenValues_Double =  new double[givenValues.length];       
        for (int i = 0; i < givenValues.length; i++) {
            givenValues_Double[i] = givenValues[i]; 
        } 
        
        // cast given Values from int to double
        double[] nominalValues_Double =  new double[nominalValues.length];       
        for (int i = 0; i < nominalValues.length; i++) {
            nominalValues_Double[i] = nominalValues[i]; 
        } 

        // Build the data for the inverse function (x->y, y->x)
        InverseRodbard fit = new InverseRodbard(measuredValues, givenValues_Double);
        fit.fit();
        double[] outputValues = InverseRodbard_Values.computeAll(fit.params, nominalValues_Double);
        
        // compute the fitted values      
        int[] outputValues_Int =  new int[256];       
        for (int i = 0; i < 256; i++) {
            outputValues_Int[i] = (int) Math.round(outputValues[i]); 
        }  
        return outputValues_Int;
    }

    public int[][] computeLookUpTable() {
        
        // only add valid pairs     
        List<Integer> valid_indices = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            double computedValue = computedInputValues[i];
            if (computedValue <= 255 && computedValue >= 0) {
                valid_indices.add(i);
            }            
        }       
        
        int nValid = valid_indices.size();
        int[][] lookUpTable = new int[2][nValid];
        for (int i = 0; i < nValid; i++) {
            lookUpTable[0][i] = nominalOutputValues[i];
            lookUpTable[1][i] = computedInputValues[i];
        }
        
        return lookUpTable;
    }
    
    public static double[][] applyLookUpTable(double[][] inputImg, int[][] lookUpTable) {
                
        int nRows = inputImg.length;
        int nCols = inputImg[0].length;
        
        // apply the look-up-table
        double[][] outputImgData = new double[nRows][nCols];
        List<Integer> nominalValues = Arrays.stream(lookUpTable[0]).boxed().collect(Collectors.toList());
        List<Integer> computedValues = Arrays.stream(lookUpTable[1]).boxed().collect(Collectors.toList());
        for (int row_num = 0; row_num < nRows; row_num++) {
            for (int col_num = 0; col_num < nCols; col_num++) {
                double imgValue = inputImg[row_num][col_num];
                int indexFound = nominalValues.indexOf(imgValue);
                int computedValue = computedValues.get(indexFound);
                outputImgData[row_num][col_num] = computedValue;
            }
        }
                
        return outputImgData;
    }
}
