package apps;

import homography.HomographyError;
import homography.LinearHomography;
import core.TXT;
import core.XML;
import java.util.List;

public class LinearHomographyApp {
    
    public static void main(String[] args) {
        
        System.out.println("Running the LinearHomographyApp:");
        
        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Please provide one argument which is the path to the XML configuration file");
        } 
        
        // Parse the arguments
        String configPath = args[0];
        
        // Load the configuration variables
        System.out.print("Loading the configuration ... ");
        
        XML conf = new XML(configPath);          
        String imagePointsPath = conf.getString("/config/imagePointsPath"); 
        String worldPointsPath = conf.getString("/config/worldPointsPath");
        String formatString = conf.getString("/config/formatString");
        String HSavePath = conf.getString("/config/HSavePath");
        
        System.out.println("Done");
        
        // Load the image points
        List<List<Double>> imagePts = TXT.loadMatrix(imagePointsPath, Double.class);
        
        // Load the world points
        List<List<Double>> worldPts = TXT.loadMatrix(worldPointsPath, Double.class);
        
        // Compute the homography
        System.out.print("Compute the homography ... ");
        
        LinearHomography plane = new LinearHomography(worldPts, imagePts);
        plane.computeHomography();
        
        System.out.println("Done");
        
        // Save the homography
        List<List<Double>> H = plane.getHomography();
        TXT.saveMatrix(H, Double.class, HSavePath, formatString);
        
        // Compute reprojection error
        double error = HomographyError.computeReprojectError(worldPts, imagePts, H);
        System.out.println("The reprojection error is: " + String.format("%.3f", error));               
        
    }
}
