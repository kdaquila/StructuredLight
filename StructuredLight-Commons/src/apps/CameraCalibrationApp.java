package apps;

import calibrationpattern.rings.ImageRings;
import calibrationpattern.rings.WorldRings;
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
import core.LaplacianFilter;
import core.TXT;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import static core.Print.println;
import frontoparallel.FrontoParallelImage;
import homography.LinearHomography;
import homography.Normalization;
import homography.nonlinear.NonLinearHomography;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class CameraCalibrationApp {
    
    Map<String,Object> config;
    List<List<Double>> xyzPts;
    Map<String, List<List<Double>>> uvPtSets;
    List<List<Double>> intrinsicMatrix;
    Map<String,List<List<Double>>> RTMatrixSet;
    List<Double> distCoeffs;
    
    
    public CameraCalibrationApp(String configPath) {
        config = XML.loadMap(configPath, "config");        
    }
    
    public void findImagePoints(Map<String,BufferedImage> grayImages, boolean isSubPixel) {
        
        boolean isSaveDataMode = (boolean)config.get("isSaveDataMode");
        boolean isSaveImageMode = (boolean)config.get("isSaveImageMode");
        String saveDataFormatString = (String) config.get("saveDataFormatString");
        String saveDataDelimiter = (String) config.get("saveDataDelimiter");
        String saveDataEndOfLine = "\n";
        Boolean saveDataAppend = false;                     
        
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
        uvPtSets = ImageRings.computeCenters_batch(contourSets, hierarchySets, nPts);
        
        if (isSaveDataMode) {
            // Save the Image Points
            String imagePointsDir = (String) config.get("imagePointsDir");            
            TXT.saveMatrix_batch(uvPtSets, Double.class, imagePointsDir, saveDataFormatString, saveDataDelimiter, saveDataEndOfLine, saveDataAppend);
        }

        // Sort the image points
        println("Sorting the image points");
        uvPtSets = ImageRings.sortCentersRowMajor_batch(uvPtSets, nRows, nCols);
        
        if (isSubPixel) {                
            // Refine ring centers to sub pixel accuracy
            println("Refining image points to subPixel Accuracy... ");

            // Compute the average ring outer radius
            Map<String, Map<String,Double>> avgWidthSet = ImageRings.findAvgRingWidths_batch(contourSets, hierarchySets, nPts); 
            
            // Compute the laplacian kernal parameters
            Map<String, Map<String,Double>> laplacianKernalParamSets = LaplacianFilter.computeKernalParameters_batch(avgWidthSet);
            
            // Compute the laplacian images
            Map<String, BufferedImage> laplacianImages = LaplacianFilter.laplacianFilter_batch(grayImages, laplacianKernalParamSets); 
            
            if (isSaveImageMode) {
                // Save the laplacian images
                String imagesLaplacianDir = (String) config.get("imagesLaplacianDir");
                ImageUtils.save_batch(laplacianImages, imagesLaplacianDir);
            }

            // Find the ring centers to subPixel accuracy  
            uvPtSets = ImageRings.refineCenters_batch(uvPtSets, laplacianImages, laplacianKernalParamSets);
        }  
        
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
    }
    
    public void findImagePointsFromFrontoParallel (Map<String,BufferedImage> frontoParallelImages) {
        
        // Update the uvPtSets
        boolean isSubPixel = false;
        findImagePoints(frontoParallelImages, isSubPixel);
        
        // Correct the uvPtSets
        double frontParallelImageScale = (Double) config.get("frontParallelImageScale");
        Map<String, List<List<Double>>> uvPtSet_New = new HashMap<>();
        for (String name: uvPtSets.keySet()) {
            
            // Get the current uvPts
            List<List<Double>> uvPts = uvPtSets.get(name);
            
            // Convert Fronto-Parallel uv coordinates to world xy coordinates
            List<List<Double>> xy = ArrayUtils.scalarMultiply_Double2D(uvPts, frontParallelImageScale);
            double dx = (Double) config.get("dx");
            xy =  ArrayUtils.scalarAdd_Double2D(xy, -dx);
            
            // Add a Z component of zero
            List<List<Double>> xyz = new ArrayList<>(xy);
            for (List<Double> pt: xy) {
                pt.add(0.0);
            }           
            
            // Get RT Matrix for this view
            List<List<Double>> RT = RTMatrixSet.get(name);
            
            // Project world xyz coorindates to image uv coorindates
            List<List<Double>> uvPtsNew = Projection.projectPoints(xyz, intrinsicMatrix, RT, distCoeffs);
            
            uvPtSet_New.put(name, uvPtsNew);
        }
        
        uvPtSets = uvPtSet_New;
        
        boolean isSaveDataMode = (boolean)config.get("isSaveDataMode");
        String saveDataFormatString = (String) config.get("saveDataFormatString");
        String saveDataDelimiter = (String) config.get("saveDataDelimiter");
        String saveDataEndOfLine = "\n";
        Boolean saveDataAppend = false;  
        if (isSaveDataMode) {
            // Save the Image Points
            String imagePointsDir = (String) config.get("imagePointsDir");            
            TXT.saveMatrix_batch(uvPtSets, Double.class, imagePointsDir, saveDataFormatString, saveDataDelimiter, saveDataEndOfLine, saveDataAppend);
        }
        
        
    }
    
    public void computeCalibration() {
        
        int nRows = (Integer) config.get("nRows");
        int nCols = (Integer) config.get("nCols");
        boolean isSaveDataMode = (boolean)config.get("isSaveDataMode");
        String saveDataFormatString = (String) config.get("saveDataFormatString");
        String saveDataDelimiter = (String) config.get("saveDataDelimiter");
        String saveDataEndOfLine = "\n";
        Boolean saveDataAppend = false;  
        
        // Compute the World Points
        println("Computing the world points");
        double dx = (Double) config.get("dx");
        double dy = (Double) config.get("dy");
        xyzPts = WorldRings.computeCenters(nRows, nCols, dx, dy);
        
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
        intrinsicMatrix = IntrinsicMatrix.compute(symmetricMatrix);
        
        if (isSaveDataMode) {
            // Save the intrinsic matrix
            String intrinsicMatrixDir = (String) config.get("intrinsicMatrixDir");
            String intrinsicMatrixFilename = (String) config.get("intrinsicMatrixFilename");
            TXT.saveMatrix(intrinsicMatrix, Double.class, intrinsicMatrixDir, intrinsicMatrixFilename, saveDataFormatString);
        }
        
        // Compute the extrinsic matrices
        println("Computing the extrinsic camera matrix");
        RTMatrixSet = ExtrinsicMatrix.compute_batch(homographySets, intrinsicMatrix);
        
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
        distCoeffs = RadialDistortion.compute(intrinsicMatrix, uvPts_allViews, uvPtsProj_allViews);
        
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
        intrinsicMatrix = optimization.getK_refined();
        distCoeffs = optimization.getRadialCoeffs_refined();
        RT_allViews = optimization.getRT_allViews_refined();
        
        // Store the RT matrices into a map
        for(int i = 0; i < RT_allViews.size(); i++) {
            RTMatrixSet.put(RTMatrixSetNames.get(i), RT_allViews.get(i));
        }
        
        if (isSaveDataMode) {            
            // Save the refined intrinsic matrix
            String intrinsicMatrixRefinedDir = (String) config.get("intrinsicMatrixRefinedDir");
            String intrinsicMatrixFilename = (String) config.get("intrinsicMatrixFilename");
            TXT.saveMatrix(intrinsicMatrix, Double.class, intrinsicMatrixRefinedDir, intrinsicMatrixFilename, saveDataFormatString);            
        }        
        
        if (isSaveDataMode) {
            // Save the refined distortions
            String distortionsRefinedDir = (String) config.get("distortionsRefinedDir");
            String distortionsFilename = (String) config.get("distortionsFilename");
            TXT.saveVector(distCoeffs, Double.class, distortionsRefinedDir, distortionsFilename, saveDataAppend, saveDataFormatString, saveDataDelimiter);
        }
        
        if (isSaveDataMode) {
            // Save the refined extrinsic matrices
            String extrinsicMatrixRefinedDir = (String) config.get("extrinsicMatrixRefinedDir");
            TXT.saveMatrix_batch(RTMatrixSet, Double.class, extrinsicMatrixRefinedDir, saveDataFormatString, saveDataDelimiter, saveDataEndOfLine, saveDataAppend);
        }    
    }    
    
    public void computeCalibrationIterative(Map<String,BufferedImage> grayImages) {
        
        Map<String,Double> errorSet;
        
        int nIter = (Integer) config.get("nIter");
        
        for (int i = 0; i < nIter; i++) {
            
            // Compute fronto-parallel images
            println("Computing the fronto-parallel images, iteration: " + i);
            Map<String, BufferedImage> frontoParallelImages = computeFrontoParallelImages(grayImages);
            
            // Find Image Points
            findImagePointsFromFrontoParallel(frontoParallelImages);
            
            // Compute Intrinsic/Extrinsic/Distortions
            println("Computing the Intrinsic/Extrinsic/Distortions");
            computeCalibration();
            
            // Compute reprojection errors
            println("Computing reprojection errors");
            errorSet = computeErrors();

            // Display repojection errors
            for (String name: errorSet.keySet()) {
                println("Error " + name + ": " + String.format("%.4f",errorSet.get(name)));
            }
        }
    }
    
    public Map<String, BufferedImage> loadRGBImages() {
        String imagesRGBDir = (String) config.get("imagesRGBDir");
        return ImageUtils.load_batch(imagesRGBDir);
    }
    
    public Map<String, BufferedImage> convertImages_rgbToGray(Map<String, BufferedImage> rgbImages) {
        Map<String, BufferedImage> output = ImageUtils.color2Gray_batch(rgbImages);
        
        boolean isSaveImageMode = (Boolean) config.get("isSaveImageMode");
        
        if (isSaveImageMode) {
            String imagesGrayDir = (String) config.get("imagesGrayDir");
            ImageUtils.save_batch(output, imagesGrayDir);
        }   
        
        return output;
    }
    
    public Map<String, BufferedImage> computeFrontoParallelImages(Map<String, BufferedImage> grayImages) {
        
        FrontoParallelImage frontoParallelImager = new FrontoParallelImage(intrinsicMatrix, distCoeffs);
        double dx = (Double) config.get("dx");
        double dy = (Double) config.get("dy");
        int nCols = (Integer) config.get("nCols");
        int nRows = (Integer) config.get("nRows");        
        double xMin = -dx;
        double xMax = nCols*dx;
        double yMin = -dy;
        double yMax = nRows*dy;
        double frontParallelImageScale = (Double) config.get("frontParallelImageScale");
        Map<String,BufferedImage> frontoParallelImageSet = frontoParallelImager.projectImage_batch(grayImages, RTMatrixSet, xMin, xMax, yMin, yMax, frontParallelImageScale);
        
        // Normalize the fronto-parallel image range
        println("Normalizing the fronto-parallel images");
        double normalizeMin = 0.0;
        double normalizeMax = 255.0;
        Map<String,BufferedImage> frontoParallelImageSet_norm = ImageUtils.normalize_batch(frontoParallelImageSet, normalizeMin, normalizeMax);
        
        boolean isSaveImageMode = (boolean)config.get("isSaveImageMode");
        if (isSaveImageMode) {
            // Save the fronto-parallel images
            String imagesGrayFrontoParallelDir = (String) config.get("imagesGrayFrontoParallelDir");
            ImageUtils.save_batch(frontoParallelImageSet_norm, imagesGrayFrontoParallelDir);
        }
        
        return frontoParallelImageSet_norm;
    }
    
    public Map<String,Double> computeErrors() {
        return ProjectionError.computeReprojectError_batch(xyzPts, intrinsicMatrix, distCoeffs, uvPtSets, RTMatrixSet);
    }
    

    
    public static void main(String[] args) {
               
        println("Running the Camera Calibration App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];
        
        CameraCalibrationApp app = new CameraCalibrationApp(configAbsPath);
        
        // Load RGB images
        println("Loading RGB Images");
        Map<String, BufferedImage> rgbImages = app.loadRGBImages();
        
        // Convert RGB to Gray images
        println("Converting RGB to Gray");
        Map<String, BufferedImage> grayImages = app.convertImages_rgbToGray(rgbImages);
        
        
        
        // Find Image Points
        boolean isSubPixel = false;
        app.findImagePoints(grayImages, isSubPixel);
        
        // Compute Intrinsic/Extrinsic/Distortions
        app.computeCalibration();
        
        // Compute reprojection errors
        println("Computing reprojection errors");
        Map<String,Double> errorSet = app.computeErrors();
        
        // Display repojection errors
        for (String name: errorSet.keySet()) {
            println("Error " + name + ": " + String.format("%.4f",errorSet.get(name)));
        }
        
        // Compute Intrinsic/Extrinsic/Distortions iteratively
        app.computeCalibrationIterative(grayImages);
    }
    
}