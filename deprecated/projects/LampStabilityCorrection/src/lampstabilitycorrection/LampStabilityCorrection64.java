package lampstabilitycorrection;

import gui.ProgressBar;
import core.FITS;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

public class LampStabilityCorrection64 {
    
    int nRows;
    int nCols;
    int nSlices;
    
    public LampStabilityCorrection64(Map<String, double[][]> grayImageStack) {
        nCols = grayImageStack.values().iterator().next()[0].length;
        nRows = grayImageStack.values().iterator().next().length;
        nSlices = grayImageStack.size();
    }
    
    
    public double[] getAvgZProfile(Map<String, double[][]> grayImageStack, int roiX, int roiY, int roiW, int roiH) {
        double[] measuredValues = new double[nSlices];
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
            double[][] grayImage = grayImageStack.get(imgNames.get(slice_num));
            
            double sum = 0.0;
            for (int y = roiY; y < (roiY + roiH); y++) {
                for (int x = roiX; x < (roiX + roiW); x++) {                    
                    sum += grayImage[y][x];
                }
            }
            double avgValue = sum/(roiW*roiH);
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
        Map<String, double[][]> grayImageStack = FITS.loadArray_Batch(dirname);
        
        LampStabilityCorrection64 lampStabilityCorrection64 = new LampStabilityCorrection64(grayImageStack);
        
        // Get the average Z-axis profile at a location
        int nSlices = lampStabilityCorrection64.nSlices;
        int nRows = lampStabilityCorrection64.nRows;
        int nCols = lampStabilityCorrection64.nCols;
        int roiW = 10;
        int roiH = 10;
        int roiX = nCols/2 - roiW/2;
        int roiY = nRows/10;
        
        double[] zProfile = lampStabilityCorrection64.getAvgZProfile(grayImageStack, roiX, roiY, roiW, roiH);
        
        // Compute the rescale factors
        double[] scaleFactors = new double[zProfile.length];
        for (int i = 0; i < zProfile.length; i++) {
            scaleFactors[i] = zProfile[0]/zProfile[i];
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
        Map<String, Object> rescaledImageStack = new HashMap<>();
        for (int slice_num = 0; slice_num < nSlices; slice_num++) {
            
            String imgName = imgNames.get(slice_num);
            
            // update the progress bar
            int percentProgress = (int)(100.0*slice_num/nSlices);
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    gui.updateProgressBar(percentProgress);
                    gui.updateLabel(imgName);
                }
            });                       
            
            
            double scaleFactor = scaleFactors[slice_num];
            double[][] grayImage = grayImageStack.get(imgName);
            double[][] rescaledImage = new double[nRows][nCols];
            for (int row_num = 0; row_num < nRows; row_num++) {
                for (int col_num = 0; col_num < nCols; col_num++) {
                    rescaledImage[row_num][col_num] = grayImage[row_num][col_num]*scaleFactor;
                }
            }
            rescaledImageStack.put(imgName, rescaledImage);
        }
        
        // Save the rescaled image stack
        String saveDirectory = dirname + "\\lampStabilzed";
        FITS.saveImageBatch(rescaledImageStack, saveDirectory);
        
        // Close the gui
        gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING));
        
        
        
    }
    
}
