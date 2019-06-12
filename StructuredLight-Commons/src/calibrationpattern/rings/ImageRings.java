package calibrationpattern.rings;

import core.ArrayUtils;
import core.Contours;
import core.FilterKernal;
import core.Homography;
import core.ImageUtils;
import core.Quad;
import curvefitting.Paraboloid;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
    
    public static Map<String, List<List<Double>>> computeCenters_batch(Map<String, List<List<List<Integer>>>> contourSets, 
                                                          Map<String, Map<Integer, List<Integer>>> hierarchySets, 
                                                          int nPts) {
        Map<String, List<List<Double>>> output = new HashMap<>();
        for (String name: contourSets.keySet()) {
            List<List<List<Integer>>> contours = contourSets.get(name);
            Map<Integer, List<Integer>> hierarchy = hierarchySets.get(name);
            List<List<Double>> centers = ImageRings.computeCenters(contours, hierarchy, nPts);
            if (centers.size() == 0) {
                System.err.println("Could not find the correct number of center points in: " + name);
                continue;
            } else {
                output.put(name, centers);
            }            
        }
        return output;
    }
           
    public static List<List<Double>> computeCenters(List<List<List<Integer>>> edges, Map<Integer, List<Integer>> hierarchy, int nPts) {
        
        // Find the rings' parent
        int nChildren = nPts;
        Integer parentID = Contours.findParentID(hierarchy, nChildren);     
        
        // Stop if parent was not found
        if (parentID == null) {
            return new ArrayList<>();
        }
        
        // Compute the rings' centers        
        List<Integer> ringIDs = hierarchy.get(parentID);
        List<List<Double>> ringCenters = Contours.computeCenters(edges, ringIDs);

        return ringCenters;
    }    
    
    public static List<List<Double>> refineCenters(List<List<Double>> centers, BufferedImage grayImage, double outerWidth, double innerWidth) {
        
        // Compute kernal Sigma and kernal Size based on average ring dimensions
        double kernalSigma = innerWidth/2.0;        
        int kernalSize = (int)outerWidth;
        if (kernalSize%2==0) {
            kernalSize += 1;
        }
        
        // Create the Laplacian filter
        float[] kernalArray = FilterKernal.Laplacian(kernalSigma, kernalSize);
        
        // Run the Laplacian filter
        BufferedImage filteredImage = ImageUtils.convolve(grayImage, kernalSize, kernalArray);    
                  
        // Fit Paraboloid and Compute Center at each exisiting point
        List<List<Double>> newCenters = new ArrayList<>();
        Paraboloid para = new Paraboloid(filteredImage);
        for (List<Double> point: centers) {
            int centerX = (int) point.get(0).doubleValue();
            int centerY = (int) point.get(1).doubleValue();
            int width = (int) (0.2 * kernalSigma);
            int height = (int) (0.2 * kernalSigma);
            if (width < 2) {
                width = 2;
            }
            if (height < 2) {
                height = 2;
            }
            Map<String,Double> fit = para.fit(centerX, centerY, width, height);
            List<Double> newCenter = new ArrayList<>();
            newCenter.add(fit.get("x0"));
            newCenter.add(fit.get("y0"));
            newCenters.add(newCenter);
        }
        
        return newCenters;
    }
    
    public static List<List<Double>> sortCentersRowMajor(List<List<Double>> centers, int nRows, int nCols) {
        // Convert to Integer
        List<List<Integer>> centers_int = ArrayUtils.castArrayDouble_To_Integer(centers);

        // Find the convex hull around contour centers
        List<List<Integer>> hull_int = Contours.findConvexHull(centers_int);

        // Find the enclosing rectangle around contour centers
        List<List<Integer>> quad_int = Quad.findMaxAreaQuad(hull_int);

        // Sort the corner points
        List<List<Integer>> corners_int = Quad.sortCorners(quad_int);
        
        // Convert to Double
        List<List<Double>> corners = ArrayUtils.castArrayInteger_To_Double(corners_int);
        
        // Create Normalized Points
        List<List<Double>> normalPts = new ArrayList<>(4);
        normalPts.add(Arrays.asList(0.0,0.0));
        normalPts.add(Arrays.asList((double)(nCols-1),0.0));
        normalPts.add(Arrays.asList((double)(nCols-1),(double)(nRows-1)));
        normalPts.add(Arrays.asList(0.0,(double)(nRows-1)));

        // Compute the corners-only homography
        Homography h = new Homography(corners, normalPts);

        // Project all contour centers to normalized plane using the corners-only homography
        List<List<Double>> centers_proj = h.projectForward(centers);        
                
        Comparator<List<Double>> sort_y = new Comparator<List<Double>>() {
            @Override
            public int compare(List<Double> o1, List<Double> o2) {
                if (o1.get(1) > o2.get(1)) {
                    return 1;
                } else if (o1.get(1) < o2.get(1)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
        
        Comparator<List<Double>> sort_x = new Comparator<List<Double>>() {
            @Override
            public int compare(List<Double> o1, List<Double> o2) {
                if (o1.get(0) > o2.get(0)) {
                    return 1;
                } else if (o1.get(0) < o2.get(0)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
        
        Collections.sort(centers_proj, sort_y);        
        
        List<List<Double>> centers_proj_2D = new ArrayList<>();        
        for (int start = 0, stop = nCols; stop <= nRows*nCols; start+=nCols, stop+=nCols) {
            List<List<Double>> row = new ArrayList<>(centers_proj.subList(start, stop));
            Collections.sort(row, sort_x);
            for (List<Double> pt: row) {
                centers_proj_2D.add(pt);
            }
        }
        
        
        // Project the contour centers grid to image plane using the corners-only homography
        List<List<Double>> centers_sort = h.projectBackward(centers_proj_2D);
                
        return centers_sort;
    }

    
    
    
    
    
    
}
