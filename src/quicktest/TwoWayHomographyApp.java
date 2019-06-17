package quicktest;

import core.ArrayUtils;
import core.TXT;
import java.util.List;
import frontoparallel.Projection;

public class TwoWayHomographyApp {
    
    public static void main(String[] args) {
        // Load the intrinsic matrix
        String kPath = "C:\\Users\\kfd18\\OneDrive\\kdaquila_SoftwareDev\\Structured-Light\\StructuredLight-Commons\\Test_Resources\\RingGrid\\RingGrid_IntrinsicMatrix_Refined\\intrinsic.txt";
        List<List<Double>> K = TXT.loadMatrix(kPath, Double.class);
        
        // Load the radial coefficients
        String radialCoeffsPath = "C:\\Users\\kfd18\\OneDrive\\kdaquila_SoftwareDev\\Structured-Light\\StructuredLight-Commons\\Test_Resources\\RingGrid\\RingGrid_Distortions_Refined\\radialDistortionCoeffs.txt";
        List<Double> radialCoeffs = TXT.loadVector(radialCoeffsPath, Double.class, ",", "\n");
        
        
        // Load the extrinsic matrix
        String RT_Path = "C:\\Users\\kfd18\\OneDrive\\kdaquila_SoftwareDev\\Structured-Light\\StructuredLight-Commons\\Test_Resources\\RingGrid\\RingGrid_ExtrinsicMatrices_Refined\\0142.txt";
        List<List<Double>> RT = TXT.loadMatrix(RT_Path, Double.class);
        
        
        // Load the image points
        String uvPtsPath = "C:\\Users\\kfd18\\OneDrive\\kdaquila_SoftwareDev\\Structured-Light\\StructuredLight-Commons\\Test_Resources\\RingGrid\\RingGrid_ImagePoints\\0142.txt";
        List<List<Double>> uvPts = TXT.loadMatrix(uvPtsPath, Double.class);
        
        // Load the world points
        String xyzPtsPath = "C:\\Users\\kfd18\\OneDrive\\kdaquila_SoftwareDev\\Structured-Light\\StructuredLight-Commons\\Test_Resources\\RingGrid\\RingGrid_WorldPoints\\worldPoints.txt";
        List<List<Double>> xyzPts = TXT.loadMatrix(xyzPtsPath, Double.class);
        List<List<Double>> xyPts = ArrayUtils.dropZ(xyzPts);
        
        //  Project world to image
        List<List<Double>> uvPts_proj = Projection.toImagePlane(xyPts, K, RT, radialCoeffs);
        
        // Compute error
        double worldToImageError = ArrayUtils.computeAverageError(uvPts, uvPts_proj);
        
        //  Project world to image
        List<List<Double>> xyPts_proj = Projection.toWorldPlane(uvPts, K, RT, radialCoeffs);
        
        // Compute error
        double imageToWorldError = ArrayUtils.computeAverageError(xyPts, xyPts_proj);
        
        // Report to console
        System.out.println("The world to image error is: " + worldToImageError);
        System.out.println("The image to world error is: " + imageToWorldError);
        
    }

    
}
