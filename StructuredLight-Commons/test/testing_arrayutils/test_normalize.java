package testing_arrayutils;

import java.util.ArrayList;
import java.util.List;
import core.ArrayUtils;
import core.TXT;

public class test_normalize {

    public static void main(String[] args) {
        testing_normalize();
    }
    
    public static void testing_normalize()
    {
        // create a matrix
        List<List<Double>> matrix = new ArrayList<>();
        Double k = 5.0;
        for (int i = 0; i < 4; i++)
        {
            matrix.add(new ArrayList<>());
            for (int j = 0; j < 3; j++)
            {
                matrix.get(i).add(k++);
            }
        }
        // show the matrix
        System.out.println(TXT.MatrixToString(matrix, "%.1f", ",", "\n"));
        
        // normalize it
        List<List<Double>> output = ArrayUtils.normalizeDoubleList(matrix);
        
        // show the matrix
        System.out.println(TXT.MatrixToString(output, "%.1f", ",", "\n"));
        
    }

}
