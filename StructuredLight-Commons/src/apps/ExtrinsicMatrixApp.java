package apps;

import cameracalibration.ExtrinsicMatrix;
import cameracalibration.Projection;
import cameracalibration.ProjectionError;
import core.ArrayUtils;
import core.TXT;
import core.XML;
import homography.Normalization;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ExtrinsicMatrixApp {

    public static void main(String[] args) {
        System.out.println("Running the ExtrinsicMatrixApp:");

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
        String homographyDir = conf.getString("/config/homographyDir"); 
        String intrinsicMatrixPath = conf.getString("/config/intrinsicMatrixPath");
        String extrinsicMatrixDir = conf.getString("/config/extrinsicMatrixDir");
        String formatStr = conf.getString("/config/formatStr");           
        String imagePointsDir = conf.getString("/config/imagePointsDir");         
        String worldPointsPath = conf.getString("/config/worldPointsPath");
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
        
        // Load the intrinsic matrix
        List<List<Double>> K = TXT.loadMatrix(intrinsicMatrixPath, Double.class);
        
        // Find the homography paths
        File homographyDir_file = new File(homographyDir);
        String[] homographyFilenames = homographyDir_file.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                String lowerName = name.toLowerCase();
                boolean isValid = lowerName.endsWith(".txt") ||
                                  lowerName.endsWith(".csv"); 
                return isValid;
            }
        });
        
        // Validate homography filenames
        if (homographyFilenames == null || homographyFilenames.length == 0) {
            throw new RuntimeException("No suitables files found in the input directory.");
        }        
        
        // Create the normalization matrices
        Normalization norm = new Normalization(xyPts, uvPts_allViews);
        
        // Load each homography and compute extrinsic matrix
        System.out.print("Computing the extrinsic matrices ... \n");
        for (String homographyFilename: homographyFilenames) {
            
            // Get the view's base name
            String baseFilename = homographyFilename.split(Pattern.quote("."))[0];
            
            System.out.println("Now processing: " + baseFilename);
                                    
            // Load the homography
            String homographyFullPath = Paths.get(homographyDir).resolve(homographyFilename).toString();
            List<List<Double>> H_norm = TXT.loadMatrix(homographyFullPath, Double.class);
            
            // Denormalize the homography
            List<List<Double>> H = norm.denormalizeHomographyMatrix(H_norm);
            
            // Compute the extrinsic matrix
            List<List<Double>> RT = ExtrinsicMatrix.compute(H, K);
            
            // Save the extrinsic matrix            
            String extrinsicMatrixFilename = baseFilename + ".txt";
            String extrinsicMatrixFullPath = Paths.get(extrinsicMatrixDir).resolve(extrinsicMatrixFilename).toString();
            TXT.saveMatrix(RT, Double.class, extrinsicMatrixFullPath, formatStr);      
            
            // Load the observed image points
            String imagePointsFullPath = Paths.get(imagePointsDir).resolve(baseFilename + ".txt").toString();
            List<List<Double>> uvPts = TXT.loadMatrix(imagePointsFullPath, Double.class);
                        
            // Compute the reprojection error
            double error = ProjectionError.computeReprojectError(xyzPts, uvPts, K, RT);
            System.out.println("Error: " + error);
            
        }
        System.out.println("Done");
    }
}
