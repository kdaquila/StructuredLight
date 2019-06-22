package phaseimagemakerapp;

import core.ImageUtils;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.Map;
import static core.Print.println;
import phaseimages.SineWaveImage;

public class PhaseImageMakerApp {
    
    Map<String,Object> config;
    Map<String, BufferedImage> phaseImages;    
    
    public PhaseImageMakerApp(String configPath) {
        // Load the XML configuration file
        XML xml = new XML(configPath);        
        
        // Prepend the base directory to all relative sub directories
        xml.prependBaseDir();
        
        // Get the configuration data as a map
        config = xml.map;   
    }
    
    public void drawPhaseImages() {
        int nRows = (Integer) config.get("nRows");
        int nCols = (Integer) config.get("nCols");
        double amplitude = (Double) config.get("amplitude");
        double offset = (Double) config.get("offset");
        double waveLength = (Double) config.get("waveLength");
        int nPhaseSteps = (Integer) config.get("nPhaseSteps");
        String waveDirection = (String) config.get("waveDirection");        
        phaseImages = SineWaveImage.makeSineWaveImageStack(nRows, nCols, amplitude, offset, waveLength, nPhaseSteps, waveDirection);  
    }       
    
    public void savePhaseImages() {
        String phasePatternsDir = (String) config.get("phasePatternsDir");
        ImageUtils.save_batch(phaseImages, phasePatternsDir);
    }    
    
    public static void main(String[] args) {
               
        println("Running the Phase Image Maker App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];
        
        PhaseImageMakerApp app = new PhaseImageMakerApp(configAbsPath);
        
        // Draw the images
        println("Drawing Images");
        app.drawPhaseImages();
        
        // Save the images
        println("Saving Images");
        app.savePhaseImages();
    }    
}