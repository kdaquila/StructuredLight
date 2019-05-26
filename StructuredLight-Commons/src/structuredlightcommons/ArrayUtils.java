package structuredlightcommons;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtils
{
    
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
    
    public static List<List<Double>> normalizeDoubleArray(List<List<Double>> matrix){
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

}
