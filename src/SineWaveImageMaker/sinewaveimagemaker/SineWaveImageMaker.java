package sinewaveimagemaker;

import brightnesscalibration.BrightnessCalibrationLookUpTable;
import brightnesscalibration.BrightnessCalibrationLookUpTable64;
import core.BufferedImageFactory;
import core.ImageUtils;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.Map;
import static core.Print.println;
import core.TXT;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import sinewavepattern.SineWavePattern;

public class SineWaveImageMaker {
    
    Map<String,Object> config;  
    
    public SineWaveImageMaker(String configPath) {
        // Load the XML configuration file
        XML xml = new XML(configPath);        
        
        // Prepend the base directory to all relative sub directories
        xml.prependBaseDir();
        
        // Get the configuration data as a map
        config = xml.map;   
    }
    
    public Map<String,short[][]> makeSineWaveArrays_10bit() {
        int nRows = (Integer) config.get("nRows");
        int nCols = (Integer) config.get("nCols");
        short amplitude = (Short) config.get("amplitude");
        short offset = (Short) config.get("offset");
        double waveLength = (Double) config.get("waveLength");
        int nPhaseSteps = (Integer) config.get("nPhaseSteps");
        String waveDirection = (String) config.get("waveDirection");        
        Map<String,short[][]> sineWaveArrayStack = SineWavePattern.makeSineArrayStack(nRows, nCols, amplitude, offset, waveLength, nPhaseSteps, waveDirection);          
        return sineWaveArrayStack;
    }  
    
    public Map<String,BufferedImage> makeImages_10bitGray(Map<String,int[][]> arrays) {
        
        Map<String,BufferedImage> outputStack= new TreeMap<>();
        for (String name: arrays.keySet()) {
            int[][] array = arrays.get(name);
            BufferedImage image = BufferedImageFactory.build_10bit_Gray(array);
            outputStack.put(name, image);
        }
        
        return outputStack;
    }     
    
    public Map<String,int[][]> applyLookUpTable(Map<String,int[][]> images) {
        
        // Load the look-up-table
        String brightnessCalibrationTableDir = (String) config.get("brightnessCalibrationTableDir");
        String brightnessCalibrationTableFilename = (String) config.get("brightnessCalibrationTableFilename");
        String brightnessCalibrationFullPath = brightnessCalibrationTableDir + "\\" + brightnessCalibrationTableFilename;
        int[][] lookUpTable = TXT.loadMatrix_Integer(brightnessCalibrationFullPath);

        Map<String,int[][]> output = new HashMap<>();
        for (String name: images.keySet()) {
            int[][] oldImg = images.get(name);
            int[][] newImg = BrightnessCalibrationLookUpTable64.applyLookUpTable(oldImg, lookUpTable);
            output.put(name, newImg);
        }        
        return output;
    }
    
    public Map<String,BufferedImage> calibrateBrightness(Map<String,BufferedImage> images) {
        
        // Load the look-up-table
        String brightnessCalibrationTableDir = (String) config.get("brightnessCalibrationTableDir");
        String brightnessCalibrationTableFilename = (String) config.get("brightnessCalibrationTableFilename");
        String brightnessCalibrationFullPath = brightnessCalibrationTableDir + "\\" + brightnessCalibrationTableFilename;
        List<List<Integer>> lookUpTable = TXT.loadMatrix(brightnessCalibrationFullPath, Integer.class);

        Map<String,BufferedImage> output = new HashMap<>();
        for (String name: images.keySet()) {
            BufferedImage oldImg = images.get(name);
            BufferedImage newImg = BrightnessCalibrationLookUpTable.applyLookUpTable(oldImg, lookUpTable);
            output.put(name, newImg);
        }
        
        return output;
    }
    
    public void savePhaseImages(Map<String,BufferedImage> phaseImages) {
        String sineWavePatternsDir = (String) config.get("sineWavePatternsDir");
        ImageUtils.save_batch(phaseImages, sineWavePatternsDir);
    }    
    
    public static void main(String[] args) {
               
        println("Running the Sine Wave Image Maker App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];
        
        // Load the XML configuration file
        XML xml = new XML(configAbsPath);        
        
        // Prepend the base directory to all relative sub directories
        xml.prependBaseDir();
        
        // Get the configuration data as a map
        Map<String, Object> config = xml.map;  
        
        // Compute the sine wave arrays
        int nRows = (Integer) config.get("nRows");
        int nCols = (Integer) config.get("nCols");
        int amplitude = (Integer) config.get("amplitude");
        int offset = (Integer) config.get("offset");
        double waveLength = (Double) config.get("waveLength");
        int nPhaseSteps = (Integer) config.get("nPhaseSteps");
        String waveDirection = (String) config.get("waveDirection");        
        Map<String,int[][]> sineWaveArrayStack = makeSineArrayStack_int(nRows, nCols, amplitude, offset, waveLength, nPhaseSteps, waveDirection);
        
        // Apply the look-up-table
        boolean doBrightnessCalibration = (Boolean) config.get("doBrightnessCalibration");
        Map<String,int[][]> sineWaveArrayStack_Adj = new TreeMap<>(sineWaveArrayStack);
        if (doBrightnessCalibration) {
            String brightnessCalibrationTableDir = (String) config.get("brightnessCalibrationTableDir");
            String brightnessCalibrationTableFilename = (String) config.get("brightnessCalibrationTableFilename");
            String brightnessCalibrationFullPath = brightnessCalibrationTableDir + "\\" + brightnessCalibrationTableFilename;
            int[][] lookUpTable = TXT.loadMatrix_Integer(brightnessCalibrationFullPath);
            sineWaveArrayStack_Adj = new TreeMap<>();
            for (String name: sineWaveArrayStack.keySet()) {
                int[][] oldImg = sineWaveArrayStack.get(name);
                int[][] newImg = BrightnessCalibrationLookUpTable64.applyLookUpTable(oldImg, lookUpTable);
                sineWaveArrayStack_Adj.put(name, newImg);
            }        
        }
         
        // Set as images      
        Map<String,BufferedImage> sineWaveImages = new TreeMap<>();
        for (String name: sineWaveArrayStack_Adj.keySet()) {
            int[][] array = sineWaveArrayStack_Adj.get(name);
            BufferedImage image = BufferedImageFactory.build_16bit_Gray(array);
            sineWaveImages.put(name, image);
        }        
        
        // Save the images
        println("Saving Images");
        String sineWavePatternsDir = (String) config.get("sineWavePatternsDir");
        ImageUtils.save_batch(sineWaveImages, sineWavePatternsDir);
    }    
    
    public static Map<String, double[][]> makeSineArrayStack_double(int nRows, int nCols, double amplitude, double offset, double waveLength, int nPhaseSteps, String orientation) {
        SortedMap<String, double[][]> imgStack = new TreeMap();
        for (int phase_index = 0; phase_index < nPhaseSteps; phase_index++) {
            double phase = 2*Math.PI/nPhaseSteps * phase_index;
            double[][] newImg = makeSineArray_double(nRows, nCols, amplitude, offset, waveLength, phase, orientation);
            String imgName = "waveLen" + String.format("%.1f", waveLength) + "_phase" + String.format("%.1f", phase);
            imgStack.put(imgName, newImg);
        }
        return imgStack;
    }

    public static Map<String, int[][]> makeSineArrayStack_int(int nRows, int nCols, int amplitude, int offset, double waveLength, int nPhaseSteps, String orientation) {
        SortedMap<String, int[][]> imgStack = new TreeMap();
        for (int phase_index = 0; phase_index < nPhaseSteps; phase_index++) {
            double phase = 2*Math.PI/nPhaseSteps * phase_index;
            int[][] newImg = makeSineArray_int(nRows, nCols, amplitude, offset, waveLength, phase, orientation);
            String imgName = "waveLength" + String.format("%.1f", waveLength) + "_phase" + String.format("%.1f", phase);
            imgStack.put(imgName, newImg);
        }
        return imgStack;
    }
    
    public static Map<String, short[][]> makeSineArrayStack_short(int nRows, int nCols, short amplitude, short offset, double waveLength, int nPhaseSteps, String orientation) {
        SortedMap<String, short[][]> imgStack = new TreeMap();
        for (int phase_index = 0; phase_index < nPhaseSteps; phase_index++) {
            double phase = 2*Math.PI/nPhaseSteps * phase_index;
            short[][] newImg = makeSineArray_short(nRows, nCols, amplitude, offset, waveLength, phase, orientation);
            String imgName = "waveLen" + String.format("%.1f", waveLength) + "_phase" + String.format("%.1f", phase);
            imgStack.put(imgName, newImg);
        }
        return imgStack;
    }
    
    public static double[][] makeSineArray_double(int nRows, int nCols, double amplitude, double offset, double waveLength, double phaseOffset, String orientation) {
                
        // create a new image and get the data array
        double[][] sineWaveArray = new double[nRows][nCols];
                
        // write the sine values       
        for (int row = 0; row < nRows; row++){
            for (int col = 0; col < nCols; col++){ 

                double gray = 0;
                if (orientation.equals("horizontal")) {
                    gray = sineFunc_double(amplitude, offset, waveLength, phaseOffset, col);
                } else if (orientation.equals("vertical")) {
                    gray = sineFunc_double(amplitude, offset, waveLength, phaseOffset, row);
                }

                sineWaveArray[row][col] = gray;
            }
        }

        return sineWaveArray;    
    }
    
    public static int[][] makeSineArray_int(int nRows, int nCols, int amplitude, int offset, double waveLength, double phaseOffset, String orientation) {
                
        // create a new image and get the data array
        int[][] sineWaveArray = new int[nRows][nCols];
                
        // write the sine values       
        for (int row = 0; row < nRows; row++){
            for (int col = 0; col < nCols; col++){ 

                int gray = 0;
                if (orientation.equals("horizontal")) {
                    gray = sineFunc_int(amplitude, offset, waveLength, phaseOffset, col);
                } else if (orientation.equals("vertical")) {
                    gray = sineFunc_int(amplitude, offset, waveLength, phaseOffset, row);
                }

                sineWaveArray[row][col] = gray;
            }
        }

        return sineWaveArray;    
    }
    
    public static short[][] makeSineArray_short(int nRows, int nCols, short amplitude, short offset, double waveLength, double phaseOffset, String orientation) {
                
        // create a new image and get the data array
        short[][] sineWaveArray = new short[nRows][nCols];
                
        // write the sine values       
        for (int row = 0; row < nRows; row++){
            for (int col = 0; col < nCols; col++){ 

                short gray = 0;
                if (orientation.equals("horizontal")) {
                    gray = sineFunc_short(amplitude, offset, waveLength, phaseOffset, col);
                } else if (orientation.equals("vertical")) {
                    gray = sineFunc_short(amplitude, offset, waveLength, phaseOffset, row);
                }

                sineWaveArray[row][col] = gray;
            }
        }

        return sineWaveArray;    
    }

    public static double sineFunc_double(double amplitude, double offset, double waveLength, double phaseOffset, int x){
        return amplitude*Math.cos((2*Math.PI/waveLength) * (x+0.5) - phaseOffset ) + offset; // the -0.5 is critical
    }
    
    public static int sineFunc_int(int amplitude, int offset, double waveLength, double phaseOffset, int x){
        return (int)(amplitude*Math.cos((2*Math.PI/waveLength) * (x+0.5) - phaseOffset ) + offset); // the -0.5 is critical
    }
    
    public static short sineFunc_short(short amplitude, short offset, double waveLength, double phaseOffset, int x){
        return (short)(amplitude*Math.cos((2*Math.PI/waveLength) * (x+0.5) - phaseOffset ) + offset); // the -0.5 is critical
    }
}