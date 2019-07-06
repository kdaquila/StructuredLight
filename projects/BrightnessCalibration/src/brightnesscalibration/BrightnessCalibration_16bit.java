package brightnesscalibration;

import core.FITS;
import core.ImageStackUtils;
import core.Print;
import static core.Print.println;
import core.TXT;
import core.XML;
import java.util.Map;
import lookuptable.LookUpTable_InverseRodbard;

public class BrightnessCalibration_16bit {
    
    Map<String,Object> config;  
    
    public BrightnessCalibration_16bit(String configPath) {
        // Load the XML configuration file
        XML xml = new XML(configPath);        
        
        // Prepend the base directory to all relative sub directories
        xml.prependBaseDir();
        
        // Get the configuration data as a map
        config = xml.map;   
    }
    
    public int[][] computeLookUpTable(Map<String, double[][]> imgStack) {

        // Compute the givenInputs
        int minValue = (Integer) config.get("minValue");
        int maxValue = (Integer) config.get("maxValue");
        int stepValue = (Integer) config.get("stepValue");
        int nSteps = (int)((maxValue - minValue)/stepValue + 1);
        int[] givenInputs = new int[nSteps];
        int givenInput = minValue;
        for (int step_num = 0; step_num < nSteps; step_num++) {
            givenInputs[step_num] = givenInput;
            givenInput += stepValue;
        }
        
        // Get the measuredOutputs
        int roiX = (Integer) config.get("roiX");
        int roiY = (Integer) config.get("roiY");
        int roiWidth = (Integer) config.get("roiWidth");
        int roiHeight = (Integer) config.get("roiHeight");
        double[] measuredOutputs = ImageStackUtils.getAvgZProfile_double(imgStack, roiX, roiY, roiWidth, roiHeight);
        
        // Compute the nominalOutputs
        int nNominalPts = (int) (Math.floor(measuredOutputs[measuredOutputs.length - 1]) - Math.ceil(measuredOutputs[0]) + 1);
        int[] nominalOutputs = new int[nNominalPts];
        int outputValue = (int) Math.ceil(measuredOutputs[0]);
        for (int i = 0; i < nNominalPts; i++) {            
            nominalOutputs[i] = outputValue;
            outputValue++;
        }
        
        LookUpTable_InverseRodbard lut64 = new LookUpTable_InverseRodbard(givenInputs, measuredOutputs, nominalOutputs);
        int[][] lookUpTable = lut64.computeTable();

        return lookUpTable;
    }
    
    public double[] computeLookUpFunc(Map<String, double[][]> imgStack) {

        // Compute the givenInputs
        int minValue = (Integer) config.get("minValue");
        int maxValue = (Integer) config.get("maxValue");
        int stepValue = (Integer) config.get("stepValue");
        int nSteps = (int)((maxValue - minValue)/stepValue + 1);
        int[] givenInputs = new int[nSteps];
        int givenInput = minValue;
        for (int step_num = 0; step_num < nSteps; step_num++) {
            givenInputs[step_num] = givenInput;
            givenInput += stepValue;
        }
        
        // Get the measuredOutputs
        int roiX = (Integer) config.get("roiX");
        int roiY = (Integer) config.get("roiY");
        int roiWidth = (Integer) config.get("roiWidth");
        int roiHeight = (Integer) config.get("roiHeight");
        double[] measuredOutputs = ImageStackUtils.getAvgZProfile_double(imgStack, roiX, roiY, roiWidth, roiHeight);
        
        // Compute the nominalOutputs
        int nNominalPts = (int) (Math.floor(measuredOutputs[measuredOutputs.length - 1]) - Math.ceil(measuredOutputs[0]) + 1);
        int[] nominalOutputs = new int[nNominalPts];
        int outputValue = (int) Math.ceil(measuredOutputs[0]);
        for (int i = 0; i < nNominalPts; i++) {            
            nominalOutputs[i] = outputValue;
            outputValue++;
        }
        
        LookUpTable_InverseRodbard lut = new LookUpTable_InverseRodbard(givenInputs, measuredOutputs, nominalOutputs);
        double[] params = lut.computeParams();
        
        double[] paramsWithBounds = new double[params.length + 2];
        System.arraycopy(params, 0, paramsWithBounds, 0, params.length);
        paramsWithBounds[params.length] = nominalOutputs[0];
        paramsWithBounds[params.length + 1] = nominalOutputs[nominalOutputs.length - 1];
        
        return paramsWithBounds;
    }

    public static void main(String[] args) {
        println("Running the Brightness Calibration App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];
        
        BrightnessCalibration_16bit app = new BrightnessCalibration_16bit(configAbsPath);
        
        // Load images
        String brightnessCalibrationImageDir = (String) app.config.get("brightnessCalibrationImageDir");   
        Map<String, double[][]> imgStack = FITS.loadArray_Batch(brightnessCalibrationImageDir);

        // Compute the look up table
        Print.println("Computing the look-up-table");
        double[] params = app.computeLookUpFunc(imgStack);
        
        // Save the look up table
        Print.println("Saving the look-up-table");
        String brightnessCalibrationDataDir = (String) app.config.get("brightnessCalibrationDataDir");
        String brightnessCalibrationDataFilename = (String) app.config.get("brightnessCalibrationDataFilename");
        String formatString = (String) app.config.get("formatString");
        TXT.saveVector(params, formatString, brightnessCalibrationDataDir, brightnessCalibrationDataFilename);                
    }
    
}
