package apps;

import core.ArrayUtils;
import homography.HomographyError;
import homography.LinearHomography;
import core.TXT;
import core.XML;
import homography.Normalization;
import homography.nonlinear.NonLinearHomography;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class HomographyApp {
    
    public static void main(String[] args) {
        
        System.out.println("Running the HomographyApp:");
        
        // Validate arguments
        if (args.length == 0) {
            String errorMessage = "Please provide one argument which is the path to the XML configuration file";
            errorMessage += "Found path: " + args[0];
            throw new IllegalArgumentException(errorMessage);
        } 
        
        // Parse the arguments
        String configPath = args[0];
        
        // Load the configuration variables
        System.out.print("Loading the configuration ... ");
        
        XML conf = new XML(configPath);          
        String imagePointsDir = conf.getString("/config/imagePointsDir"); 
        String worldPointsPath = conf.getString("/config/worldPointsPath");
        String formatString = conf.getString("/config/formatString");
        String homographySaveDir = conf.getString("/config/homographySaveDir");
        boolean isRefine = conf.getBool("/config/isRefine");        
        
        System.out.println("Done");
        
        // Load the world xyz points
        List<List<Double>> xyzPts = TXT.loadMatrix(worldPointsPath, Double.class);   

        // Convert world xyz to xy points (drop Z, since it is constant)
        List<List<Double>> xyPts = ArrayUtils.dropZ(xyzPts);
        
        // Find the image point paths
        String[] imagePointFilenames = (new File(imagePointsDir)).list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                String lowerName = name.toLowerCase();
                boolean isValid = lowerName.endsWith(".txt") ||
                                  lowerName.endsWith(".csv"); 
                return isValid;
            }
        });
        
        // Validate file names
        if (imagePointFilenames == null || imagePointFilenames.length == 0) {
            throw new RuntimeException("No suitables files found in the input directory.");
        }
        
        // Merge all the image point sets
        List<List<Double>> uvPts_allViews = new ArrayList<>();
        for (String imagePointFilename: imagePointFilenames) {
            
            // Load the image points
            String imagePointsFullPath = Paths.get(imagePointsDir).resolve(imagePointFilename).toString();
            List<List<Double>> uvPts = TXT.loadMatrix(imagePointsFullPath, Double.class);
            
            // Append to master list
            for (List<Double> pt: uvPts) {
                uvPts_allViews.add(pt);
            }            
        }
        
        // Create the normalization matrices
        Normalization norm = new Normalization(xyPts, uvPts_allViews);  
        
        // Normalize the world xy points
        List<List<Double>> xyPts_norm = norm.normalizePointsXY(xyPts);        
            
        // Compute the homography for each view
        for (String imagePointFilename: imagePointFilenames) { 
            
            System.out.println("\n\nNow Processing Points from: " + imagePointFilename + "\n");
            
            // Load the image points for this view
            String imagePointsFullPath = Paths.get(imagePointsDir).resolve(imagePointFilename).toString();
            List<List<Double>> uvPts = TXT.loadMatrix(imagePointsFullPath, Double.class);
            
            // Normalize the image points
            List<List<Double>> uvPts_norm = norm.normalizePointsUV(uvPts);
            
            // Compute the linear homography
            System.out.print("Compute the linear homography ... ");

            LinearHomography linHomog = new LinearHomography(xyPts_norm, uvPts_norm);
            List<List<Double>> H_norm = linHomog.getHomography();
            List<List<Double>> H = norm.denormalizeHomographyMatrix(H_norm);    

            System.out.println("Done");
            
            // Compute linear reprojection error
            double linearError = HomographyError.computeReprojectError(xyPts, uvPts, H);
            System.out.println("The linear reprojection error is: " + String.format("%.6f", linearError));  

            if (isRefine) {
                // Compute the nonlinear homography
                System.out.print("Compute the nonlinear homography ... ");

                NonLinearHomography nonLinHomog = new NonLinearHomography(xyPts_norm, uvPts_norm, H_norm);
                H_norm = nonLinHomog.getHomography();
                H = norm.denormalizeHomographyMatrix(H_norm);
                        
                System.out.println("Done");
                
                // Compute non-linear reprojection error
                double nonLinearError = HomographyError.computeReprojectError(xyPts, uvPts, H);
                System.out.println("The non-linear reprojection error is: " + String.format("%.6f", nonLinearError)); 
            }
            
            // Save the homography matrix
            String baseFilename = imagePointFilename.split(Pattern.quote("."))[0];
            String homgraphyFilename = baseFilename + ".txt";
            String homgraphyFullPath = Paths.get(homographySaveDir).resolve(homgraphyFilename).toString();
            TXT.saveMatrix(H_norm, Double.class, homgraphyFullPath, formatString);
        }
    }
}
