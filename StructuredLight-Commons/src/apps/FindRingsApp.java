package apps;

import calibrationpattern.rings.ImagePoints;
import core.ImageUtil;
import core.TXT;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.List;

public class FindRingsApp {
    
    public static void main(String[] args) {
        
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
        
        System.out.println("Done");
        
        // Load the image
        System.out.print("Loading the RGB image ... ");        
        
        BufferedImage rgbImage = ImageUtil.load(rgbImagePath);
        
        System.out.println("Done");
        
        // Find the rings centers    
        System.out.print("Searching for ring centers ... ");
        
        List<List<Double>> ringCenters = ImagePoints.find(rgbImage, nRings);
        
        System.out.println("Done");
        
        // Save the point data
        System.out.print("Saving the ring centers data... ");
        
        TXT.saveMatrix(ringCenters, Double.class, saveDataPath, formatString);
        
        System.out.println("Done");
    }

}
