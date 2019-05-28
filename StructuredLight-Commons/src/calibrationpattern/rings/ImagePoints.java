package calibrationpattern.rings;

import core.Contours;
import core.ImageUtil;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImagePoints {
    
    public static void find() {
    // Load the rgb image
    System.out.println("Loading the rgb image: Please wait ...");
    BufferedImage rgbImage = ImageUtil.load(rgbImgPath);

    // Convert RGB to gray
    System.out.println("Converting to grayscale: Please wait ...");
    BufferedImage grayImage = ImageUtil.color2Gray(rgbImage);

    // Adaptive threshold to black and white
    System.out.println("Adaptive thresholding to black and white: Please wait ...");
    int windowSize = 21;
    int offset = 5;s
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
