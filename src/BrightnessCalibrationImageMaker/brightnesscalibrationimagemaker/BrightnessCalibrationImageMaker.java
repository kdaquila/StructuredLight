package brightnesscalibrationimagemaker;

import core.ImageUtils;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.Map;
import static core.Print.println;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.DataBufferInt;
import java.util.HashMap;

public class BrightnessCalibrationImageMaker {
    
    Map<String,Object> config;  
    
    public BrightnessCalibrationImageMaker(String configPath) {
        // Load the XML configuration file
        XML xml = new XML(configPath);        
        
        // Prepend the base directory to all relative sub directories
        xml.prependBaseDir();
        
        // Get the configuration data as a map
        config = xml.map;   
    }
    
    public Map<String,BufferedImage> drawImages() {
        int nRows = (Integer) config.get("nRows");
        int nCols = (Integer) config.get("nCols");
        double minValue = (Double) config.get("minValue");
        double maxValue = (Double) config.get("maxValue");
        double stepValue = (Double) config.get("stepValue");
        
        Map<String,BufferedImage> outputImages = new HashMap<>();
        
        for (double value = minValue; value <= maxValue; value += stepValue) {
            
            // create a new image and get the data array
            BufferedImage singleColorImg = new BufferedImage(nCols, nRows, BufferedImage.TYPE_INT_RGB);
            int[] singleColorArray = ((DataBufferInt) singleColorImg.getRaster().getDataBuffer()).getData();
            
            // write the gray values 
            int gray = (int) value;
            int rgb = (gray << 16) + (gray << 8) + gray;
            for (int index = 0; index < nRows*nCols; index++){
                singleColorArray[index] = rgb;                
            }
            
            // define display name string
            String displayName = String.format("%d", gray);
            
            // create the graphics object
            Graphics2D g = (Graphics2D) singleColorImg.getGraphics();
            
            // set the font
            int hText = 100;
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, hText));
            
            // draw the background rect
            int xBackgroundRect = 0;
            int yBackgroundRect = 0; 
            double scaleFactor = 1.2;
            int wBackgroundRect = (int)(g.getFontMetrics().stringWidth(displayName)*scaleFactor);
            int hBackgroundRect = (int) (hText*1.2); 
            g.setColor(Color.WHITE);
            g.fillRect(xBackgroundRect, yBackgroundRect, wBackgroundRect, hBackgroundRect);
            
            // add text            
            int xText = (int)(g.getFontMetrics().stringWidth(displayName)*(scaleFactor-1)/2.0);;
            int yText = hText; 
            g.setColor(Color.BLACK);            
            g.drawString(displayName, xText, yText);
            
            // store the images
            outputImages.put(String.valueOf(gray), singleColorImg);
        }  
        
        return outputImages;
    }       
    
    public void saveImages(Map<String,BufferedImage> imageStack) {
        String brightnessCalibrationPatternDir = (String) config.get("brightnessCalibrationPatternDir");
        ImageUtils.save_batch(imageStack, brightnessCalibrationPatternDir);
    }    
    
    public static void main(String[] args) {
               
        println("Running the Brightness Calibration Image Maker App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];
        
        BrightnessCalibrationImageMaker app = new BrightnessCalibrationImageMaker(configAbsPath);
        
        // Draw the images
        println("Drawing Images");
        Map<String,BufferedImage> brightnessImages = app.drawImages();
        
        // Save the images
        println("Saving Images");
        app.saveImages(brightnessImages);
    }    
}