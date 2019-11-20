package brightnesscalibrationimagemaker;

import core.ImageUtils;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.Map;
import static core.Print.println;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.DataBufferUShort;
import java.util.HashMap;
import java.util.TreeMap;

public class BrightnessCalibrationImageMaker16bit {
    
    Map<String,Object> config;  
    
    public BrightnessCalibrationImageMaker16bit(String configPath) {
        // Load the XML configuration file
        XML xml = new XML(configPath);        
        
        // Prepend the base directory to all relative sub directories
        xml.prependBaseDir();
        
        // Get the configuration data as a map
        config = xml.map;   
    }
    
    public Map<String,BufferedImage> drawImages_16bit() {
        int nRows = (Integer) config.get("nRows");
        int nCols = (Integer) config.get("nCols");
        int minValue = (Integer) config.get("minValue");
        int maxValue = (Integer) config.get("maxValue");
        int stepValue = (Integer) config.get("stepValue");
        
        Map<String,BufferedImage> outputImages = new HashMap<>();
        
        for (int value = minValue; value <= maxValue; value += stepValue) {
            
            if (value > 65535 || value < 0) {
                throw new RuntimeException("Can't draw the image. Value " + value + " is out of the range (0,65535)");
            }
            
            // create a new image and get the data array
            BufferedImage img = new BufferedImage(nCols, nRows, BufferedImage.TYPE_USHORT_GRAY);
            short[] data = ((DataBufferUShort)img.getRaster().getDataBuffer()).getData();
 
            // create the graphics object
            Graphics2D g = (Graphics2D) img.getGraphics();
            
            // draw the background
            for (int row_num = 0; row_num < nRows ; row_num++) {
                for (int col_num = 0; col_num < nCols; col_num++) {
                    int index = row_num*nCols + col_num;
                    data[index] = (short)65535;
                }
            }
            
            // draw the main area rectangle
            int borderWidth = 200;
            for (int row_num = borderWidth; row_num < nRows - borderWidth; row_num++) {
                for (int col_num = borderWidth; col_num < nCols - borderWidth; col_num++) {
                    int index = row_num*nCols + col_num;
                    data[index] = (short) value;
                }
            }
            
            // draw the border around the main area rectangle
            int row_num = borderWidth;
            for (int col_num = borderWidth; col_num < nCols - borderWidth; col_num++) {
                int index = row_num*nCols + col_num;
                data[index] = (short) value;
            }
            row_num = nRows - borderWidth;
            for (int col_num = borderWidth; col_num < nCols - borderWidth; col_num++) {
                int index = row_num*nCols + col_num;
                data[index] = (short) value;
            }
            int col_num = borderWidth;
            for (row_num = borderWidth; row_num < nRows - borderWidth; row_num++) {
                int index = row_num*nCols + col_num;
                data[index] = (short) value;
            }
            col_num = nCols - borderWidth;
            for (row_num = borderWidth; row_num < nRows - borderWidth; row_num++) {
                int index = row_num*nCols + col_num;
                data[index] = (short) value;
            }
  
            // define display name string
            String displayName = String.format("%d", value);                      
            
            // set the font
            int hText = 100;
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, hText));
            
            // draw the font background rect
            double scaleFactor = 1.2;
            int wBackgroundRect = (int)(g.getFontMetrics().stringWidth(displayName)*scaleFactor);
            int hBackgroundRect = (int) (hText*1.2);             
            for ( row_num = 0; row_num < hBackgroundRect; row_num++) {
                for ( col_num = 0; col_num < wBackgroundRect; col_num++) {
                    int index = row_num*nCols + col_num;
                    data[index] = (short)65535;
                }
            }

            
            // add text            
            int xText = (int)(g.getFontMetrics().stringWidth(displayName)*(scaleFactor-1)/2.0);
            int yText = hText; 
            g.setColor(Color.BLACK);            
            g.drawString(displayName, xText, yText);
            
            // store the images
            outputImages.put(String.valueOf(value), img);
        }  
        
        return outputImages;
    }    
    
    public Map<String,BufferedImage> drawImages_8bit() {
        int nRows = (Integer) config.get("nRows");
        int nCols = (Integer) config.get("nCols");
        int minValue = (Integer) config.get("minValue");
        int maxValue = (Integer) config.get("maxValue");
        int stepValue = (Integer) config.get("stepValue");
        
        Map<String,BufferedImage> outputImages = new TreeMap<>();
        
        for (int value = minValue; value <= maxValue; value += stepValue) {
            
            // create a new image and get the data array
            BufferedImage singleColorImg = new BufferedImage(nCols, nRows, BufferedImage.TYPE_INT_RGB);
 
            // create the graphics object
            Graphics2D g = (Graphics2D) singleColorImg.getGraphics();
            
            // draw a border
            g.setColor(new Color(255, 255, 255));  
            g.fillRect(0, 0, nCols, nRows);
            
            // draw the main area rectangle
            int borderWidth = 200;
            g.setColor(new Color(value, value, value));  
            g.fillRect(borderWidth, borderWidth, nCols-2*borderWidth, nRows-2*borderWidth);
            g.setColor(new Color(0, 0, 0)); 
            g.drawRect(borderWidth, borderWidth, nCols-2*borderWidth, nRows-2*borderWidth);
            
            // define display name string
            String displayName = String.format("%d", value);                      
            
            // set the font
            int hText = 100;
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, hText));
            
            // draw the font background rect
            int xBackgroundRect = 0;
            int yBackgroundRect = 0; 
            double scaleFactor = 1.2;
            int wBackgroundRect = (int)(g.getFontMetrics().stringWidth(displayName)*scaleFactor);
            int hBackgroundRect = (int) (hText*1.2); 
            g.setColor(Color.WHITE);
            g.fillRect(xBackgroundRect, yBackgroundRect, wBackgroundRect, hBackgroundRect);
            
            // add text            
            int xText = (int)(g.getFontMetrics().stringWidth(displayName)*(scaleFactor-1)/2.0);
            int yText = hText; 
            g.setColor(Color.BLACK);            
            g.drawString(displayName, xText, yText);
            
            // store the images
            outputImages.put(String.valueOf(value), singleColorImg);
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
        
        BrightnessCalibrationImageMaker16bit app = new BrightnessCalibrationImageMaker16bit(configAbsPath);
        
        // Draw the images
        println("Drawing Images");
        Map<String,BufferedImage> brightnessImages = app.drawImages_16bit();
        
        // Save the images
        println("Saving Images");
        app.saveImages(brightnessImages);
    }    
}