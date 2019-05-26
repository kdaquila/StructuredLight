package testing_contours;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import core.Contours;

public class test_center {

    public static void main(String[] args) {
        testing_findCenter();
    }
    
    public static void testing_findCenter()
    {
        System.out.println("Testing: find center");
        List<List<Integer>> contour = new ArrayList<>();
        contour.add(Arrays.asList(0,0));
        contour.add(Arrays.asList(20,0));
        contour.add(Arrays.asList(20,10));
        contour.add(Arrays.asList(0,10));
        List<Double> center = Contours.findCenter(contour);
        System.out.println(center);
        System.out.println("\n");
    }

}
