package apps;

import calibrationpattern.rings.ImageRings;
import core.Contours;
import core.ImageUtil;
import core.TXT;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class FindRingsApp {
    
    public static void main(String[] args) {
        System.out.println("Running the FindRingsApp:");
        
        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Please provide one argument which is the path to the XML configuration file");
        } 
        
        // Parse the arguments
        String configPath = args[0];
        
        // Load the configuration variables
        System.out.print("Loading the configuration ... ");
        
        XML conf = new XML(configPath);      
        int nRings = conf.getInt("/config/nRings");
        String formatString = conf.getString("/config/formatString"); 
        String rgbImagePath = conf.getString("/config/rgbImagePath"); 
        String saveDataPath = conf.getString("/config/saveDataPath");
        String subPixelSaveDataPath = conf.getString("/config/subPixelSaveDataPath");
        boolean isSubPixel = conf.getBool("/config/isSubPixel");
        
        System.out.println("Done");
        
        // Load the RGB image
        System.out.print("Loading the RGB image ... ");        
        
        BufferedImage rgbImage = ImageUtil.load(rgbImagePath);
        
        System.out.println("Done");
        
        // Convert RGB to gray        
        BufferedImage grayImage = ImageUtil.color2Gray(rgbImage);

        // Adaptive threshold to black and white        
        System.out.print("Thresholding to black and white ... "); 
        
        int windowSize = 21;
        int offset = 5;
        BufferedImage bwImage = ImageUtil.adaptiveThreshold(grayImage, windowSize, offset);
        
        System.out.println("Done");
        
        // Find all contours
        System.out.print("Searching for contours ... ");
        Contours contours = new Contours(bwImage);   
        int minArea = 200; 
        List<List<List<Integer>>> edges = contours.findContours(minArea);  
        
        System.out.println("Done");

        // Organize the edges into a hierarchy
        System.out.print("Indexing the contours ... ");
        
        Map<Integer, List<Integer>> hierarchy = Contours.findHierarchy(edges); 
        
        System.out.println("Done");
        
        // Find the rings centers    
        System.out.print("Computing the ring centers ... ");        
           
        List<List<Double>> ringCenters = ImageRings.computeCenters(edges, hierarchy, nRings);
        
        System.out.println("Done");      
        
        // Save the point data
        System.out.print("Saving the ring centers data... ");

        TXT.saveMatrix(ringCenters, Double.class, saveDataPath, formatString);

        System.out.println("Done");
        
        // Refine ring centers to sub pixel accuracy
        System.out.print("Refining ring centers to subPixel Accuracy... ");
        
        if (isSubPixel) {
            // Compute the average ring outer radius
            Map<String,Double> averageWidths = ImageRings.findAvgRingWidths(edges, hierarchy, nRings);
            double ringOuterRadius = averageWidths.get("Outer");
            double ringInnerRadius = averageWidths.get("Inner");            
            
            // Find the ring centers to subPixel accuracy  
            List<List<Double>> subPixelRingCenters = ImageRings.refineCenters(ringCenters, grayImage,
                                                                              ringOuterRadius, ringInnerRadius);

            System.out.println("Done");

            // Save the point data
            System.out.print("Saving the subPixel ring centers data... ");

            TXT.saveMatrix(subPixelRingCenters, Double.class, subPixelSaveDataPath, formatString);

            System.out.println("Done");                        
        }      
        
    }

}
