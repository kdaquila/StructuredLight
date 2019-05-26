package testing_arrayutils;

import java.util.ArrayList;
import java.util.List;
import core.ArrayUtils;
import core.TXT;

public class testing_conversions {

    public static void main(String[] args) {
        test_ListToArrayDouble();
        test_ListToArrayInteger();
        test_ArrayToListDouble();
        test_ArrayToListInteger();
        test_ListToArrayDouble2D();
        test_ListToArrayInteger2D();
        test_ArrayToListDouble2D();
        test_ArrayToListInteger2D();
    }
    
    public static void test_ListToArrayDouble()
    {
        System.out.println("Testing: ListToArrayDouble");
        
        // make some data
        List<Double> vector = new ArrayList<>();
        Double k = 0.0;
        for (int j = 0; j < 6; j++){
            vector.add(k++);
        }
        
        // display it
        System.out.println("the list");
        System.out.println(vector);
        
        // convert it
        double[] output = ArrayUtils.ListToArray_Double(vector);
        
        // display it
        System.out.println("the array");
        for(double item: output)
        {
            System.out.print(item);
            System.out.print(", ");
        }
        System.out.println("\n");        
    }
    
    public static void test_ListToArrayInteger()
    {
        System.out.println("Testing: ListToArrayInteger");
        
        // make some data
        List<Integer> vector = new ArrayList<>();
        Integer k = 0;
        for (int j = 0; j < 6; j++){
            vector.add(k++);
        }
        
        // display it
        System.out.println("the list");
        System.out.println(vector);
        
        // convert it
        int[] output = ArrayUtils.ListToArray_Integer(vector);
        
        // display it
        System.out.println("the array");
        for(int item: output)
        {
            System.out.print(item);
            System.out.print(", ");
        }
        System.out.println("\n");        
    }
    
    public static void test_ArrayToListDouble()
    {
        System.out.println("Testing: ArrayToListDouble");
        
        // make some data
        double[] vector = new double[6];
        double k = 0.0;
        for (int j = 0; j < 6; j++){
            vector[j]=k++;
        }
        
        // display it
        System.out.println("the array");
        for(double item: vector)
        {
            System.out.print(item);
            System.out.print(", ");
        }
        System.out.println();  
        
        // convert it
        List<Double> output = ArrayUtils.ArrayToList_Double(vector);
        
        // display it
        System.out.println("the list");
        System.out.println(output);
        System.out.println("\n");        
    }
    
    public static void test_ArrayToListInteger()
    {
        System.out.println("Testing: ArrayToListInteger");
        
        // make some data
        int[] vector = new int[6];
        int k = 0;
        for (int j = 0; j < 6; j++){
            vector[j]=k++;
        }
        
        // display it
        System.out.println("the array");
        for(int item: vector)
        {
            System.out.print(item);
            System.out.print(", ");
        }
        System.out.println();  
        
        // convert it
        List<Integer> output = ArrayUtils.ArrayToList_Integer(vector);
        
        // display it
        System.out.println("the list");
        System.out.println(output);
        System.out.println("\n");        
    }
    
    public static void test_ListToArrayDouble2D()
    {
        System.out.println("Testing: ListToArrayDouble2D");
        
        // make some data
        List<List<Double>> matrix = new ArrayList<>();
        Double k = 0.0;
        for (int i = 0; i < 3; i++){
            matrix.add(new ArrayList<>());
            for (int j = 0; j < 3; j++){
                matrix.get(i).add(k++);
            }
        }
        
        // display it
        System.out.println("the list");
        System.out.println(matrix);
        
        // convert it
        double[][] output = ArrayUtils.ListToArray_Double2D(matrix);
        
        // display it
        System.out.println("the array");
        for(double[] row : output)
        {
            for(double item: row)
            {
                System.out.print(item);
                System.out.print(", ");
            }
            System.out.print("\n");
            
        }
        System.out.println("\n");        
    }
    
    public static void test_ListToArrayInteger2D()
    {
        System.out.println("Testing: ListToArrayInteger2D");
        
        // make some data
        List<List<Integer>> matrix = new ArrayList<>();
        Integer k = 0;
        for (int i = 0; i < 3; i++){
            matrix.add(new ArrayList<>());
            for (int j = 0; j < 3; j++){
                matrix.get(i).add(k++);
            }
        }
        
        // display it
        System.out.println("the list");
        System.out.println(matrix);
        
        // convert it
        int[][] output = ArrayUtils.ListToArray_Integer2D(matrix);
        
        // display it
        System.out.println("the array");
        for(int[] row : output)
        {
            for(int item: row)
            {
                System.out.print(item);
                System.out.print(", ");
            }
            System.out.print("\n");
            
        }
        System.out.println("\n");        
    }
    
    public static void test_ArrayToListDouble2D()
    {
        System.out.println("Testing: ArrayToListDouble2D");
        
        // make some data
        double[][] matrix = new double[3][3];
        double k = 0.0;
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++)
            {
                matrix[i][j]=k++;
            }
        }
        
        // display it
        System.out.println("the array");
        for(double[] row : matrix)
        {
            for(double item: row)
            {
                System.out.print(item);
                System.out.print(", ");
            }
            System.out.print("");
            
        }
        System.out.println();  
        
        // convert it
        List<List<Double>> output = ArrayUtils.ArrayToList_Double2D(matrix);
        
        // display it
        System.out.println("the list");
        System.out.println(output);
        System.out.println("\n");        
    }
    
    public static void test_ArrayToListInteger2D()
    {
        System.out.println("Testing: ArrayToListInteger2D");
        
        // make some data
        int[][] matrix = new int[3][3];
        int k = 0;
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++)
            {
                matrix[i][j]=k++;
            }
        }
        
        // display it
        System.out.println("the array");
        for(int[] row : matrix)
        {
            for(int item: row)
            {
                System.out.print(item);
                System.out.print(", ");
            }
            System.out.print("");
            
        }
        System.out.println();  
        
        // convert it
        List<List<Integer>> output = ArrayUtils.ArrayToList_Integer2D(matrix);
        
        // display it
        System.out.println("the list");
        System.out.println(output);
        System.out.println("\n");        
    }

}
