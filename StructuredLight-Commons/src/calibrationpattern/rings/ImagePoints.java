package calibrationpattern.rings;

import core.Contours;
import core.ImageUtil;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImagePoints {
    
    public BufferedImage rgbImg;
    public BufferedImage grayImg;
    
    public ImagePoints(BufferedImage rgbImg) {
        this.rgbImg = rgbImg;
        this.grayImg = ImageUtil.color2Gray(rgbImg);
    }
    
    // Compute the normalized points
    
    // Loop 
    
        // Adaptive threshold to black and white

        // Find the contours

        // Find the contour hierarchy

        // Find the contour centers with shoelace formula
    
        // Find the contour centers with image correlation
    
        // If image was already rectified with all-point homography
    
            // Sort the contour centers into a grid
    
            // Project the contour centers to the original image plane using the previous all-point homography

        // If image was not already rectified

            // Find the convex hull around contour centers

            // Find the enclosing rectangle around contour centers

            // Find the corner points

            // Compute the corners-only homography
    
            // Project all contour centers to normalized plane using the corners-only homography
    
            // Sort the contour centers into a grid
    
            // Project the contour centers grid to image plane using the corners-only homography
    
        // Compute the all-points homography
    
        // Compute change in the all-point homography

            // If no change or max tries: Break
    
            // If change: Rectify the gray image using the all-point homography
    
    
    
    public void find(int nRows, int nCols) {

        // Convert RGB to gray
        System.out.println("Converting to grayscale: Please wait ...");
        BufferedImage grayImage = ImageUtil.color2Gray(rgbImg);

        // Adaptive threshold to black and white
        System.out.println("Adaptive thresholding to black and white: Please wait ...");
        int windowSize = 21;
        int offset = 5;
        BufferedImage bwImage = ImageUtil.adaptiveThreshold(grayImage, windowSize, offset);

        // Find the contours
        System.out.println("Looking for contours: Please wait ...");
        Contours contours = new Contours(bwImage);
        int minArea = 200;
        contours.findContours(minArea);    
        List<List<List<Integer>>> edges = contours.outerEdges;   

        // Organize the contours into a hierarchy
        System.out.println("Organizing Contours into a Hierarchy: Please wait ...");
        Map<Integer, List<Integer>> hierarchy = contours.findOuterEdgeHierarchy();        

        // Find the ring centers
        System.out.println("Computing Centers Points: Please wait ...");
        List<List<Double>> discCenters = new ArrayList<>();
        int nRings = nRows*nCols;        
        for (List<Integer> children: hierarchy.values()) {

            if (children.size() == nRings) {
                for (Integer childID: children) {
                    List<List<Integer>> childContour = edges.get(childID);
                    discCenters.add(Contours.computeCenter(childContour));
                }
                break;
            }
        }
        if (discCenters.isEmpty()) {
            throw new RuntimeException("Could not find the correct number of discs.");
        }
    }
}
