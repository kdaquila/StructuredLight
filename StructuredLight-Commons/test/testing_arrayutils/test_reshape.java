package testing_arrayutils;

import java.util.ArrayList;
import java.util.List;
import structuredlightcommons.ArrayUtils;
import structuredlightcommons.TXT;

public class test_reshape
{

    public static void main(String[] args) 
    {
        test_reshape_int();
        test_reshape_double();
    }

    public static void test_reshape_int()
    {
        System.out.println("Testing: reshape(<List<List<Integer>> ...)");
        
        // create a matrix
        List<List<Integer>> matrix = new ArrayList<>();
        Integer k = 0;
        for (int i = 0; i < 12; i++)
        {
            matrix.add(new ArrayList<>());
            for (int j = 0; j < 1; j++)
            {
                matrix.get(i).add(k++);
            }
        }
        
        // show the matrix
        System.out.println(TXT.MatrixToString(matrix, "%d", ",", "\n"));
        
        // reshape it
        List<List<Integer>> output = ArrayUtils.reshape(matrix, 4, 3);
        
        // show the output
        System.out.println(TXT.MatrixToString(output, "%d", ",", "\n"));
    }
    
    public static void test_reshape_double()
    {
        System.out.println("Testing: reshape(<List<List<Double>> ...)");
        
        // create a matrix
        List<List<Double>> matrix = new ArrayList<>();
        Double k = 0.0;
        for (int i = 0; i < 12; i++)
        {
            matrix.add(new ArrayList<>());
            for (int j = 0; j < 1; j++)
            {
                matrix.get(i).add(k++);
            }
        }
        
        // show the matrix
        System.out.println(TXT.MatrixToString(matrix, "%.2f", ",", "\n"));
        
        // reshape it
        List<List<Double>> output = ArrayUtils.reshape(matrix, 4, 3);
        
        // show the output
        System.out.println(TXT.MatrixToString(output, "%.2f", ",", "\n"));
    }
    
}
