package apps;

import homography.HomographyError;
import homography.LinearHomography;
import core.TXT;
import core.XML;
import homography.nonlinear.NonLinearHomography;
import java.util.List;

public class NonLinearHomographyApp {
    
    public static void main(String[] args) {
        
        System.out.println("Running the NonLinearHomographyApp:");
        
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
        String linearHSavePath = conf.getString("/config/linearHSavePath");
        String nonlinearHSavePath = conf.getString("/config/nonlinearHSavePath");
        
        System.out.println("Done");
        
        // Load the image points
        List<List<Double>> uvPts = TXT.loadMatrix(imagePointsPath, Double.class);
        
        // Load the world points
        List<List<Double>> xyPts = TXT.loadMatrix(worldPointsPath, Double.class);
        
        // Compute the linear homography
        System.out.print("Compute the linear homography ... ");
        
        LinearHomography plane = new LinearHomography(xyPts, uvPts);
        plane.computeHomography();
        
        System.out.println("Done");
        
        // Save the linear homography
        List<List<Double>> linearH = plane.getHomography();
        TXT.saveMatrix(linearH, Double.class, linearHSavePath, formatString);
        
        // Compute linear reprojection error
        double linearError = HomographyError.computeReprojectError(xyPts, uvPts, linearH);
        System.out.println("The linear reprojection error is: " + String.format("%.6f", linearError));  
        
        // Compute the nonlinear homography
        System.out.print("Compute the nonlinear homography ... ");
        
        NonLinearHomography nonLinHomog = new NonLinearHomography();
        nonLinHomog.computeHomography(xyPts, uvPts, linearH);
        
        System.out.println("Done");
        
        // Save the non-linear homography
        List<List<Double>> nonLinearH = nonLinHomog.getHomography();
        TXT.saveMatrix(nonLinearH, Double.class, nonlinearHSavePath, formatString);
        
        // Compute non-linear reprojection error
        double nonLinearError = HomographyError.computeReprojectError(xyPts, uvPts, nonLinearH);
        System.out.println("The non-linear reprojection error is: " + String.format("%.6f", nonLinearError)); 
        
    }
}
