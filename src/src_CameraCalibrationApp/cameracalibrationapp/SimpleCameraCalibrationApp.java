package cameracalibrationapp;

import calibrationpattern.soliddiscs.ImageDiscs;
import calibrationpattern.soliddiscs.WorldDiscs;
import cameracalibration.ExtrinsicMatrix;
import cameracalibration.IntrinsicMatrix;
import cameracalibration.Projection;
import cameracalibration.ProjectionError;
import cameracalibration.RadialDistortion;
import cameracalibration.SymmetricMatrix;
import cameracalibration.nonlinear.NonLinearOptimization;
import core.ArrayUtils;
import core.Contours;
import core.Draw;
import core.ImageUtils;
import core.TXT;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import static core.Print.println;
import homography.LinearHomography;
import homography.Normalization;
import homography.nonlinear.NonLinearHomography;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;

public class SimpleCameraCalibrationApp {
    
    Map<String,Object> config;
    List<List<Double>> xyzPts;
    Map<String, List<List<Double>>> uvPtSets;
    List<List<Double>> intrinsicMatrix;
    Map<String,List<List<Double>>> RTMatrixSet;
    List<Double> distCoeffs;
    Map<String, BufferedImage> rgbImages;
    Map<String, BufferedImage> grayImages;
    Map<String, BufferedImage> bwImages;
    Map<String, BufferedImage> drawPointsImages;
    Map<String,Double> errorSet;
    
    
    public SimpleCameraCalibrationApp(String configPath) {
        // Load the XML configuration file
        XML xml = new XML(configPath);        
        
        // Prepend the base directory to all relative sub directories
        xml.prependBaseDir();
        
        // Get the configuration data as a map
        config = xml.map;   
    }
    
    public void findImagePoints() {
        
        // Find the Contours/Contour-Hierarchy        
        double contoursMinArea = (Double) config.get("contoursMinArea");
        println("Finding contours");
        Map<String, List<List<List<Integer>>>> contourSets = Contours.findContours_batch(bwImages, contoursMinArea);
        println("Finding contour areas");
        Map<String, List<Double>> contourAreaSets = Contours.computeArea_batch(contourSets);
        Map<String, List<Path2D>> contourPaths = Contours.contourToPath_batch(contourSets);
        println("Finding contour hierarchies");
        Map<String, Map<Integer, List<Integer>>> hierarchySets = Contours.findHierarchy_batch(contourSets, contourAreaSets, contourPaths);
        
        // Find Image Points
        println("Finding image points");
        int nRows = (Integer) config.get("nRows");
        int nCols = (Integer) config.get("nCols");
        int nPts = nRows*nCols;
        uvPtSets = ImageDiscs.computeCenters_batch(contourSets, hierarchySets, nPts);                

        // Sort the image points
        println("Sorting the image points");
        uvPtSets = ImageDiscs.sortCentersRowMajor_batch(uvPtSets, nRows, nCols);       
          
    }    
    
    public void computeWorldPoints() {
        int nRows = (Integer) config.get("nRows");
        int nCols = (Integer) config.get("nCols");
        double dx = (Double) config.get("dx");
        double dy = (Double) config.get("dy");
        xyzPts = WorldDiscs.computeCenters(nRows, nCols, dx, dy);
    }
        
    public void computeCalibration() {
                        
        // Convert world xyz to xy points (drop Z, since it is constant)
        List<List<Double>> xyPts = ArrayUtils.dropZ(xyzPts);
        
        // Merge all the image point sets
        List<List<Double>> uvPts_allViews = new ArrayList<>();
        List<String> uvPtSetsNames = new ArrayList<>(uvPtSets.keySet());
        Collections.sort(uvPtSetsNames);
        for (String name: uvPtSetsNames) {
            uvPts_allViews.addAll(uvPtSets.get(name));          
        }        
        
        // Create the normalization matrices
        Normalization norm = new Normalization(xyPts, uvPts_allViews);  
        
        // Normalize the world xy points
        List<List<Double>> xyPts_norm = norm.normalizePointsXY(xyPts); 
        
        // Normalize the image points
        Map<String,List<List<Double>>> uvPtSets_norm = norm.normalizePointsUV_batch(uvPtSets);
        
        // Compute the homography matrices
        println("Compute the homography matrices");        
        Map<String,List<List<Double>>> homographySets_norm = LinearHomography.computeHomography_batch(xyPts_norm, uvPtSets_norm);
        
        // Denormalize the homography matrices
        Map<String,List<List<Double>>> homographySets = norm.denormalizeHomographyMatrix_batch(homographySets_norm);
        
        // Compute the refined homography matrices
        println("Non-linear refinement of the homography matrices");        
        Map<String,List<List<Double>>> homographySets_norm_refined = NonLinearHomography.computeHomography_batch(xyPts_norm, uvPtSets_norm, homographySets_norm);
        
        // Denormalize the refined homography matrices
        Map<String,List<List<Double>>> homographySets_refined = norm.denormalizeHomographyMatrix_batch(homographySets_norm_refined);
                
        // Compute the symmetric matrix
        println("Computing the symmetric matrix");
        List<List<Double>> symmetricMatrix_norm = SymmetricMatrix.compute(homographySets_norm_refined);
        
        // Denormalize the symmetric matrix
        List<List<Double>> symmetricMatrix = norm.denormalizeSymmetricMatrix(symmetricMatrix_norm);        
        
        // Compute the intrinsic matrix
        println("Computing the intrinsic camera matrix");
        intrinsicMatrix = IntrinsicMatrix.compute(symmetricMatrix);
        
        // Compute the extrinsic matrices
        println("Computing the extrinsic camera matrix");
        RTMatrixSet = ExtrinsicMatrix.compute_batch(homographySets_refined, intrinsicMatrix);

        // Project the xyz world points to uv image plane
        Map<String,List<List<Double>>> uvPtsProjSet = Projection.projectPoints_batch(xyzPts, intrinsicMatrix, RTMatrixSet);
        
        // Merge all the projected uv image points
        List<List<Double>> uvPtsProj_allViews = new ArrayList<>();
        List<String> uvPtsProjSetNames = new ArrayList<>(uvPtsProjSet.keySet());
        Collections.sort(uvPtsProjSetNames);
        for (String name: uvPtsProjSetNames) {
            uvPtsProj_allViews.addAll(uvPtsProjSet.get(name));          
        }  
        
        // Compute the radial distortion coefficients
        println("Computing radial distortions");
        distCoeffs = RadialDistortion.compute(intrinsicMatrix, uvPts_allViews, uvPtsProj_allViews);
        
        // Merge all the RT matrices into a list
        List<List<List<Double>>> RT_allViews = new ArrayList<>();
        List<String> RTMatrixSetNames = new ArrayList<>(RTMatrixSet.keySet());
        Collections.sort(RTMatrixSetNames);
        for (String name: RTMatrixSetNames) {
            RT_allViews.add(RTMatrixSet.get(name));          
        }  
        
        // Compute the refined intrinsic/extrinsic/distortions
        println("Compute the refined intrinsic/extrinsic/distortions");
        NonLinearOptimization optimization = new NonLinearOptimization(xyzPts, uvPts_allViews, intrinsicMatrix, distCoeffs, RT_allViews);
        intrinsicMatrix = optimization.getK_refined();
        distCoeffs = optimization.getRadialCoeffs_refined();
        RT_allViews = optimization.getRT_allViews_refined();
        
        // Store the RT matrices into a map
        for(int i = 0; i < RT_allViews.size(); i++) {
            RTMatrixSet.put(RTMatrixSetNames.get(i), RT_allViews.get(i));
        }       
    }        
        
    public void loadRGBImages() {
        String imagesRGBDir = (String) config.get("imagesRGBDir");
        rgbImages = ImageUtils.load_batch(imagesRGBDir);
    }
    
    public void convertImages_rgbToGray() {
        grayImages = ImageUtils.color2Gray_batch(rgbImages);           
    }
    
    public void convertImages_grayToBW() {
        int adaptiveThresholdWindowSize = (Integer) config.get("adaptiveThresholdWindowSize");
        int adaptiveThresholdOffset = (Integer) config.get("adaptiveThresholdOffset");
        bwImages = ImageUtils.adaptiveThreshold_batch(grayImages, adaptiveThresholdWindowSize, adaptiveThresholdOffset);
    }
    
    public void computeErrors() {
        errorSet = ProjectionError.computeReprojectError_batch(xyzPts, intrinsicMatrix, distCoeffs, uvPtSets, RTMatrixSet);
    }
    
    public void displayErrors() {
        for (String name: errorSet.keySet()) {
            println("Error " + name + ": " + String.format("%.4f",errorSet.get(name)));
        }
    }
    
    public void drawImagePoints() {
        int drawCirclesRadius = (Integer) config.get("drawCirclesRadius");
        double drawCirclesLineWidth = (Double) config.get("drawCirclesLineWidth");
        Integer drawCirclesColor = (Integer) config.get("drawCirclesColor");
        boolean drawCirclesIsFill = (boolean) config.get("drawCirclesIsFill");
        drawPointsImages = Draw.drawCircles_batch(grayImages, uvPtSets, drawCirclesRadius, drawCirclesLineWidth, drawCirclesColor, drawCirclesIsFill);
    }
    
    public void saveGrayImages() {        
        String imagesGrayDir = (String) config.get("imagesGrayDir");
        ImageUtils.save_batch(grayImages, imagesGrayDir);        
    }
    
    public void saveBWImages() {
        String imagesBWDir = (String) config.get("imagesBWDir");
        ImageUtils.save_batch(bwImages, imagesBWDir);
    }
    
    public void saveImagePoints() {
        String imagePointsDir = (String) config.get("imagePointsDir");     
        String saveDataFormatString = (String) config.get("saveDataFormatString");
        String saveDataDelimiter = (String) config.get("saveDataDelimiter");
        String saveDataEndOfLine = (String) config.get("saveDataEndOfLine");
        boolean saveDataAppend = false;
        TXT.saveMatrix_batch(uvPtSets, Double.class, imagePointsDir, saveDataFormatString, saveDataDelimiter, saveDataEndOfLine, saveDataAppend);
    }
    
    public void savePointDrawings() {
        String pointDrawingsDir = (String) config.get("pointDrawingsDir");
        ImageUtils.save_batch(drawPointsImages, pointDrawingsDir);
    }
    
    public void saveIntrinsicMatrix() {
        // Save the intrinsic matrix
        String intrinsicMatrixDir = (String) config.get("intrinsicMatrixDir");
        String intrinsicMatrixFilename = (String) config.get("intrinsicMatrixFilename");        
        String saveDataFormatString = (String) config.get("saveDataFormatString");
        TXT.saveMatrix(intrinsicMatrix, Double.class, intrinsicMatrixDir, intrinsicMatrixFilename, saveDataFormatString);            
    }
    
    public void saveDistortionCoeffs() {
        // Save the distortions        
        String saveDataFormatString = (String) config.get("saveDataFormatString");
        String saveDataDelimiter = (String) config.get("saveDataDelimiter");
        boolean saveDataAppend = false;
        String distortionsDir = (String) config.get("distortionsDir");
        String distortionsFilename = (String) config.get("distortionsFilename");
        TXT.saveVector(distCoeffs, Double.class, distortionsDir, distortionsFilename, saveDataAppend, saveDataFormatString, saveDataDelimiter);
    }
    
    public void saveExtrinsicMatrices() {
        // Save the refined extrinsic matrices
        String extrinsicMatrixDir = (String) config.get("extrinsicMatrixDir");
        String saveDataFormatString = (String) config.get("saveDataFormatString");
        String saveDataDelimiter = (String) config.get("saveDataDelimiter");
        String saveDataEndOfLine = (String) config.get("saveDataEndOfLine");
        boolean saveDataAppend = false;
        TXT.saveMatrix_batch(RTMatrixSet, Double.class, extrinsicMatrixDir, saveDataFormatString, saveDataDelimiter, saveDataEndOfLine, saveDataAppend);
    }
    
    public void saveWorldPoints() {
        String worldPointsDir = (String) config.get("worldPointsDir");
        String worldPointsFilename = (String) config.get("worldPointsFilename");
        String saveDataFormatString = (String) config.get("saveDataFormatString");
        TXT.saveMatrix(xyzPts, Double.class, worldPointsDir, worldPointsFilename, saveDataFormatString);
    }

    
    public static void main(String[] args) {
               
        println("Running the Camera Calibration App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];
        
        SimpleCameraCalibrationApp app = new SimpleCameraCalibrationApp(configAbsPath);
        
        // Load RGB images
        println("Loading RGB Images");
        app.loadRGBImages();
        
        // Convert RGB to Gray images
        println("Converting RGB to Gray");
        app.convertImages_rgbToGray();       
        
        // Save the Gray images
        boolean isSaveImageMode = (Boolean) app.config.get("isSaveImageMode");        
        if (isSaveImageMode) {            
            println("Saving the Gray Images");
            app.saveGrayImages();
        }
        
        // Convert Gray to Black/White images
        println("Converting gray to bw");
        app.convertImages_grayToBW();
        
        // Save the Black/White images
        if (isSaveImageMode) {            
            app.saveBWImages();
        }
        
        // Find Image Points
        app.findImagePoints();
        
        // Save the Image Points
        boolean isSaveDataMode = (Boolean) app.config.get("isSaveDataMode");
        if (isSaveDataMode) {
            println("Saving the Images Centers");
            app.saveImagePoints();
        }
        
        // Draw and Save the Image Points
        if (isSaveImageMode) {
            
            println("Drawing the image points");
            app.drawImagePoints();
        
            println("Saving the drawing of the image points");
            app.savePointDrawings();
        }
        
        // Compute the World Points
        println("Computing the world points");
        app.computeWorldPoints();
        
        // Save the World Points
        if (isSaveDataMode) { 
            println("Saving the world points");
            app.saveWorldPoints();
        }
        
        // Compute Intrinsic/Extrinsic/Distortions data
        app.computeCalibration();
        
        // Save Intrinsic/Extrinsic/Distortions data
        if (isSaveDataMode) {            
            println("Saving the intrinsicMatrix");
            app.saveIntrinsicMatrix();
            
            println("Saving the distortion coefficients");
            app.saveDistortionCoeffs();
            
            println("Saving the extrinsic matrices");
            app.saveExtrinsicMatrices();
        }        
        
        app.computeErrors();
        app.displayErrors();

    }
    
    
}