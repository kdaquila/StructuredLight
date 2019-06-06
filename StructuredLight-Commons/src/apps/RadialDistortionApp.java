package apps;

import cameracalibration.Projection;
import cameracalibration.ProjectionError;
import cameracalibration.RadialDistortion;
import core.ArrayUtils;
import core.TXT;
import core.XML;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RadialDistortionApp {

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
        String formatStr = conf.getString("/config/formatStr"); 
        String intrinsicMatrixPath = conf.getString("/config/intrinsicMatrixPath");
        String worldPointsPath = conf.getString("/config/worldPointsPath");
        String extrinsicMatrixDir = conf.getString("/config/extrinsicMatrixDir");          
        String imagePointsDir = conf.getString("/config/imagePointsDir");     
        String radialDistPath = conf.getString("/config/radialDistPath");
        System.out.println("Done");
        
        System.out.print("Loading the input data ... ");
        
        // Load the world xyz points
        List<List<Double>> xyzPts = TXT.loadMatrix(worldPointsPath, Double.class); 
        
        // Convert world xyz to xy points (drop Z, since it is constant)
        List<List<Double>> xyPts = ArrayUtils.dropZ(xyzPts);
        
        // Load the intrinsic matrix
        List<List<Double>> K = TXT.loadMatrix(intrinsicMatrixPath, Double.class);
        
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
        
        // Build the observed and projected image point sets
        List<List<Double>> uvPts_observed_allViews = new ArrayList<>();
        List<List<Double>> uvPts_projected_allViews = new ArrayList<>();
        for (String imagePointFilename: imagePointFilenames) {
            
            // Load the observed image points
            String imagePointsFullPath = Paths.get(imagePointsDir).resolve(imagePointFilename).toString();
            List<List<Double>> uvPts_observed = TXT.loadMatrix(imagePointsFullPath, Double.class);
            
            // Append to master list of observed image points
            for (List<Double> pt: uvPts_observed) {
                uvPts_observed_allViews.add(pt);
            }

            // Get the view's base name
            String baseFilename = imagePointFilename.split(Pattern.quote("."))[0]; 
            
            // Load the extrinsic matrix
            String extrinsicMatrixFilename = baseFilename + ".txt";
            String extrinsicMatrixFullPath = Paths.get(extrinsicMatrixDir).resolve(extrinsicMatrixFilename).toString();
            List<List<Double>> RT = TXT.loadMatrix(extrinsicMatrixFullPath, Double.class);
            
            // Project the world points into this view
            List<List<Double>> uvPts_projected = Projection.projectPoints(xyzPts, K, RT);
            
            // Append to master list of projected image points
            for (List<Double> pt: uvPts_projected) {
                uvPts_projected_allViews.add(pt);
            }            
        }
        
        System.out.println("Done");
        
        // Compute the radial distortions
        System.out.print("Computing the radial distortion coefficients ... ");
        List<Double> distCoeffs = RadialDistortion.compute(K, uvPts_observed_allViews, uvPts_projected_allViews);
        System.out.println("Done");
        
        // Save the radial distortions
        TXT.saveVector(distCoeffs, Double.class, radialDistPath, false, formatStr, ",");
        
        // Compute the reprojection error for each view
        for (String imagePointFilename: imagePointFilenames) {
            
            // Load the observed image points
            String imagePointsFullPath = Paths.get(imagePointsDir).resolve(imagePointFilename).toString();
            List<List<Double>> uvPts_observed = TXT.loadMatrix(imagePointsFullPath, Double.class);
            
            // Get the view's base name
            String baseFilename = imagePointFilename.split(Pattern.quote("."))[0]; 
            
            // Load the extrinsic matrix
            String extrinsicMatrixFilename = baseFilename + ".txt";
            String extrinsicMatrixFullPath = Paths.get(extrinsicMatrixDir).resolve(extrinsicMatrixFilename).toString();
            List<List<Double>> RT = TXT.loadMatrix(extrinsicMatrixFullPath, Double.class);
            
            double error_noDistCorr = ProjectionError.computeReprojectError(xyzPts, uvPts_observed, K, RT);
            System.out.println("Image " + baseFilename + ": Error without radialDist correction: " + error_noDistCorr);
            
            double error_withDistCorr = ProjectionError.computeReprojectError(xyzPts, uvPts_observed, K, RT, distCoeffs);
            System.out.println("Image " + baseFilename + ": Error without radialDist correction: " + error_withDistCorr);
        }
        
    
    }
}
