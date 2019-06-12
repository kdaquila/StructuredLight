package apps;

import core.XML;

public class CameraCalibrationApp {
    
    public static void Main(String[] args) {
        
        System.out.println("Running the Camera Calibration App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];

        System.out.print("Loading the configuration ... ");
        
        // Load the configuration variables        
        XML conf = new XML(configAbsPath);                  
        String baseDir = conf.getString("/config/baseDir"); 
        String intrinsicMatrixDir = conf.getString("/config/intrinsicMatrixDir");
        String intrinsicMatrixRefinedDir = conf.getString("/config/intrinsicMatrixRefinedDir");
        String intrinsicMatrixFilename = conf.getString("/config/intrinsicMatrixFilename");          
        String worldPointsDir = conf.getString("/config/worldPointsDir");     
        String worldPointsFilename = conf.getString("/config/worldPointsFilename");
        String distortionsDir = conf.getString("/config/distortionsDir");
        String distortionsRefinedDir = conf.getString("/config/distortionsRefinedDir");
        String distortionsFilename = conf.getString("/config/distortionsFilename");
        String extrinsicMatrixDir = conf.getString("/config/extrinsicMatrixDir");
        String extrinsicMatrixRefinedDir = conf.getString("/config/extrinsicMatrixRefinedDir");
        String imagesRGBDir = conf.getString("/config/imagesRGBDir");
        String imagesGrayDir = conf.getString("/config/imagesGrayDir");
        String imagesBWDir = conf.getString("/config/imagesBWDir");
        String imagesLaplacianDir = conf.getString("/config/imagesLaplacianDir");
        String imagesGrayFrontoParallelDir = conf.getString("/config/imagesGrayFrontoParallelDir");
        String imagesContoursAllDir = conf.getString("/config/imagesContoursAllDir");
        String imagesContoursSelectDir = conf.getString("/config/imagesContoursSelectDir");
        String imagePointsDir = conf.getString("/config/imagePointsDir");
        String imagePointsRefinedDir = conf.getString("/config/imagePointsRefinedDir");
        String contourPointsDir = conf.getString("/config/contourPointsDir");
        String contourHierarchyDir = conf.getString("/config/contourHierarchyDir");
        
        System.out.println("Done");
        
    }
    

}
