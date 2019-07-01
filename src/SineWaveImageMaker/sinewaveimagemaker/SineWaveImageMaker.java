package sinewaveimagemaker;

import brightnesscalibration.BrightnessCalibrationLookUpTable;
import core.ImageUtils;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.Map;
import static core.Print.println;
import core.TXT;
import java.util.HashMap;
import java.util.List;
import phaseimages.SineWaveImage;

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
    
    public Map<String,BufferedImage> drawPhaseImages() {
        int nRows = (Integer) config.get("nRows");
        int nCols = (Integer) config.get("nCols");
        double amplitude = (Double) config.get("amplitude");
        double offset = (Double) config.get("offset");
        double waveLength = (Double) config.get("waveLength");
        int nPhaseSteps = (Integer) config.get("nPhaseSteps");
        String waveDirection = (String) config.get("waveDirection");        
        Map<String,BufferedImage> phaseImages = SineWaveImage.makeSineWaveImageStack(nRows, nCols, amplitude, offset, waveLength, nPhaseSteps, waveDirection);  
        return phaseImages;
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
               
        println("Running the Phase Image Maker App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];
        
        SineWaveImageMaker app = new SineWaveImageMaker(configAbsPath);
        
        // Draw the images
        println("Drawing Images");
        Map<String,BufferedImage> phaseImages = app.drawPhaseImages();
        
        // Apply the Brightness Calibration
        boolean doBrightnessCalibration = (Boolean) app.config.get("doBrightnessCalibration");
        if (doBrightnessCalibration) {
            println("Applying Brightness Calibration");
            phaseImages = app.calibrateBrightness(phaseImages);
        }
        
        
        // Save the images
        println("Saving Images");
        app.savePhaseImages(phaseImages);
    }    
}