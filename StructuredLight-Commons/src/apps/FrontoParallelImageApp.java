package apps;

import core.ArrayUtils;
import core.ImageUtils;
import core.TXT;
import core.XML;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import frontoparallel.FrontoParallelImage;

public class FrontoParallelImageApp {

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
        String intrinsicMatrixPath = conf.getString("/config/intrinsicMatrixPath");
        String extrinsicMatrixDir = conf.getString("/config/extrinsicMatrixDir");          
        String imagePointsDir = conf.getString("/config/imagePointsDir");     
        String radialDistPath = conf.getString("/config/radialDistPath");
        String rectifiedImagesDir = conf.getString("/config/rectifiedImagesDir");
        String originalImagesDir = conf.getString("/config/originalImagesDir");              
        int nRows = conf.getInt("/config/nRows");
        int nCols = conf.getInt("/config/nCols");
        double dx = conf.getDouble("/config/dx_mm");
        double dy = conf.getDouble("/config/dy_mm");
        double incr = conf.getDouble("/config/rectifyImg_mmPerPixel");
        
        System.out.println("Done");
        
        System.out.print("Loading the input data ... ");
        
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
        
        System.out.println("Done");
        
        // Load the image points and RT matrices
        for (String imagePointFilename: imagePointFilenames) {
            
            // Get the view's base name
            String baseFilename = imagePointFilename.split(Pattern.quote("."))[0];
            
            System.out.println("Now processing image: " + baseFilename);           
            
            // Load the extrinsic matrix
            String extrinsicMatrixFilename = baseFilename + ".txt";
            String extrinsicMatrixFullPath = Paths.get(extrinsicMatrixDir).resolve(extrinsicMatrixFilename).toString();
            List<List<Double>> RT = TXT.loadMatrix(extrinsicMatrixFullPath, Double.class);
            
            // Load the original image
            String originalImageFilename = baseFilename + ".png";
            String originalImageFullPath = Paths.get(originalImagesDir).resolve(originalImageFilename).toString();
            BufferedImage originalImage = ImageUtils.load(originalImageFullPath);
            
            // Rectify the image
            FrontoParallelImage rectifier = new FrontoParallelImage(K, radialCoeffs);
            double xMin = -dx;
            double xMax = nCols*dx;
            double yMin = -dy;
            double yMax = nRows*dy;            
            BufferedImage rectifiedImage = rectifier.rectify(originalImage, RT, xMin, xMax, yMin, yMax, incr);
            
            // Normalize the image
            List<List<Double>> rectifiedImageData = ImageUtils.GrayImageToList(rectifiedImage);
            BufferedImage rectifiedImage_norm = ImageUtils.ListToGrayImage_Double(ArrayUtils.normalizeDoubleList(rectifiedImageData));

            // Save the image
            String rectifiedImageFullPath = Paths.get(rectifiedImagesDir).resolve(originalImageFilename).toString();
            ImageUtils.save(rectifiedImage_norm, rectifiedImageFullPath);
            
            
        }
        
        
    }
}
