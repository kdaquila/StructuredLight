package calibrationpattern.rings;

import core.Contours;
import core.FilterKernal;
import core.ImageUtil;
import curvefitting.Paraboloid;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageRings {
    
    public static Map<String,Double> findAvgRingWidths(List<List<List<Integer>>> edges, Map<Integer, List<Integer>> hierarchy, int nPts) {
        // Find the outer rings' parent
        int nChildren = nPts;
        Integer parentID = Contours.findParentID(hierarchy, nChildren);        

        // Find the outer rings' IDs
        List<Integer> outerRingIDs = hierarchy.get(parentID);
        
        // Find the inner ring IDs
        List<Integer> innerRingIDs = new ArrayList<>();
        for (Integer outerRingID: outerRingIDs) {
            innerRingIDs.add(hierarchy.get(outerRingID).get(0));
        }
        
        // Compute the average widths
        Map<String,Double> averageWidths = new HashMap<>();
        Double avgInnerWidth = Contours.computeAverageWidth(edges, innerRingIDs);
        Double avgOuterWidth = Contours.computeAverageWidth(edges, outerRingIDs);
        
        averageWidths.put("Outer", avgOuterWidth);
        averageWidths.put("Inner", avgInnerWidth);
        
        return averageWidths;
    }
           
    public static List<List<Double>> computeCenters(List<List<List<Integer>>> edges, Map<Integer, List<Integer>> hierarchy, int nPts) {
        
        // Find the rings' parent
        int nChildren = nPts;
        Integer parentID = Contours.findParentID(hierarchy, nChildren);        

        // Compute the rings' centers        
        List<Integer> ringIDs = hierarchy.get(parentID);
        List<List<Double>> ringCenters = Contours.computeCenters(edges, ringIDs);

        return ringCenters;
    }    
    
    public static List<List<Double>> refineCenters(List<List<Double>> centers, BufferedImage grayImage, double outerRadius, double innerRadius) {
        
        // Compute kernal Sigma and kernal Size based on average ring dimensions
        double kernalSigma = innerRadius;
        int kernalSize = (int)outerRadius*2+1;
        
        // Create the Laplacian filter
        float[] kernalArray = FilterKernal.Laplacian(kernalSigma, kernalSize);
        
        // Run the Laplacian filter
        BufferedImage filteredImage = ImageUtil.convolve(grayImage, kernalSize, kernalArray);    
                
        // Fit Paraboloid and Compute Center at each exisiting point
        List<List<Double>> newCenters = new ArrayList<>();
        Paraboloid para = new Paraboloid(filteredImage);
        for (List<Double> point: centers) {
            int centerX = (int) point.get(0).doubleValue();
            int centerY = (int) point.get(1).doubleValue();
            int width = (int) (0.4 * innerRadius);
            int height = (int) (0.4 * innerRadius);
            Map<String,Double> fit = para.fit(centerX, centerY, width, height);
            List<Double> newCenter = new ArrayList<>();
            newCenter.add(fit.get("x0"));
            newCenter.add(fit.get("y0"));
            newCenters.add(newCenter);
        }
        
        return newCenters;
    }

    
    
    
    
    
    
}
