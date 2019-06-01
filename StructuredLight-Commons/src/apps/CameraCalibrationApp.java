package apps;

import cameramatrix_development.CameraMatrixDecomposition;
import cameramatrix_development.PinholeCameraModel;
import core.ArrayUtils;
import core.TXT;
import core.XML;
import java.util.List;

public class CameraCalibrationApp {
    
    public static void main(String[] args) {
        
        System.out.println("Running the CameraCalibrationApp:");
        
        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Please provide one argument which is the path to the XML configuration file");
        } 
        
        // Parse the arguments
        String configPath = args[0];
        
        // Load the configuration variables
        System.out.print("Loading the configuration ... ");
        
        XML conf = new XML(configPath);          
        String imagePointsPath = conf.getString("/config/imagePointsPath"); 
        String worldPointsPath = conf.getString("/config/worldPointsPath");
        String formatString = conf.getString("/config/formatString");
        String KSavePath = conf.getString("/config/KSavePath");
        String RSavePath = conf.getString("/config/RSavePath");
        String TSavePath = conf.getString("/config/TSavePath");
        
        System.out.println("Done");
        
        // Load the image points
        List<List<Double>> imagePts = TXT.loadMatrix(imagePointsPath, Double.class);
        
        // Load the world points
        List<List<Double>> worldPts = TXT.loadMatrix(worldPointsPath, Double.class);
        
        // Compute the full camera calibration matrix
        PinholeCameraModel camera = new PinholeCameraModel();
        camera.computeParameters(imagePts, worldPts);        
        List<List<Double>> cameraMatrix = camera.getMatrix();
        
        // Compute the intrinsic and extrinsic matrices
        CameraMatrixDecomposition camDecomp = new CameraMatrixDecomposition(ArrayUtils.ListToArray_Double2D(cameraMatrix));
        camDecomp.decompose();
        List<List<Double>> K = camDecomp.getK();
        List<List<Double>> R = camDecomp.getR();
        List<List<Double>> T = camDecomp.getT();
                
        // Save the intrinsic and extrinsic matrices
        TXT.saveMatrix(K, Double.class, KSavePath, formatString);
        TXT.saveMatrix(R, Double.class, RSavePath, formatString);
        TXT.saveMatrix(T, Double.class, TSavePath, formatString);
        
    }
}
