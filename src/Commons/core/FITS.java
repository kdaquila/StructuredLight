package core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import org.apache.commons.io.FilenameUtils;

public class FITS {
    
    public static <T> Map<String, T> loadArray_Batch(String folderName) {
        // Find the image filenames
        String[] fileNames = (new File(folderName)).list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                String lowerName = name.toLowerCase();
                boolean isValid = lowerName.endsWith(".fits"); 
                return isValid;
            }
        });
        
        // Validate file names
        if (fileNames == null || fileNames.length == 0) {
            throw new RuntimeException("No suitables files found in the input directory.");
        }
        
        Map<String, T> arrayStack = new HashMap<>();
        for (String fileName: fileNames) {
            Print.println("Now loading image " + fileName);
            
            // Load the array
            String imageAbsPath = folderName + "\\" + fileName;
            T array = loadArray(imageAbsPath);
            
            // Store the array
            String baseFilename = FilenameUtils.removeExtension(fileName);
            arrayStack.put(baseFilename, array);
        }
        return arrayStack;
    }
    
    public static <T> T loadArray(String path) {
        T data;
        try {
            Fits f = new Fits(path);
            data = (T) f.getHDU(0).getKernel();
        } catch (FitsException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }       
        return data;
    }
    
    public static Map<String, double[][]> loadDoubleArray2D_Batch(String folderName) {
        // Find the image filenames
        String[] fileNames = (new File(folderName)).list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                String lowerName = name.toLowerCase();
                boolean isValid = lowerName.endsWith(".fits"); 
                return isValid;
            }
        });
        
        // Validate file names
        if (fileNames == null || fileNames.length == 0) {
            throw new RuntimeException("No suitables files found in the input directory.");
        }
        
        Map<String, double[][]> arrayStack = new HashMap<>();
        for (String fileName: fileNames) {
            Print.println("Now loading image " + fileName);
            
            // Load the array
            String imageAbsPath = folderName + "\\" + fileName;
            double[][] array = loadDoubleArray2D(imageAbsPath);
            
            // Store the array
            String baseFilename = FilenameUtils.removeExtension(fileName);
            arrayStack.put(baseFilename, array);
        }
        return arrayStack;
    }
    
    public static double[][] loadDoubleArray2D(String path) {
        double[][] data;
        try {
            Fits f = new Fits(path);
            data = (double[][]) f.getHDU(0).getKernel();
        } catch (FitsException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }       
        return data;
    }
    
    public static Map<String, double[][][]> loadDoubleArray3D_Batch(String folderName) {
        
        return new HashMap<>();
    }
    
    public static double[][][] loadDoubleArray3D(String path) {
        double[][][] data;
        try {
            Fits f = new Fits(path);
            data = (double[][][]) f.getHDU(0).getKernel();
        } catch (FitsException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }       
        return data;
    }

    public static void saveImageBatch(Map<String, Object> arrayStack, String folderName) {
        
    }
    
    public static void saveImage(Object array, String folderName, String fileName) {
        
    }
}
