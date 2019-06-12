package apps;

import calibrationpattern.rings.ImageRings;
import core.Contours;
import core.ImageUtils;
import core.TXT;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import static core.Print.println;

public class CameraCalibrationApp {
    
    public static void main(String[] args) {
        
        println("Running the Camera Calibration App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];

        // Load the configuration variables      
        Map<String,Object> config = XML.loadMap(configAbsPath, "config");
        
        // Load RGB images
        println("Loading rgb images");
        String imagesRGBDir = (String) config.get("imagesRGBDir");
        Map<String, BufferedImage> rgbImages = ImageUtils.load_batch(imagesRGBDir);
        
        // Convert RGB to Gray images
        println("Converting rgb to gray");
        Map<String, BufferedImage> grayImages = ImageUtils.color2Gray_batch(rgbImages);
        
        // Save the gray images
        String imagesGrayDir = (String) config.get("imagesGrayDir");
        ImageUtils.save_batch(grayImages, imagesGrayDir);
        
        // Convert Gray to Black/White images
        println("Converting gray to bw");
        int adaptiveThresholdWindowSize = (Integer) config.get("adaptiveThresholdWindowSize");
        int adaptiveThresholdOffset = (Integer) config.get("adaptiveThresholdOffset");
        Map<String, BufferedImage> bwImages = ImageUtils.adaptiveThreshold_batch(grayImages, adaptiveThresholdWindowSize, adaptiveThresholdOffset);
        
        // Save the Black/White images
        String imagesBWDir = (String) config.get("imagesBWDir");
        ImageUtils.save_batch(bwImages, imagesBWDir);
        
        // Find the Contours/Contour-Hierarchy        
        double contoursMinArea = (Double) config.get("contoursMinArea");
        println("Finding contours");
        Map<String, List<List<List<Integer>>>> contourSets = Contours.findContours_batch(bwImages, contoursMinArea);
        println("Finding contour hierarchies");
        Map<String, Map<Integer, List<Integer>>> hierarchySets = Contours.findHierarchy_batch(contourSets);
        
        // Find Image Points
        println("Finding image points");
        int nRows = (Integer) config.get("nRows");
        int nCols = (Integer) config.get("nCols");
        int nPts = nRows*nCols;
        Map<String, List<List<Double>>> imagePointSets = ImageRings.computeCenters_batch(contourSets, hierarchySets, nPts);
        
        // Save the Image Points
        String imagePointsDir = (String) config.get("imagePointsDir");
        String saveDataFormatString = (String) config.get("saveDataFormatString");
        String saveDataDelimiter = (String) config.get("saveDataDelimiter");
        String saveDataEndOfLine = "\n";
        Boolean saveDataAppend = false;
        TXT.saveMatrix_batch(imagePointSets, Double.class, imagePointsDir, saveDataFormatString, saveDataDelimiter, saveDataEndOfLine, saveDataAppend);
        
        // Generate and Save the World Points
        
        // Compute and Save the homography matrices
        
        // Compute and Save the intrinsic matrix
        
        // Compute and Save the extrinsic matrices
        
        // Compute and Save the radial distortion coefficients
        
        // Compute and Save the refined intrinsic/extrinsice/distortions
        
        // Compute and Save ront-parallel gray images
        
        
    }
    

}
