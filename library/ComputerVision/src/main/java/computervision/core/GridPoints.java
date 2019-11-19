package core;

import java.util.ArrayList;
import java.util.List;

public class GridPoints {
    
    int nRows;
    int nCols;
    
    public GridPoints(int nRows, int nCols) {
        this.nRows = nRows;
        this.nCols = nCols;
    }
        
    public List<List<Double>> rectify(List<List<Double>> points) {
        return new ArrayList<>();
    }
    
    public List<List<Double>> derectify(List<List<Double>> points) {
        return new ArrayList<>();
    }
    
    public List<List<Double>> sort(List<List<Double>> points) {
        return new ArrayList<>();
    }
    
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
}
