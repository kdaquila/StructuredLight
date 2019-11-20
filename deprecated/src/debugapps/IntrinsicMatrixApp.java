package apps;

import cameracalibration.IntrinsicMatrix;
import cameracalibration.SymmetricMatrix;
import core.ArrayUtils;
import core.TXT;
import core.XML;
import homography.Normalization;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IntrinsicMatrixApp {

    public static void main(String[] args) {
        System.out.println("Running the IntrinsicMatrixApp:");

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
        String homographyDir = conf.getString("/config/homographyDir"); 
        String intrinsicMatrixPath = conf.getString("/config/intrinsicMatrixPath");
        String formatStr = conf.getString("/config/formatStr");        
        System.out.println("Done");
        
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
        
        // Load the homographies
        List<List<List<Double>>> homographies = new ArrayList<>(homographyFilenames.length);
        for (String homographyFilename: homographyFilenames) {
            String homographyFullPath = Paths.get(homographyDir).resolve(homographyFilename).toString();
            homographies.add(TXT.loadMatrix(homographyFullPath, Double.class));            
        }
        
        // Compute the symmetric matrix
        List<List<Double>> B_norm = SymmetricMatrix.compute(homographies);
        
        // Load the world xyz points
        List<List<Double>> xyzPts = TXT.loadMatrix(worldPointsPath, Double.class);   

        // Convert world xyz to xy points (drop Z, since it is constant)
        List<List<Double>> xyPts = ArrayUtils.dropZ(xyzPts);
        
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
        
        // Validate image point file names
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
        
        // Denormalize the symmetric matrix
        List<List<Double>> B = norm.denormalizeSymmetricMatrix(B_norm);
        
        // Compute the intrinsic matrix
        System.out.print("Computing the intrinsic camera matrix ... ");
        List<List<Double>> K = IntrinsicMatrix.compute(B);
        System.out.println("Done"); 
        
        // Save the intrinsic matrix
        TXT.saveMatrix(K, Double.class, intrinsicMatrixPath, formatStr);
        
    }
}
