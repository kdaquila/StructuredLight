package testing_quad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import core.TXT;
import core.Quad;

public class test_findMaxAreaQuad {

    public static void main(String[] args) {
        test_findMaxAreaQuad();
    }

    public static void test_findMaxAreaQuad()
    {
        System.out.println("Testing: findMaxAreaQuad(List<Double> ...)");
        
        // create some data
        List<List<Integer>> points = new ArrayList<>();
        
        points.add(Arrays.asList(5,1));
        points.add(Arrays.asList(6,3));
        points.add(Arrays.asList(5,5));
        points.add(Arrays.asList(1,5));
        points.add(Arrays.asList(1,1));
        points.add(Arrays.asList(2,1));
        
        // display the original points
        System.out.println(TXT.MatrixToString(points, "%d", ",", "\n"));
        
        // save the original points
        String folder = "C:\\Users\\kfd18\\Downloads";
        String filename = "original_points.txt";
        TXT.saveMatrix(points, Integer.class, folder, filename, false, "%d", ",", "\n");
        
        List<List<Integer>> quadPoints = Quad.findMaxAreaQuad(points);
        
        // display the quad points
        System.out.println(TXT.MatrixToString(quadPoints, "%d", ",", "\n"));
        
        // save the original points
        folder = "C:\\Users\\kfd18\\Downloads";
        filename = "quad_points.txt";
        TXT.saveMatrix(quadPoints, Integer.class, folder, filename, false, "%d", ",", "\n");
        
    }
}
