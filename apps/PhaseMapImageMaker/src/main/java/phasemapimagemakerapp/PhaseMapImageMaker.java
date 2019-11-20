package phasemapimagemakerapp;

import core.BufferedImageFactory;
import core.ImageUtils;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.Map;
import static core.Print.println;
import core.TXT;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.DataBufferUShort;
import java.util.SortedMap;
import java.util.TreeMap;
import lookuptable.LookUpTable;
import sinewavepattern.SineWavePattern;

public class PhaseMapImageMaker {
    
    Map<String,Object> config;  
    
    public PhaseMapImageMaker(String configPath) {
        // Load the XML configuration file
        XML xml = new XML(configPath);        
        
        // Prepend the base directory to all relative sub directories
        xml.prependBaseDir();
        
        // Get the configuration data as a map
        config = xml.map;   
    }
    
    public void makeImagesTwoAxisTwoFreq() {
        double horizontalWaveLength1 = (Double) config.get("horizontalWaveLength1");
        SortedMap<String,BufferedImage> horizontalWaveLength1Stack = makeImages(horizontalWaveLength1, "horizontal");
        saveImages(horizontalWaveLength1Stack);
        
        double horizontalWaveLength2 = (Double) config.get("horizontalWaveLength2");
        SortedMap<String,BufferedImage> horizontalWaveLength2Stack = makeImages(horizontalWaveLength2, "horizontal");
        saveImages(horizontalWaveLength2Stack);
        
        double verticalWaveLength1 = (Double) config.get("verticalWaveLength1");
        SortedMap<String,BufferedImage> verticalWaveLength1Stack = makeImages(verticalWaveLength1, "vertical");
        saveImages(verticalWaveLength1Stack);
        
        double verticalWaveLength2 = (Double) config.get("verticalWaveLength2");
        SortedMap<String,BufferedImage> verticalWaveLength2Stack = makeImages(verticalWaveLength2, "vertical");
        saveImages(verticalWaveLength2Stack);
    }
        
    private SortedMap<String, BufferedImage> makeImages(double waveLength, String orientation) {
        // Compute the sine wave arrays
        int nRows = (Integer) config.get("nRows");
        int nCols = (Integer) config.get("nCols");
        int amplitude = (Integer) config.get("amplitude");
        int offset = (Integer) config.get("offset");        
        int nPhaseSteps = (Integer) config.get("nPhaseSteps");                 
        SortedMap<String,int[][]> sineWaveArrayStack = SineWavePattern.makeSineArrayStack(nRows, nCols, amplitude, offset, waveLength, nPhaseSteps, orientation);
        
        // modify the images with white border and label
        SortedMap<String,int[][]> modifiedSineWaveArrayStack = new TreeMap<>();
        int borderWidth = (Integer) config.get("borderWidth");
        for (Map.Entry<String, int[][]> entry: sineWaveArrayStack.entrySet()) {
            String imgName = entry.getKey();
            int[][] data = entry.getValue();
            
            // draw the border regions
            for (int row_num = 0; row_num < nRows ; row_num++) {
                for (int col_num = 0; col_num < borderWidth; col_num++) {
                    data[row_num][col_num] = offset + amplitude;
                }
            }
            for (int row_num = 0; row_num < nRows ; row_num++) {
                for (int col_num = nCols-borderWidth; col_num < nCols; col_num++) {
                    int index = row_num*nCols + col_num;
                    data[row_num][col_num] = offset + amplitude;
                }
            }
            for (int row_num = 0; row_num < borderWidth ; row_num++) {
                for (int col_num = 0; col_num < nCols; col_num++) {
                    data[row_num][col_num] = offset + amplitude;
                }
            }
            for (int row_num = nRows - borderWidth; row_num < nRows ; row_num++) {
                for (int col_num = 0; col_num < nCols; col_num++) {
                    data[row_num][col_num] = offset + amplitude;
                }
            }
                        
            // draw the border around the main area rectangle
            int row_num = borderWidth;
            for (int col_num = borderWidth; col_num < nCols - borderWidth; col_num++) {
                data[row_num][col_num] = offset - amplitude;
            }
            row_num = nRows - borderWidth;
            for (int col_num = borderWidth; col_num < nCols - borderWidth; col_num++) {
                data[row_num][col_num] = offset - amplitude;
            }
            int col_num = borderWidth;
            for (row_num = borderWidth; row_num < nRows - borderWidth; row_num++) {
                data[row_num][col_num] = offset - amplitude;
            }
            col_num = nCols - borderWidth;
            for (row_num = borderWidth; row_num < nRows - borderWidth; row_num++) {
                data[row_num][col_num] = offset - amplitude;
            }
            
            modifiedSineWaveArrayStack.put(imgName, data);
        }
        
        // Apply the brightness calibration
        modifiedSineWaveArrayStack = applyBrightnessCalibration(modifiedSineWaveArrayStack);
    
        // Set as images      
        SortedMap<String,BufferedImage> sineWaveImages = new TreeMap<>();
        for (String name: modifiedSineWaveArrayStack.keySet()) {
            int[][] array = modifiedSineWaveArrayStack.get(name);
            BufferedImage image = BufferedImageFactory.build_16bit_Gray(array);
            sineWaveImages.put(name, image);
        }    
        
        
        // Add text overlay
        int fontSize = (Integer) config.get("fontSize");
        for (String name: sineWaveImages.keySet()) {
            BufferedImage image = sineWaveImages.get(name);
            Graphics2D g = image.createGraphics();
            short[] data = ((DataBufferUShort)image.getRaster().getDataBuffer()).getData();
            
            // define display name string
            String displayName = name;                      
            
            // set the font            
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
            
            // draw the font background rect
            double scaleFactor = 1.2;
            int wBackgroundRect = (int)(g.getFontMetrics().stringWidth(displayName)*scaleFactor);
            int hBackgroundRect = (int) (fontSize*1.2);             
            for (int row_num = 0; row_num < hBackgroundRect; row_num++) {
                for (int col_num = 0; col_num < wBackgroundRect; col_num++) {
                    int index = row_num*nCols + col_num;
                    data[index] = (short)(offset + amplitude);
                }
            }
            
            // add text            
            int xText = (int)(g.getFontMetrics().stringWidth(displayName)*(scaleFactor-1)/2.0);
            int yText = fontSize; 
            g.setColor(Color.BLACK);            
            g.drawString(displayName, xText, yText);
        }
        
        // Add an all-white image        
        BufferedImage allWhiteImg = new BufferedImage(nCols, nRows, BufferedImage.TYPE_USHORT_GRAY);
        short[] data = ((DataBufferUShort)allWhiteImg.getRaster().getDataBuffer()).getData();
        Graphics2D g = (Graphics2D) allWhiteImg.getGraphics();
        for (int row_num = 0; row_num < nRows ; row_num++) {
            for (int col_num = 0; col_num < nCols; col_num++) {
                int index = row_num*nCols + col_num;
                data[index] = (short)(offset + amplitude);
            }
        }
        sineWaveImages.put("white", allWhiteImg);
          
        return sineWaveImages;
    }
    
    private SortedMap<String,int[][]> applyBrightnessCalibration(SortedMap<String,int[][]> inputStack) {
        // Apply the look-up-table
        boolean doBrightnessCalibration = (Boolean) config.get("doBrightnessCalibration");        
        if (doBrightnessCalibration) {
            SortedMap<String,int[][]> sineWaveArrayStack_Adj = new TreeMap<>();
            String brightnessCalibrationTableDir = (String) config.get("brightnessCalibrationTableDir");
            String brightnessCalibrationTableFilename = (String) config.get("brightnessCalibrationTableFilename");
            String brightnessCalibrationFullPath = brightnessCalibrationTableDir + "\\" + brightnessCalibrationTableFilename;
            double[] params = TXT.loadVector_Double(brightnessCalibrationFullPath); 
            for (String name: inputStack.keySet()) {
                int[][] oldImg = inputStack.get(name);
                int[][] newImg = LookUpTable.applyParams(oldImg, params);
                sineWaveArrayStack_Adj.put(name, newImg);
            }                 
            return sineWaveArrayStack_Adj;
            
        } else {
            return inputStack;
        }
    }
    
    private void saveImages(SortedMap<String,BufferedImage> sineWaveImages) {
        
        // Save the images        
        String sineWavePatternsDir = (String) config.get("sineWavePatternsDir");
        println("Saving the Images to: " + sineWavePatternsDir);
        ImageUtils.save_batch(sineWaveImages, sineWavePatternsDir);
    }
    
    public Map<String,BufferedImage> makeImages_withLookUpFunction() {
        // Compute the sine wave arrays
        int nRows = (Integer) config.get("nRows");
        int nCols = (Integer) config.get("nCols");
        int amplitude = (Integer) config.get("amplitude");
        int offset = (Integer) config.get("offset");
        int nPhaseSteps = (Integer) config.get("nPhaseSteps");
        String orientation = (String) config.get("waveDirection");  
        String brightnessCalibrationTableDir = (String) config.get("brightnessCalibrationTableDir");
        String brightnessCalibrationTableFilename = (String) config.get("brightnessCalibrationTableFilename");
        String brightnessCalibrationFullPath = brightnessCalibrationTableDir + "\\" + brightnessCalibrationTableFilename;
        double[] params = TXT.loadVector_Double(brightnessCalibrationFullPath);        
        double waveLength1 = (Double) config.get("waveLength1");                       
        double waveLength2 = (Double) config.get("waveLength2");
        double[] waveLenghts = new double[]{waveLength1, waveLength2};
        Map<String,BufferedImage> sineWaveImages = new TreeMap<>();
        for (double wavelength: waveLenghts) {
            Map<String,int[][]> sineWaveArrayStack = SineWavePattern.makeSineArrayStack(nRows, nCols, amplitude, offset, wavelength, nPhaseSteps, orientation);
        
            // Apply the look-up-table
            boolean doBrightnessCalibration = (Boolean) config.get("doBrightnessCalibration");        
            if (doBrightnessCalibration) {
                Map<String,int[][]> sineWaveArrayStack_Adj = new TreeMap<>();
                for (String name: sineWaveArrayStack.keySet()) {
                    int[][] oldImg = sineWaveArrayStack.get(name);
                    int[][] newImg = LookUpTable.applyParams(oldImg, params);
                    sineWaveArrayStack_Adj.put(name, newImg);
                }     

                // Overwrite the original image stack with the adjusted one
                sineWaveArrayStack = sineWaveArrayStack_Adj;
            }        

            // Set as images 
            for (String name: sineWaveArrayStack.keySet()) {
                int[][] array = sineWaveArrayStack.get(name);
                BufferedImage image = BufferedImageFactory.build_16bit_Gray(array);
                sineWaveImages.put(name, image);
            }    
        }
        return sineWaveImages;
    }
    
    public static void main(String[] args) {
               
        println("Running the Sine Wave Image Maker App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];
        
        // Create the app
        PhaseMapImageMaker app = new PhaseMapImageMaker(configAbsPath);
        
        // Create and Save the images
        println("Creating the Images");
        app.makeImagesTwoAxisTwoFreq();
        
        
    }    
    
}