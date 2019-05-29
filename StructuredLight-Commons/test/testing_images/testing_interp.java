package testing_images;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import core.ImageUtil;
import core.Interpolation;

public class testing_interp {

    public static void main(String[] args) {
        testing_Interpolation();
    }
    
    public static void testing_Interpolation()
    {
        System.out.println("Testing: Interpolation");
        
        // define file
        String load_folder = "F:\\kdaquila_SoftwareLibraries\\Java\\kdaquila\\StructuredLight-Commons\\Test_Resources";
        String load_filename = "Image5_gray.png";
        
        // load the gray
        BufferedImage grayImage = ImageUtil.load(load_folder, load_filename); 
        
        // do the interpolation
        List<List<Double>> points = new ArrayList<>(6);     
        double y = 2.75;
        for (double x = 0.0; x < 6; x++)
        {
            List<Double> newPoint = new ArrayList<>(2);
            newPoint.add(x);
            newPoint.add(y);
            points.add(newPoint);
        }
        List<Double> values = Interpolation.interpolate_1D(grayImage, points);
        
        System.out.println("the points are: " + points);
        System.out.println("the interpolated values are: " + values);
        
        System.out.println("\n");
    }

}
