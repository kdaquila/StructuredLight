package apps;

import core.XML;
import java.util.Map;

public class CameraCalibrationApp {
    
    public static void main(String[] args) {
        
        System.out.println("Running the Camera Calibration App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];

        // Load the configuration variables      
        Map<String,Object> configMap = XML.loadMap(configAbsPath, "config");
        
        
        
        
    }
    

}
