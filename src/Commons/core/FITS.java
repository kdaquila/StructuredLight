package core;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.util.BufferedFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class FITS {
    
    public static <T> SortedMap<String, T> loadArray_Batch(String folderName) {
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
        
        SortedMap<String, T> arrayStack = new TreeMap<>();
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
        for (String name: arrayStack.keySet()) {
            Object array = arrayStack.get(name);
            String fileName = name + ".FITS";
            saveImage(array, folderName, fileName);
        }
    }
    
    public static void saveImage(Object array, String folderName, String fileName) {
        // Create the FITS data structure
        Fits f = new Fits();
        try {
            f.addHDU(FitsFactory.HDUFactory(array));
        } catch (FitsException e) {
            throw new RuntimeException(e.getMessage());
        }

        // Create the folder if necessary
        try {
            FileUtils.forceMkdir(new File(folderName));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        // Save the FITS structure
        try {
            BufferedFile bf = new BufferedFile(folderName + "\\" + fileName, "rw");
            f.write(bf);
            bf.close();
        } catch (FitsException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
