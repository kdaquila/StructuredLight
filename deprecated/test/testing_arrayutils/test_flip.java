package testing_arrayutils;

import java.util.ArrayList;
import java.util.List;
import core.ArrayUtils;
import core.TXT;

public class test_flip {

    public static void main(String[] args) {
        test_fliplr();
        test_flipud();
    }

    public static void test_fliplr()
    {
        System.out.println("Testing: fliplr(<List<List<Double>> ...)");
        
        // create a matrix
        List<List<Double>> matrix = new ArrayList<>();
        Double k = 0.0;
        for (int i = 0; i < 4; i++)
        {
            matrix.add(new ArrayList<>());
            for (int j = 0; j < 3; j++)
            {
                matrix.get(i).add(k++);
            }
        }
        
        // show the matrix
        System.out.println(TXT.MatrixToString(matrix, "%.2f", ",", "\n"));
        
        // flip
        List<List<Double>> output = ArrayUtils.fliplr(matrix);
        
        // show the flipped matrix
        System.out.println(TXT.MatrixToString(output, "%.2f", ",", "\n"));
    }
    
    public static void test_flipud()
    {
        System.out.println("Testing: flipud(<List<List<Double>> ...)");
        
        // create a matrix
        List<List<Double>> matrix = new ArrayList<>();
        Double k = 0.0;
        for (int i = 0; i < 4; i++)
        {
            matrix.add(new ArrayList<>());
            for (int j = 0; j < 3; j++)
            {
                matrix.get(i).add(k++);
            }
        }
        
        // show the matrix
        System.out.println(TXT.MatrixToString(matrix, "%.2f", ",", "\n"));
        
        // flip
        List<List<Double>> output = ArrayUtils.flipud(matrix);
        
        // show the flipped matrix
        System.out.println(TXT.MatrixToString(output, "%.2f", ",", "\n"));
    }
}
