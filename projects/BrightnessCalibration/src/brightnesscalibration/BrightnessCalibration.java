package brightnesscalibration;

import core.FITS;
import core.ImageStackUtils;
import core.Print;
import static core.Print.println;
import core.TXT;
import core.XML;
import java.util.Map;
import lookuptable.LookUpTable_InverseRodbard;

public class BrightnessCalibration {
    
     Map<String,Object> config;  
    
    public BrightnessCalibration(String configPath) {
        // Load the XML configuration file
        XML xml = new XML(configPath);        
        
        // Prepend the base directory to all relative sub directories
        xml.prependBaseDir();
        
        // Get the configuration data as a map
        config = xml.map;   
    }
//    
//    public List<Double> loadGivenValues() {
//        // Enumerate the given gray values
//        double minValue = (Double) config.get("minValue");
//        double maxValue = (Double) config.get("maxValue");
//        double stepValue = (Double) config.get("stepValue");
//        List<Double> givenValues = new ArrayList<>();
//        for (double value = minValue; value <= maxValue; value += stepValue) {
//            givenValues.add(value);
//        }
//        return givenValues;
//    }
//    
//    public void saveLookUpTable(List<List<Integer>> lookUpTable) {
//        String brightnessCalibrationDataDir = (String) config.get("brightnessCalibrationDataDir");
//        String brightnessCalibrationDataFilename = (String) config.get("brightnessCalibrationDataFilename");
//        String formatString = (String) config.get("formatString");
//        TXT.saveMatrix(lookUpTable, Integer.class, brightnessCalibrationDataDir, brightnessCalibrationDataFilename, formatString);
//    }
//
//    public static void main(String[] args) {
//        println("Running the Brightness Calibration App:");
//
//        // Validate arguments
//        if (args.length == 0) {
//            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
//        } 
//
//        // Parse the arguments
//        String configAbsPath = args[0];
//        
//        BrightnessCalibration app = new BrightnessCalibration(configAbsPath);
//        
//        // Load images
//        String brightnessCalibrationImageDir = (String) app.config.get("brightnessCalibrationImageDir");         
//        Map<String,BufferedImage> imgStack = ImageUtils.load_batch(brightnessCalibrationImageDir);  
//
//        // Convert to Gray
//        Map<String,BufferedImage> grayStack = ImageUtils.color2Gray_batch(imgStack);
//
//        // Load the given values
//        List<Double> givenValues = app.loadGivenValues();
//
//        Print.println("Computing the look-up-table");
//        List<List<Integer>> lookUpTable = BrightnessCalibrationLookUpTable.computeLookUpTable(grayStack, givenValues);
//
//        Print.println("Saving the look-up-table");
//        app.saveLookUpTable(lookUpTable);
//        
//                
//    }
    
    public int[][] computeLookUpTable(Map<String, int[][]> imgStack) {

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
        double[] measuredOutputs = ImageStackUtils.getAvgZProfile_int(imgStack, roiX, roiY, roiWidth, roiHeight);
        
        // Compute the nominalOutputs
        int nNominalPts = (int) (Math.floor(measuredOutputs[measuredOutputs.length - 1]) - Math.ceil(measuredOutputs[0]) + 1);
        int[] nominalOutputs = new int[nNominalPts];
        int outputValue = (int) Math.ceil(measuredOutputs[0]);
        for (int i = 0; i < nNominalPts; i++) {            
            nominalOutputs[i] = outputValue;
            outputValue++;
        }
        
        LookUpTable_InverseRodbard lut = new LookUpTable_InverseRodbard(givenInputs, measuredOutputs, nominalOutputs);
        int[][] lookUpTable = lut.computeTable();

        return lookUpTable;
    }

    public static void main(String[] args) {
        println("Running the Brightness Calibration App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];
        
        BrightnessCalibration app = new BrightnessCalibration(configAbsPath);
               
        // Load images
        String brightnessCalibrationImageDir = (String) app.config.get("brightnessCalibrationImageDir");
        Map<String, int[][]> imgStack = ImageStackUtils.loadStack_8BitGray(brightnessCalibrationImageDir);

        // Compute the look up table
        Print.println("Computing the look-up-table");
        int[][] lookUpTable = app.computeLookUpTable(imgStack);
        
        // Save the look up table
        Print.println("Saving the look-up-table");
        String brightnessCalibrationDataDir = (String) app.config.get("brightnessCalibrationDataDir");
        String brightnessCalibrationDataFilename = (String) app.config.get("brightnessCalibrationDataFilename");
        String formatString = (String) app.config.get("formatString");
        TXT.saveMatrix(lookUpTable, formatString, brightnessCalibrationDataDir, brightnessCalibrationDataFilename);     
                
    }
    
}
