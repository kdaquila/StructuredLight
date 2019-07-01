package brightnesscalibration;

import core.FITS;
import core.Print;
import static core.Print.println;
import core.TXT;
import core.XML;
import java.util.Map;

public class BrightnessCalibration64 {
    
     Map<String,Object> config;  
    
    public BrightnessCalibration64(String configPath) {
        // Load the XML configuration file
        XML xml = new XML(configPath);        
        
        // Prepend the base directory to all relative sub directories
        xml.prependBaseDir();
        
        // Get the configuration data as a map
        config = xml.map;   
    }
    
    public int[] loadGivenValues() {
        // Enumerate the given gray values
        int minValue = (Integer) config.get("minValue");
        int maxValue = (Integer) config.get("maxValue");
        int stepValue = (Integer) config.get("stepValue");
        int nSteps = (int)((maxValue - minValue)/stepValue + 1);
        int[] givenValues = new int[nSteps];
        int value = minValue;
        for (int step_num = 0; step_num < nSteps; step_num++) {
            givenValues[step_num] = value;
            value += stepValue;
        }
        return givenValues;
    }
    
    public void saveLookUpTable(int[][] lookUpTable) {
        String brightnessCalibrationDataDir = (String) config.get("brightnessCalibrationDataDir");
        String brightnessCalibrationDataFilename = (String) config.get("brightnessCalibrationDataFilename");
        String formatString = (String) config.get("formatString");
        TXT.saveMatrix(lookUpTable, formatString, brightnessCalibrationDataDir, brightnessCalibrationDataFilename);
    }

    public static void main(String[] args) {
        println("Running the Brightness Calibration App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];
        
        BrightnessCalibration64 app = new BrightnessCalibration64(configAbsPath);
        
        // Load images
        String brightnessCalibrationImageDir = (String) app.config.get("brightnessCalibrationImageDir");   
        Map<String, double[][]> imgStack = FITS.loadArray_Batch(brightnessCalibrationImageDir);

        // Load the given values
        int[] givenValues = app.loadGivenValues();

        Print.println("Computing the look-up-table");
        BrightnessCalibrationLookUpTable64 lut64 = new BrightnessCalibrationLookUpTable64(imgStack, givenValues);
        int[][] lookUpTable = lut64.computeLookUpTable();

        Print.println("Saving the look-up-table");
        app.saveLookUpTable(lookUpTable); 
        
        int[] computedInputValues = lut64.computedInputValues;
        int[] givenInputValues = lut64.givenInputValues;
        int[] nominalOutputValues = lut64.nominalOutputValues;
        double[] measuredOutputValues = lut64.measuredOutputValues;
        String debugPath = "C:\\Users\\kfd18\\kfd18_Downloads";
                
    }
    
}
