package apps;

import homography.HomographyError;
import homography.LinearHomography;
import core.TXT;
import core.XML;
import homography.nonlinear.NonLinearHomography;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
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
        
        // Find the image point paths
        File imagePointsDir_file = new File(imagePointsDir);
        String[] imagePointFilenames = imagePointsDir_file.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                String lowerName = name.toLowerCase();
                boolean isValid = lowerName.endsWith(".txt") ||
                                  lowerName.endsWith(".csv"); 
                return isValid;
            }
        });
        
        if (imagePointFilenames == null || imagePointFilenames.length == 0) {
            throw new RuntimeException("No suitables files found in the input directory.");
        }
        
        for (String imagePointFilename: imagePointFilenames) {
            
            System.out.println("\n\nNow Processing Points from: " + imagePointFilename + "\n");

            // Load the image points
            String imagePointsFullPath = Paths.get(imagePointsDir).resolve(imagePointFilename).toString();
            List<List<Double>> uvPts = TXT.loadMatrix(imagePointsFullPath, Double.class);

            // Load the world points
            List<List<Double>> xyPts = TXT.loadMatrix(worldPointsPath, Double.class);

            // Compute the linear homography
            System.out.print("Compute the linear homography ... ");

            LinearHomography linHomog = new LinearHomography(xyPts, uvPts);
            List<List<Double>> H = linHomog.getHomography();
            List<List<Double>> H_norm = linHomog.getNormalizedHomography();

            System.out.println("Done");
            
            // Compute linear reprojection error
            double linearError = HomographyError.computeReprojectError(xyPts, uvPts, H);
            System.out.println("The linear reprojection error is: " + String.format("%.6f", linearError));  

            if (isRefine) {
                // Compute the nonlinear homography
                System.out.print("Compute the nonlinear homography ... ");

                NonLinearHomography nonLinHomog = new NonLinearHomography(xyPts, uvPts, H_norm);
                H = nonLinHomog.getHomography();
                H_norm = nonLinHomog.getNormalizedHomography();
                        
                System.out.println("Done");
                
                // Compute non-linear reprojection error
                double nonLinearError = HomographyError.computeReprojectError(xyPts, uvPts, H);
                System.out.println("The non-linear reprojection error is: " + String.format("%.6f", nonLinearError)); 
            }
            
            // Save the homography matrix
            String baseFilename = imagePointFilename.split(Pattern.quote("."))[0];
            String homgraphyFilename = baseFilename + "_Homography.txt";
            String homgraphyFullPath = Paths.get(homographySaveDir).resolve(homgraphyFilename).toString();
            TXT.saveMatrix(H_norm, Double.class, homgraphyFullPath, formatString);
        }
    }
}
