package apps;

import cameracalibration.Projection;
import cameracalibration.ProjectionError;
import cameracalibration.nonlinear.NonLinearOptimization;
import core.TXT;
import core.XML;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class OptimizeProjectionApp {
    public static void main(String[] args) {
        System.out.println("Running the OptimizeProjectionApp:");

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
        String kSavePath = conf.getString("/config/kSavePath");
        String radialDistSavePath = conf.getString("/config/radialDistSavePath");
        String extrinsicMatrixSaveDir = conf.getString("/config/extrinsicMatrixSaveDir");
        
        System.out.println("Done");
        
        System.out.print("Loading the input data ... ");
        
        // Load the world xyz points
        List<List<Double>> xyzPts = TXT.loadMatrix(worldPointsPath, Double.class); 
        
        // Load the intrinsic matrix
        List<List<Double>> K = TXT.loadMatrix(intrinsicMatrixPath, Double.class);
        
        // Load the radial distortion coefficients
        List<Double> radialCoeffs = TXT.loadVector(radialDistPath, Double.class, ",", "\n");
        
        // Find the image point paths
        String[] imagePointFilenames = (new File(imagePointsDir)).list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                String lowerName = name.toLowerCase();
                boolean isValid = lowerName.endsWith(".txt"); 
                return isValid;
            }
        });
        
        // Validate file names
        if (imagePointFilenames == null || imagePointFilenames.length == 0) {
            throw new RuntimeException("No suitables files found in the input directory.");
        }
        
        // Load the image points and RT matrices
        List<List<Double>> uvPts_observed_allViews = new ArrayList<>();
        List<List<List<Double>>> RT_allViews = new ArrayList<>();
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
            RT_allViews.add(RT);
            
            // Project the world points into this view
            List<List<Double>> uvPts_projected = Projection.projectPoints(xyzPts, K, RT);  
        }
        
        System.out.println("Done");
        
        // Run the optimiation
        System.out.print("Running the optimization ... ");
        NonLinearOptimization optimization = new NonLinearOptimization(xyzPts, uvPts_observed_allViews, K, radialCoeffs, RT_allViews);
        System.out.println("Done");
        
        // Get the results
        List<List<Double>> K_refined = optimization.getK_refined();
        List<List<List<Double>>> RT_refined_allViews = optimization.getRT_allViews_refined();
        List<Double> radialCoeffs_refined = optimization.getRadialCoeffs_refined();
        
        // Save the results
        System.out.print("Saving the results ... ");
        TXT.saveMatrix(K_refined, Double.class, kSavePath, formatStr);
        TXT.saveVector(radialCoeffs_refined, Double.class, radialDistSavePath, false, formatStr, ",");
        for (int view_num = 0; view_num < RT_refined_allViews.size(); view_num++) {

            // Get the view's base name
            String imagePointFilename = imagePointFilenames[view_num];
            String baseFilename = imagePointFilename.split(Pattern.quote("."))[0]; 
            
            // Save the extrinsic matrix
            String extrinsicMatrixFilename = baseFilename + ".txt";
            String extrinsicMatrixFullSavePath = Paths.get(extrinsicMatrixSaveDir).resolve(extrinsicMatrixFilename).toString();
            List<List<Double>> RT_refined = RT_refined_allViews.get(view_num);
            TXT.saveMatrix(RT_refined, Double.class, extrinsicMatrixFullSavePath, formatStr);
        }
        
        System.out.println("Done");
        
        // Compute the reprojection error for each view
        for (int view_num = 0; view_num < RT_refined_allViews.size(); view_num++) {
            
            // Get the view's image points name
            String imagePointFilename = imagePointFilenames[view_num];
            
            // Load the observed image points
            String imagePointsFullPath = Paths.get(imagePointsDir).resolve(imagePointFilename).toString();
            List<List<Double>> uvPts_observed = TXT.loadMatrix(imagePointsFullPath, Double.class);
            
            List<List<Double>> RT = RT_allViews.get(view_num);
            List<List<Double>> RT_refined = RT_refined_allViews.get(view_num);
            
            String baseFilename = imagePointFilename.split(Pattern.quote("."))[0];
            
            double error_beforeOptim = ProjectionError.computeReprojectError(xyzPts, uvPts_observed, K, RT, radialCoeffs);
            System.out.println("Image " + baseFilename + ": Error before optimization: " + String.format("%5.3f", error_beforeOptim));
            
            double error_afterOptim = ProjectionError.computeReprojectError(xyzPts, uvPts_observed, K_refined, RT_refined, radialCoeffs_refined);
            System.out.println("Image " + baseFilename + ": Error after optimization:  " + String.format("%5.3f", error_afterOptim));
            
            System.out.println("");
        }
        
        
    }
}
