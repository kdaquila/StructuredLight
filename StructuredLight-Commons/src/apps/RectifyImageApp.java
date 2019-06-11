package apps;

import cameracalibration.Projection;
import core.ImageUtils;
import core.TXT;
import core.XML;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import rectify.RectifyImage;

public class RectifyImageApp {

    public static void main(String[] args) {
        System.out.println("Running the RectifyImageApp:");

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
        String rectifiedImagesDir = conf.getString("/config/rectifiedImagesDir");
        String originalImagesDir = conf.getString("/config/originalImagesDir");
        
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
            
            // Get the view's base name
            String baseFilename = imagePointFilename.split(Pattern.quote("."))[0];
            
            System.out.println("Now processing image: " + baseFilename);
            
            // Load the observed image points
            String imagePointsFullPath = Paths.get(imagePointsDir).resolve(imagePointFilename).toString();
            List<List<Double>> uvPts_observed = TXT.loadMatrix(imagePointsFullPath, Double.class);
            
            // Append to master list of observed image points
            uvPts_observed_allViews.addAll(uvPts_observed);
            
            // Load the extrinsic matrix
            String extrinsicMatrixFilename = baseFilename + ".txt";
            String extrinsicMatrixFullPath = Paths.get(extrinsicMatrixDir).resolve(extrinsicMatrixFilename).toString();
            List<List<Double>> RT = TXT.loadMatrix(extrinsicMatrixFullPath, Double.class);
            RT_allViews.add(RT);  
            
            // Load the original image
            String originalImageFilename = baseFilename + ".png";
            String originalImageFullPath = Paths.get(originalImagesDir).resolve(originalImageFilename).toString();
            BufferedImage originalImage = ImageUtils.load(originalImageFullPath);
            
            // Rectify the image
            RectifyImage rectifier = new RectifyImage(K, radialCoeffs);
            BufferedImage rectifiedImage = rectifier.rectify(originalImage, RT);

            // Save the image
            String rectifiedImageFullPath = Paths.get(rectifiedImagesDir).resolve(originalImageFilename).toString();
            ImageUtils.save(rectifiedImage, rectifiedImageFullPath);
            
            
        }
        
        
    }
}
