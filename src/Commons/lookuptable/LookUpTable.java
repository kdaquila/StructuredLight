package lookuptable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LookUpTable {
    
    public static int[][] applyTable(int[][] inputImageData, int[][] lookUpTable) {
        
        int nRows = inputImageData.length;
        int nCols = inputImageData[0].length;
        
        // apply the look-up-table
        int[][] outputImgData = new int[nRows][nCols];
        List<Integer> nominalValues = Arrays.stream(lookUpTable[0]).boxed().collect(Collectors.toList());
        List<Integer> computedValues = Arrays.stream(lookUpTable[1]).boxed().collect(Collectors.toList());
        for (int row_num = 0; row_num < nRows; row_num++) {
            for (int col_num = 0; col_num < nCols; col_num++) {
                int imgValue = inputImageData[row_num][col_num];
                int indexFound = nominalValues.indexOf(imgValue);
                if (indexFound == -1) {
                    throw new RuntimeException("Could not apply look up table. Value " + imgValue + " was not found" );
                }
                int tableValue = computedValues.get(indexFound);
                outputImgData[row_num][col_num] = tableValue;
            }
        }
                
        return outputImgData;
    }
    

}
