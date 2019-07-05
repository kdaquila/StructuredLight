package lampstabilitycorrection;

import core.BufferedImageFactory;
import gui.ProgressBar;
import core.ImageStackUtils;
import core.ImageUtils;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FilenameUtils;

public class LampStabilityCorrection {
    
    int nRows;
    int nCols;
    int nSlices;
    
    public LampStabilityCorrection(SortedMap<String, int[][]> grayImageStack) {
        nCols = grayImageStack.values().iterator().next()[0].length;
        nRows = grayImageStack.values().iterator().next().length;
        nSlices = grayImageStack.size();
    }
    
    
    public int[] getAvgZProfile(SortedMap<String, int[][]> grayImageStack, int roiX, int roiY, int roiW, int roiH) {
        int[] measuredValues = new int[nSlices];
        List<String> imgNames = new ArrayList<>(grayImageStack.keySet());
        Collections.sort(imgNames, new Comparator<String>(){
            @Override
            public int compare(String s1, String s2) {
                int int1 = Integer.parseInt(s1.replaceAll("[^0-9]", ""));
                int int2 = Integer.parseInt(s2.replaceAll("[^0-9]", ""));
                return int1 - int2;
            }
            
        });
        for (int slice_num = 0; slice_num < nSlices; slice_num++) {
            int[][] grayImage = grayImageStack.get(imgNames.get(slice_num));
            
            double sum = 0.0;
            for (int y = roiY; y < (roiY + roiH); y++) {
                for (int x = roiX; x < (roiX + roiW); x++) {                    
                    sum += grayImage[y][x];
                }
            }
            int avgValue = (int)(sum/(roiW*roiH));
            measuredValues[slice_num] = avgValue;
        }
        return measuredValues;
    }
    
    public static void main(String[] args) {
        
        // Get the input directory
        String dirname;
        if (args.length >= 2) {
            throw new IllegalArgumentException("Must provide exactly 0-1 arguments. Found " + args.length);
        } else if (args.length == 1) {
            dirname = args[0];
        } else {
            JFileChooser f = new JFileChooser();
            f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            f.setCurrentDirectory(new File(System.getProperty("user.home")));
            int result = f.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                dirname = f.getSelectedFile().getAbsolutePath();
            } else {
                System.out.println("Ended by user");
                return;
            }
        }
        System.out.println("Loading images from: " + dirname);
        
        // Load the gray image stack
        SortedMap<String, int[][]> grayImageStack = ImageStackUtils.loadStack_8BitGray(dirname);
        
        LampStabilityCorrection lampStabilityCorrection = new LampStabilityCorrection(grayImageStack);
        
        // Get the average Z-axis profile at a location
        int nSlices = lampStabilityCorrection.nSlices;
        int nRows = lampStabilityCorrection.nRows;
        int nCols = lampStabilityCorrection.nCols;
        int roiW = 10;
        int roiH = 10;
        int roiX = nCols/2 - roiW/2;
        int roiY = nRows/10;        
        int[] zProfile = lampStabilityCorrection.getAvgZProfile(grayImageStack, roiX, roiY, roiW, roiH);
        
        // Compute the average Z-Profile value
        int sum = 0;
        for (int i = 0; i < zProfile.length; i++) {
            sum += zProfile[i];
        }
        double zProfile_mean = sum/zProfile.length;
        
        
        // Compute the rescale factors
        double[] scaleFactors = new double[zProfile.length];
        for (int i = 0; i < zProfile.length; i++) {
            scaleFactors[i] = zProfile_mean/zProfile[i];
        }
        
        // Sort the image names
        List<String> imgNames = new ArrayList<>(grayImageStack.keySet());
        Collections.sort(imgNames, new Comparator<String>(){
            @Override
            public int compare(String s1, String s2) {
                int int1 = Integer.parseInt(s1.replaceAll("[^0-9]", ""));
                int int2 = Integer.parseInt(s2.replaceAll("[^0-9]", ""));
                return int1 - int2;
            }
            
        });
        
        ProgressBar gui = new ProgressBar();
        gui.updateProgressBar(0);
        
        // Rescale each image in the stack
        SortedMap<String, int[][]> rescaledImageStack = new TreeMap<>();
        for (int slice_num = 0; slice_num < nSlices; slice_num++) {
            
            String imgName = imgNames.get(slice_num);                                            
            
            // update the progress bar
            int percentProgress = (int)(100.0*slice_num/nSlices);
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    gui.updateProgressBar(percentProgress);
                    gui.updateLabel("Rescaling " + imgName);
                }
            }); 
            
            double scaleFactor = scaleFactors[slice_num];
            int[][] grayImage = grayImageStack.get(imgName);
            int[][] rescaledImage = new int[nRows][nCols];
            for (int row_num = 0; row_num < nRows; row_num++) {
                for (int col_num = 0; col_num < nCols; col_num++) {
                    int rescaledValue = (int)(grayImage[row_num][col_num]*scaleFactor);
                    if (rescaledValue > 255) rescaledValue = 255;
                    if (rescaledValue < 0) rescaledValue = 0;
                    rescaledImage[row_num][col_num] = rescaledValue;
                }
            }
            rescaledImageStack.put(imgName, rescaledImage);
        }
        
        // Save the rescaled image stack
        String saveDirectory = dirname + "\\lampStabilized";
        for (int slice_num = 0; slice_num < nSlices; slice_num++) {
            String imgName = imgNames.get(slice_num);
            
            // update the progress bar
            int percentProgress = (int)(100.0*slice_num/nSlices);
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    gui.updateProgressBar(percentProgress);
                    gui.updateLabel("Saving " + imgName);
                    gui.pack();
                }
            }); 
            
            int[][] rescaledImage = rescaledImageStack.get(imgName);
            BufferedImage image = BufferedImageFactory.build_8bit_Gray(rescaledImage);
            String saveFilename = imgName + ".png";
            ImageUtils.save(image, saveDirectory, saveFilename);
        } 
        
        // Close the gui
        gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING));
        
        
        
    }
    
}
