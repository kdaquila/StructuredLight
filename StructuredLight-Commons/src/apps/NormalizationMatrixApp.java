package apps;

import core.ArrayUtils;
import core.TXT;
import core.XML;
import homography.NormalizationMatrix;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class NormalizationMatrixApp {
    
    public static void main(String[] args) {
    
        System.out.println("Running the NormalizationMatrixApp:");

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
        String N_Dir = conf.getString("/config/N_Dir");
        String N_xy_Filename = conf.getString("/config/N_xy_Filename");
        String N_xy_inv_Filename = conf.getString("/config/N_xy_inv_Filename");
        String N_uv_Filename = conf.getString("/config/N_uv_Filename");
        String N_uv_inv_Filename = conf.getString("/config/N_uv_inv_Filename");
        double factor = conf.getDouble("/config/factor");
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

        // Validate filenames
        if (imagePointFilenames == null || imagePointFilenames.length == 0) {
            throw new RuntimeException("No suitables files found in the input directory.");
        }
        
        // Load the world points
        System.out.print("Loading the world points ...");
        List<List<Double>> xyPts = TXT.loadMatrix(worldPointsPath, Double.class);
        System.out.println("Done");
        
        // Build the world points normalization matrix
        System.out.print("Computing the world points normalization ... ");
        List<Double> xPts = new ArrayList<>();
        List<Double> yPts = new ArrayList<>();
        ArrayUtils.unzipList(xyPts, xPts, yPts);
        NormalizationMatrix N_xy = new NormalizationMatrix(xPts, yPts, factor);
        System.out.println("Done");
        
        // Save the world points normalization matrix
        System.out.print("Saving the world points normalization matrices... ");
        TXT.saveMatrix(N_xy.getMatrix(), Double.class, N_Dir, N_xy_Filename, formatString);
        TXT.saveMatrix(N_xy.getInvMatrix(), Double.class, N_Dir, N_xy_inv_Filename, formatString);
        System.out.println("Done");
        
        // Load the image points
        List<List<Double>> uvPts = new ArrayList<>();
        for (String imagePointFilename: imagePointFilenames) {

            System.out.println("Loading image points from: " + imagePointFilename);

            // Load the image points for this view
            String imagePointsFullPath = Paths.get(imagePointsDir).resolve(imagePointFilename).toString();
            List<List<Double>> uvPts_view = TXT.loadMatrix(imagePointsFullPath, Double.class);
        
            // Append to master list
            for (List<Double> pt: uvPts_view) {
                uvPts.add(pt);
            }            
        }
        
        // Build the image points normalization matrix
        System.out.print("Computing the image points normalization ... ");
        List<Double> uPts = new ArrayList<>();
        List<Double> vPts = new ArrayList<>();
        ArrayUtils.unzipList(uvPts, uPts, vPts);
        NormalizationMatrix N_uv = new NormalizationMatrix(uPts, vPts, factor);
        System.out.println("Done"); 
        
        // Save the image points normalization matrix
        System.out.print("Saving the image points normalization matrices... ");
        TXT.saveMatrix(N_uv.getMatrix(), Double.class, N_Dir, N_uv_Filename, formatString);
        TXT.saveMatrix(N_uv.getInvMatrix(), Double.class, N_Dir, N_uv_inv_Filename, formatString);
        System.out.println("Done");    
    }
}
