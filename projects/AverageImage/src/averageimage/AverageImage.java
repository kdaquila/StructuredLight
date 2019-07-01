package averageimage;

import core.FITS;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

public class AverageImage {
    
    public static double[][] computeAverageImage(Map<String, double[][]> grayImageStack) {
        // get stack dimensions
        int nCols = grayImageStack.values().iterator().next()[0].length;
        int nRows = grayImageStack.values().iterator().next().length;
        int nSlices = grayImageStack.size();
        
        // getthe image names
        List<String> imgNames = new ArrayList<>(grayImageStack.keySet());
        Collections.sort(imgNames, new Comparator<String>(){
            @Override
            public int compare(String s1, String s2) {
                int int1 = Integer.parseInt(s1.replaceAll("[^0-9]", ""));
                int int2 = Integer.parseInt(s2.replaceAll("[^0-9]", ""));
                return int1 - int2;
            }
            
        });
        
//        // average the images
//        double[][] averageImage = new double[nRows][nCols];
//        double avgFactor = 1.0/nSlices;
//        for (int slice_num = 0; slice_num < nSlices; slice_num++) {
//            double[][] grayImage = grayImageStack.get(imgNames.get(slice_num));            
//            for (int row_num = 0; row_num < nRows; row_num++) {
//                for (int col_num = 0; col_num < nCols; col_num++) {                    
//                    averageImage[row_num][col_num] += (grayImage[row_num][col_num]*avgFactor);
//                }
//            }
//        }
        
//        // average the images
//        double[][] averageImage = new double[nRows][nCols];  
//        for (int row_num = 0; row_num < nRows; row_num++) {
//            for (int col_num = 0; col_num < nCols; col_num++) { 
//                double sum = 0.0;
//                for (int slice_num = 0; slice_num < nSlices; slice_num++) {
//                    double[][] grayImage = grayImageStack.get(imgNames.get(slice_num));
//                    double grayValue = grayImage[row_num][col_num];
//                    sum += grayValue;
//                }
//                averageImage[row_num][col_num] = sum/nSlices;
//            }
//        }

        // median filter the images
        double[][] averageImage = new double[nRows][nCols];  
        for (int row_num = 0; row_num < nRows; row_num++) {
            for (int col_num = 0; col_num < nCols; col_num++) { 
                List<Double> zProfile = new ArrayList<>();
                for (int slice_num = 0; slice_num < nSlices; slice_num++) {
                    double[][] grayImage = grayImageStack.get(imgNames.get(slice_num));
                    double grayValue = grayImage[row_num][col_num];
                    zProfile.add(grayValue);
                }
                Collections.sort(zProfile);
                averageImage[row_num][col_num] = zProfile.get(nSlices/2);
            }
        }
        
        
        return averageImage;
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
                
        // Get Sub Directories
        File directory = new File(dirname);
        String[] subDirs = directory.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
              return new File(current, name).isDirectory();
            }
          });
        
        GUI gui = new GUI();
        gui.updateProgressBar(0);
        
        for (int i = 0; i < subDirs.length; i++) {  
            
            String subDirName = subDirs[i];
            
            System.out.println("Loading images from: " + subDirName);
            
            // update the progress bar
            int percentProgress = (int)(100.0*i/subDirs.length);
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    gui.updateProgressBar(percentProgress);
                    gui.updateLabel(subDirName);
                }
            });  
            
            // Load the gray image stack
            Map<String, double[][]> grayImageStack = FITS.loadArray_Batch(dirname + "\\" + subDirName);

            // Compute the average image
            double[][] averageImage = computeAverageImage(grayImageStack);
            
            // Save the average image
            String saveFolder = dirname + "\\averageImage";
            String saveFileName = subDirName + ".FITS";
            FITS.saveImage(averageImage, saveFolder, saveFileName);
        }
        
        // Close the gui
        gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING));
        
        
        
    }
    
}
