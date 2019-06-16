package testing_contours;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import core.Contours;

public class test_convexHull {

    public static void main(String[] args) {
        testing_findConvexHull();
    }
    
    public static void testing_findConvexHull()
    {
        System.out.println("Testing: find convex hull");
        List<List<Integer>> contour = new ArrayList<>();
        contour.add(Arrays.asList(0,0));
        contour.add(Arrays.asList(13,2));
        contour.add(Arrays.asList(20,0));
        contour.add(Arrays.asList(23,6));
        contour.add(Arrays.asList(20,10));
        contour.add(Arrays.asList(0,10));
        List<List<Integer>> hull = Contours.findConvexHull(contour);
        System.out.println(hull);
        System.out.println("\n");
    }

}
