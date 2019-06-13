package apps;

import calibrationpattern.rings.ImageRings;
import calibrationpattern.rings.WorldRings;
import cameracalibration.ExtrinsicMatrix;
import cameracalibration.IntrinsicMatrix;
import cameracalibration.Projection;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class CameraCalibrationApp {
    
    public static void main(String[] args) {
               
        println("Running the Camera Calibration App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];

        // Load the configuration variables      
        Map<String,Object> config = XML.loadMap(configAbsPath, "config");
        
        boolean isSaveDataMode = (boolean)config.get("isSaveDataMode");
        boolean isSaveImageMode = (boolean)config.get("isSaveImageMode");
        String saveDataFormatString = (String) config.get("saveDataFormatString");
        String saveDataDelimiter = (String) config.get("saveDataDelimiter");
        String saveDataEndOfLine = "\n";
        Boolean saveDataAppend = false;
        
        // Load RGB images
        println("Loading rgb images");
        String imagesRGBDir = (String) config.get("imagesRGBDir");
        Map<String, BufferedImage> rgbImages = ImageUtils.load_batch(imagesRGBDir);
        
        // Convert RGB to Gray images
        println("Converting rgb to gray");
        Map<String, BufferedImage> grayImages = ImageUtils.color2Gray_batch(rgbImages);        
        
        if (isSaveImageMode) {
            // Save the gray images
            String imagesGrayDir = (String) config.get("imagesGrayDir");
            ImageUtils.save_batch(grayImages, imagesGrayDir);
        }
        
        // Convert Gray to Black/White images
        println("Converting gray to bw");
        int adaptiveThresholdWindowSize = (Integer) config.get("adaptiveThresholdWindowSize");
        int adaptiveThresholdOffset = (Integer) config.get("adaptiveThresholdOffset");
        Map<String, BufferedImage> bwImages = ImageUtils.adaptiveThreshold_batch(grayImages, adaptiveThresholdWindowSize, adaptiveThresholdOffset);        
        
        if (isSaveImageMode) {
            // Save the Black/White images
            String imagesBWDir = (String) config.get("imagesBWDir");
            ImageUtils.save_batch(bwImages, imagesBWDir);
        }
        
        // Find the Contours/Contour-Hierarchy        
        double contoursMinArea = (Double) config.get("contoursMinArea");
        println("Finding contours");
        Map<String, List<List<List<Integer>>>> contourSets = Contours.findContours_batch(bwImages, contoursMinArea);
        println("Finding contour hierarchies");
        Map<String, Map<Integer, List<Integer>>> hierarchySets = Contours.findHierarchy_batch(contourSets);
        
        // Find Image Points
        println("Finding image points");
        int nRows = (Integer) config.get("nRows");
        int nCols = (Integer) config.get("nCols");
        int nPts = nRows*nCols;
        Map<String, List<List<Double>>> uvPtSets = ImageRings.computeCenters_batch(contourSets, hierarchySets, nPts);

        // Sort the image points
        println("Sorting the image points");
        uvPtSets = ImageRings.sortCentersRowMajor_batch(uvPtSets, nRows, nCols);
        
        if (isSaveDataMode) {
            // Save the Image Points
            String imagePointsDir = (String) config.get("imagePointsDir");            
            TXT.saveMatrix_batch(uvPtSets, Double.class, imagePointsDir, saveDataFormatString, saveDataDelimiter, saveDataEndOfLine, saveDataAppend);
        }
                
        if (isSaveImageMode) {
            // Draw the Image Points
            println("Drawing the image points");
            int drawCirclesRadius = (Integer) config.get("drawCirclesRadius");
            double drawCirclesLineWidth = (Double) config.get("drawCirclesLineWidth");
            Integer drawCirclesColor = (Integer) config.get("drawCirclesColor");
            boolean drawCirclesIsFill = (boolean) config.get("drawCirclesIsFill");
            Map<String, BufferedImage> drawPointsImages = Draw.drawCircles_batch(grayImages, uvPtSets, drawCirclesRadius, drawCirclesLineWidth, drawCirclesColor, drawCirclesIsFill);
        
            // Save the Image Point Images
            String imagesDrawPointsDir = (String) config.get("imagesDrawPointsDir");
            ImageUtils.save_batch(drawPointsImages, imagesDrawPointsDir);
        }
        
        // Compute the World Points
        println("Computing the world points");
        double dx = (Double) config.get("dx");
        double dy = (Double) config.get("dy");
        List<List<Double>> xyzPts = WorldRings.computeCenters(nRows, nCols, dx, dy);
        
        if (isSaveDataMode) {
            // Save the World Points
            String worldPointsDir = (String) config.get("worldPointsDir");
            String worldPointsFilename = (String) config.get("worldPointsFilename");
            TXT.saveMatrix(xyzPts, Double.class, worldPointsDir, worldPointsFilename, saveDataFormatString);
        }
        
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
        
        if (isSaveDataMode) {
            // Save the homography matrices
            String homographyMatrixDir = (String) config.get("homographyMatrixDir");
            TXT.saveMatrix_batch(homographySets, Double.class, homographyMatrixDir, saveDataFormatString, saveDataDelimiter, saveDataEndOfLine, saveDataAppend);
        }
        
        // Compute the refined homography matrices
        println("Refining the homography matrices");        
        Map<String,List<List<Double>>> homographySets_norm_refined = NonLinearHomography.computeHomography_batch(xyPts_norm, uvPtSets_norm, homographySets_norm);
        
        // Denormalize the refined homography matrices
        Map<String,List<List<Double>>> homographySets_refined = norm.denormalizeHomographyMatrix_batch(homographySets_norm_refined);
        
        if (isSaveDataMode) {
            // Save the homography matrices
            String homographyMatrixRefinedDir = (String) config.get("homographyMatrixRefinedDir");
            TXT.saveMatrix_batch(homographySets_refined, Double.class, homographyMatrixRefinedDir, saveDataFormatString, saveDataDelimiter, saveDataEndOfLine, saveDataAppend);
        }
        
        // Compute the symmetric matrix
        println("Computing the symmetric matrix");
        List<List<Double>> symmetricMatrix_norm = SymmetricMatrix.compute(homographySets_norm_refined);
        
        // Denormalize the symmetric matrix
        List<List<Double>> symmetricMatrix = norm.denormalizeSymmetricMatrix(symmetricMatrix_norm);
        
        if (isSaveDataMode) {
            // Save the symmetric matrix
            String symmetricMatrixDir = (String) config.get("symmetricMatrixDir");
            String symmetricMatrixFilename = (String) config.get("symmetricMatrixFilename");
            TXT.saveMatrix(symmetricMatrix, Double.class, symmetricMatrixDir, symmetricMatrixFilename, saveDataFormatString, saveDataDelimiter, saveDataEndOfLine, saveDataAppend);
        }
        
        // Compute the intrinsic matrix
        println("Computing the intrinsic camera matrix");
        List<List<Double>> intrinsicMatrix = IntrinsicMatrix.compute(symmetricMatrix);
        
        if (isSaveDataMode) {
            // Save the intrinsic matrix
            String intrinsicMatrixDir = (String) config.get("intrinsicMatrixDir");
            String intrinsicMatrixFilename = (String) config.get("intrinsicMatrixFilename");
            TXT.saveMatrix(intrinsicMatrix, Double.class, intrinsicMatrixDir, intrinsicMatrixFilename, saveDataFormatString);
        }
        
        // Compute the extrinsic matrices
        println("Computing the extrinsic camera matrix");
        Map<String,List<List<Double>>> RTMatrixSet = ExtrinsicMatrix.compute_batch(homographySets, intrinsicMatrix);
        
        if (isSaveDataMode) {
            // Save the extrinsic matrices
            String extrinsicMatrixDir = (String) config.get("extrinsicMatrixDir");
            TXT.saveMatrix_batch(RTMatrixSet, Double.class, extrinsicMatrixDir, saveDataFormatString, saveDataDelimiter, saveDataEndOfLine, saveDataAppend);
        }
        
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
        List<Double> distCoeffs = RadialDistortion.compute(intrinsicMatrix, uvPts_allViews, uvPtsProj_allViews);
        
        // Save the radial distortion coefficients
        if (isSaveDataMode) {
            String distortionsDir = (String) config.get("distortionsDir");
            String distortionsFilename = (String) config.get("distortionsFilename");
            TXT.saveVector(distCoeffs, Double.class, distortionsDir, distortionsFilename, saveDataAppend, saveDataFormatString, saveDataDelimiter);
        }
        
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
        List<List<Double>> intrinsicMatrix_refined = optimization.getK_refined();
        List<Double> distCoeffs_refined = optimization.getRadialCoeffs_refined();
        List<List<List<Double>>> RT_allViews_refined = optimization.getRT_allViews_refined();
        
        // Store the RT matrices into a map
        Map<String,List<List<Double>>> RTMatrixSet_refined = new HashMap<>();
        for(int i = 0; i < RT_allViews_refined.size(); i++) {
            RTMatrixSet_refined.put(RTMatrixSetNames.get(i), RT_allViews_refined.get(i));
        }
        
        if (isSaveDataMode) {            
            // Save the refined intrinsic matrix
            String intrinsicMatrixRefinedDir = (String) config.get("intrinsicMatrixRefinedDir");
            String intrinsicMatrixFilename = (String) config.get("intrinsicMatrixFilename");
            TXT.saveMatrix(intrinsicMatrix_refined, Double.class, intrinsicMatrixRefinedDir, intrinsicMatrixFilename, saveDataFormatString);            
        }        
        
        if (isSaveDataMode) {
            // Save the refined distortions
            String distortionsRefinedDir = (String) config.get("distortionsRefinedDir");
            String distortionsFilename = (String) config.get("distortionsFilename");
            TXT.saveVector(distCoeffs_refined, Double.class, distortionsRefinedDir, distortionsFilename, saveDataAppend, saveDataFormatString, saveDataDelimiter);
        }
        
        if (isSaveDataMode) {
            // Save the refined extrinsic matrices
            String extrinsicMatrixRefinedDir = (String) config.get("extrinsicMatrixRefinedDir");
            TXT.saveMatrix_batch(RTMatrixSet_refined, Double.class, extrinsicMatrixRefinedDir, saveDataFormatString, saveDataDelimiter, saveDataEndOfLine, saveDataAppend);
        }        
        
        // Compute and Save front-parallel gray images
        
        
    }
    

}
