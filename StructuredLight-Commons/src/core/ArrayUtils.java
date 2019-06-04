package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayUtils
{
    public static List<List<Integer>> castArrayDouble_To_Integer(List<List<Double>> input) {
        List<List<Integer>> output = new ArrayList<>();
        for (List<Double> oldRow: input) {
            List<Integer> newRow = new ArrayList<>();
            for (Double item: oldRow) {                
                newRow.add(item.intValue());
            }
            output.add(newRow);
        }        
        return output;
    }
    
    public static List<List<Double>> castArrayInteger_To_Double(List<List<Integer>> input) {
        List<List<Double>> output = new ArrayList<>();
        for (List<Integer> oldRow: input) {
            List<Double> newRow = new ArrayList<>();
            for (Integer item: oldRow) {                
                newRow.add(item.doubleValue());
            }
            output.add(newRow);
        }        
        return output;
    }
    
    public static <T> List<List<T>> addDim_1Dto2D(List<T> list1D) {
        List<List<T>> list2D = new ArrayList<>(1);
        list2D.add(list1D);
        return list2D;
    }
    
    public static <T> List<List<T>> zipLists(List<T> list1, List<T> list2) {
        int nPts = list1.size();
        List<List<T>> list2D = new ArrayList<>(nPts);        
        for (int i = 0; i < nPts; i++) {
            List<T> newRow = new ArrayList<>(2);
            newRow.add(list1.get(i));
            newRow.add(list2.get(i));
            list2D.add(newRow);
        }
        return list2D;
    }
    
    public static <T> void unzipList(List<List<T>> inputList2D, List<T> outList1, List<T> outList2) {
        for (List<T> row: inputList2D) {
            outList1.add(row.get(0));
            outList2.add(row.get(1));
        }
    }
    
    public static <T> List<List<T>> reshape(List<List<T>> array, int new_nRows, int new_nCols) 
    {
        // get dimensions of the array
        int nRows = array.size();
        int nCols = array.get(0).size(); 
        
        // create the new array
        List<List<T>> new_array = new ArrayList<>();        
        
        // old array index
        int k = 0;
        
        // iterate over new array
        for (int new_row_num = 0; new_row_num < new_nRows; new_row_num++)
        {
            List<T> newRow = new ArrayList<>();
            for (int new_col_num = 0; new_col_num < new_nCols; new_col_num++)
            {                
                // compute position in old array
                int row_num = k/nCols;
                int col_num = k - nCols*row_num;      
                
                // update new array
                newRow.add(array.get(row_num).get(col_num));
                
                // advance old array index
                k++;
            }
            new_array.add(newRow);
        }
        
        return new_array;
    }
    
    public static <T> List<List<T>> fliplr(List<List<T>> matrix) 
    {
        List<List<T>> output = new ArrayList<>();

        // get dimensions
        int nRows = matrix.size();
        int nCols = matrix.get(0).size();

        // copy 
        for (int row_num = 0; row_num < nRows; row_num++) 
        {
            List<T> newRow = new ArrayList<>();
            for (int col_num = nCols-1; col_num >= 0; col_num--) 
            {
                newRow.add(matrix.get(row_num).get(col_num));
            }
            output.add(newRow);
        }

        return output;
    }

    public static <T> List<List<T>> flipud(List<List<T>> matrix) 
    {
        List<List<T>> output = new ArrayList<>();

        // get dimensions
        int nRows = matrix.size();

        // copy 
        for (int row_num = nRows-1; row_num >= 0; row_num--) 
        {
            List<T> newRow = new ArrayList<>(matrix.get(row_num));
            output.add(newRow);
        }

        return output;
    }
    
    public static double[] ListToArray_Double(List<Double> input)
    {
        double[] output = new double[input.size()];
        for(int i = 0; i < input.size(); i++)
        {
            output[i] = input.get(i);
        }
        return output;
    }
    
    public static int[] ListToArray_Integer(List<Integer> input)
    {
        int[] output = new int[input.size()];
        for(int i = 0; i < input.size(); i++)
        {
            output[i] = input.get(i);
        }
        return output;
    }
    
    public static List<Double> ArrayToList_Double(double[] input)
    {
        List<Double> output = new ArrayList<>(input.length);
        for(int i = 0; i < input.length; i++)
        {
            output.add(input[i]);
        }
        return output;
    }
    
    public static List<Float> ArrayToList_Float(float[] input)
    {
        List<Float> output = new ArrayList<>(input.length);
        for(int i = 0; i < input.length; i++)
        {
            output.add(input[i]);
        }
        return output;
    }
    
    public static List<Integer> ArrayToList_Integer(int[] input)
    {
        List<Integer> output = new ArrayList<>(input.length);
        for(int i = 0; i < input.length; i++)
        {
            output.add(input[i]);
        }
        return output;
    }
    
    
    public static double[][] ListToArray_Double2D(List<List<Double>> input)
    {
        double[][] output = new double[input.size()][input.get(0).size()];
        for(int i = 0; i < input.size(); i++)
        {
            for(int j = 0; j < input.get(0).size(); j++)
            {
                output[i][j] = input.get(i).get(j);
            }            
        }
        return output;
    }
    
    public static int[][] ListToArray_Integer2D(List<List<Integer>> input)
    {
        int[][] output = new int[input.size()][input.get(0).size()];
        for(int i = 0; i < input.size(); i++)
        {
            for(int j = 0; j < input.get(0).size(); j++)
            {
                output[i][j] = input.get(i).get(j);
            }            
        }
        return output;
    }
    
    public static List<List<Double>> ArrayToList_Double2D(double[][] input)
    {
        List<List<Double>> output = new ArrayList<>(input.length);
        for(int i = 0; i < input.length; i++)
        {
            List<Double> newRow = new ArrayList<>(input[0].length);
            for(int j = 0; j < input[0].length; j++)
            {
                newRow.add(input[i][j]);
            }
            output.add(newRow);
        }
        return output;
    }
    
    public static List<List<Integer>> ArrayToList_Integer2D(int[][] input)
    {
        List<List<Integer>> output = new ArrayList<>(input.length);
        for(int i = 0; i < input.length; i++)
        {
            List<Integer> newRow = new ArrayList<>(input[0].length);
            for(int j = 0; j < input[0].length; j++)
            {
                newRow.add(input[i][j]);
            }
            output.add(newRow);
        }
        return output;
    }
    
    public static int closestIndexOf(int value, List<Integer> in)
    {
        int min = Integer.MAX_VALUE;
        int closest_item = 0;
        for (Integer item : in)
        {
            int diff = Math.abs(item - value);
            if (diff < min)
            {
                min = diff;
                closest_item = item;
            }
        }
        // Minimum didn't change
        if (min == Integer.MAX_VALUE) {
            throw new RuntimeException("Could not find the closest item.");
        }
        
        int closestIndex = in.indexOf(closest_item);
        return closestIndex;
    }
    
    public static float[] normalizeFloatArray(float[] input, float newMax, float newMin) {
        
        int nPts = input.length;
        
        // store in a list
        List<Float> inputList = new ArrayList<>();
        for (float item: input) {
            inputList.add(item);
        }

        // find the min
        Float minVal = Collections.min(inputList);
        
        // subtract all elements by the min
        for (int i=0; i<nPts; i++) {
            inputList.set(i, inputList.get(i) - minVal);
        }        
        
        // find the max
        Float maxVal = Collections.max(inputList);
        
        // rescale
        for (int i=0; i<nPts; i++) {
            inputList.set(i, inputList.get(i)/maxVal*(newMax - newMin) + newMin);
        }
        
        // store back in array
        float[] outputArray = new float[nPts];
        for (int i=0; i<nPts; i++) {
            outputArray[i] = inputList.get(i);
        } 
        
        return outputArray;
    }
    
    public static List<List<Double>> normalizeDoubleList(List<List<Double>> matrix){
        // find the min
        double min = min_Double2D(matrix);

        // subtract all elements by the min
        List<List<Double>> array1 = scalarAdd_Double2D(matrix, -min);
        
        // find the max
        double max = max_Double2D(array1);

        // rescale all elements
        List<List<Double>> array2 = scalarMultiply_Double2D(array1, 255.0/max);
        
        return array2;
    }   
    
    public static double mean_Double1D(List<Double> vector)
    {
        // convert to array
        double[] array = ListToArray_Double(vector);
        
        double sum = 0;
        int nPts = array.length;
        for (int i = 0; i < nPts; i++)
        {
            double item = array[i];
            sum += item;
        }
        return sum/nPts;
    }    
    
    public static double mean_Double2D(List<List<Double>> matrix)
    {
        // convert to array
        double[][] array = ListToArray_Double2D(matrix);
        
        double sum = 0;
        int nRows = array.length;
        int nCols = array[0].length;
        for (int row_num = 0; row_num < nRows; row_num++)
        {
            for (int col_num = 0; col_num < nCols; col_num++)
            {
                double item = array[row_num][col_num];
                sum += item;
            }
        }
        double nPts = nRows*nCols;
        return sum/nPts;
    }
    
    public static double avgDist(List<Double> xList, List<Double> yList, double xMean, double yMean) {
        int nPts = xList.size();
        double distSum = 0;
        for (int pt_num = 0; pt_num < nPts; pt_num++) {
            double xDistSqr = Math.pow(xList.get(pt_num) - xMean, 2);
            double yDistSqr = Math.pow(yList.get(pt_num) - yMean, 2);
            distSum += Math.sqrt(xDistSqr + yDistSqr );       
        }        
        return distSum/nPts;
    }

    public static double var_Double1D(List<Double> vector)
    {
        // convert to array
        double[] array = ListToArray_Double(vector);
        
        double mean = mean_Double1D(vector);
        
        double sumSqr = 0;
        int nPts = array.length;
        for (int i = 0; i < nPts; i++)
        {
            double item = array[i];
            sumSqr += Math.pow(item - mean, 2);
        }
        return sumSqr/nPts;
    }     
    
    public static double var_Double2D(List<List<Double>> matrix)
    {
        // convert to array
        double[][] array = ListToArray_Double2D(matrix);
        
        double mean = mean_Double2D(matrix);
        
        double sumSqr = 0;
        int nRows = array.length;
        int nCols = array[0].length;
        for (int row_num = 0; row_num < nRows; row_num++)
        {
            for (int col_num = 0; col_num < nCols; col_num++)
            {
                double item = array[row_num][col_num];
                sumSqr += Math.pow(item - mean, 2);
            }
        }
        double nPts = nRows*nCols;
        return sumSqr/nPts;
    } 
    
    public static double std_Double1D(List<Double> vector)
    {
        return Math.sqrt(var_Double1D(vector));
    }
    
    public static double std_Double2D(List<List<Double>> matrix)
    {
        return Math.sqrt(var_Double2D(matrix));
    }

    public static double max_Double2D(List<List<Double>> matrix)
    {
        // convert to array
        double[][] array = ListToArray_Double2D(matrix);
        
        double maxValue = Double.MIN_VALUE;
        int nRows = array.length;
        int nCols = array[0].length;
        for (int row_num = 0; row_num < nRows; row_num++)
        {
            for (int col_num = 0; col_num < nCols; col_num++)
            {
                double item = array[row_num][col_num];
                if (item > maxValue)
                {
                    maxValue = item;
                }
            }
        }
        return maxValue;
    }

    public static double min_Double2D(List<List<Double>> matrix)
    {        
        // convert to array
        double[][] array = ListToArray_Double2D(matrix);
        
        double minValue = Double.MAX_VALUE;
        int nRows = array.length;
        int nCols = array[0].length;
        for (int row_num = 0; row_num < nRows; row_num++)
        {
            for (int col_num = 0; col_num < nCols; col_num++)
            {
                double item = array[row_num][col_num];
                if (item < minValue)
                {
                    minValue = item;
                }
            }
        }
        return minValue;
    }

    public static List<List<Double>> scalarAdd_Double2D(List<List<Double>> matrix, double addend)
    {
        // convert to array
        double[][] array = ListToArray_Double2D(matrix);
        
        // get dimensions
        int nRows = array.length;
        int nCols = array[0].length;
        
        // operate on each element
        for (int row_num = 0; row_num < nRows; row_num++) {
            for (int col_num = 0; col_num < nCols; col_num++) {
                array[row_num][col_num] += addend;
            }
        }
        
        // convert to list
        List<List<Double>> output = ArrayToList_Double2D(array);
        
        return output;
    }

    public static List<List<Double>> scalarMultiply_Double2D(List<List<Double>> matrix, double factor)
    {
        // convert to array
        double[][] array = ListToArray_Double2D(matrix);
        
        // get dimensions
        int nRows = array.length;
        int nCols = array[0].length;
        
        // operate on each element
        for (int row_num = 0; row_num < nRows; row_num++) {
            for (int col_num = 0; col_num < nCols; col_num++) {
                array[row_num][col_num] *= factor;
            }
        }
        
        // convert to list
        List<List<Double>> output = ArrayToList_Double2D(array);
        
        return output;
    }
    
    public static void printList_Double2D(List<List<Double>> list) {
        printList_Double2D(list, "%+03.3e");
    }
    
    public static void printList_Double2D(List<List<Double>> list, String formatStr) {
        for (int row_ind = 0; row_ind < list.size(); row_ind++) {      
            List<Double> row = list.get(row_ind);
            System.out.print("row " + String.format("%02d", row_ind) + ": ");
            for (int col_ind = 0; col_ind < row.size(); col_ind++) {
                Double item = row.get(col_ind);                
                if (col_ind == row.size() - 1) {
                    System.out.print(String.format(formatStr, item));
                } else {
                    System.out.print(String.format(formatStr, item) + ",");
                } 
            }  
            System.out.print("\n");
        }
        
    }

}
