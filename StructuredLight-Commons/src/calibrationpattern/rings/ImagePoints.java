package calibrationpattern.rings;

import core.Contours;
import core.FilterKernal;
import core.ImageUtil;
import curvefitting.Paraboloid;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImagePoints {
           
    public static List<List<Double>> find(BufferedImage rgbImg, int nPts) {

        // Convert RGB to gray        
        BufferedImage grayImage = ImageUtil.color2Gray(rgbImg);

        // Adaptive threshold to black and white
        int windowSize = 21;
        int offset = 5;
        BufferedImage bwImage = ImageUtil.adaptiveThreshold(grayImage, windowSize, offset);

        // Find all outer edges
        Contours contours = new Contours(bwImage);
        int minArea = 200;            
        List<List<List<Integer>>> outerEdges = contours.findContours(minArea);  

        // Organize the edges into a hierarchy
        Map<Integer, List<Integer>> hierarchy = Contours.findHierarchy2(outerEdges); 

        // Find the rings' parent
        int nChildren = nPts;
        Integer parentID = Contours.findParentID(hierarchy, nChildren);        

        // Compute the rings' centers        
        List<Integer> ringIDs = hierarchy.get(parentID);
        List<List<Double>> ringCenters = Contours.computeCenters(outerEdges, ringIDs);

        return ringCenters;
    }
    
    
    public static List<List<Double>> refineSubPixel(BufferedImage rgbImg, List<List<Double>> points) {
        // Convert RGB to gray        
        BufferedImage grayImage = ImageUtil.color2Gray(rgbImg);
        
        // TODO Compute kernal Sigma and kernal Size based on average contour size
        double kernalSigma = 9.25;
        int kernalSize = 37;
        
        // Create the Laplacian filter
        float[] kernalArray = FilterKernal.Laplacian(kernalSigma, kernalSize);
        
        // Run the Laplacian filter
        BufferedImage filteredImage = ImageUtil.convolve(grayImage, kernalSize, kernalArray);
        
        // Fit Paraboloid and Compute Center at each exisiting point
        List<List<Double>> newCenters = new ArrayList<>();
        Paraboloid para = new Paraboloid(filteredImage);
        for (List<Double> point: points) {
            int x = (int) point.get(0).doubleValue();
            int y = (int) point.get(1).doubleValue();
            int w = (int) 7;
            int h = (int) 7;
            List<Double> coeffs = para.fit(x, y, w, h);
            List<Double> newCenter = new ArrayList<>();
            newCenter.add(coeffs.get(2));
            newCenter.add(coeffs.get(3));
            newCenters.add(newCenter);
        }
        
        return newCenters;
    }

    
    
    
    
    
    
}
