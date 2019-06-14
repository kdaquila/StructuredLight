package apps;

import core.ArrayUtils;
import core.LaplacianFilter;
import core.ImageUtils;
import core.TXT;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class LaplacianFilterApp {

    public static void main(String[] args) {
        System.out.println("Running the LaplacianFilterApp:");
        
        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Please provide one argument which is the path to the XML configuration file");
        } 

        // Parse the arguments
        String configPath = args[0];

        // Load the configuration variables
        System.out.print("Loading the configuration ... ");

        XML conf = new XML(configPath);      
        String rgbImagePath = conf.getString("/config/rgbImagePath"); 
        String saveImagePath = conf.getString("/config/saveImagePath"); 
        String saveKernalPath = conf.getString("/config/saveKernalPath");
        String kernalFormatStr = conf.getString("/config/kernalFormatStr");
        String delimiter = conf.getString("/config/delimiter");
        String EOL = conf.getString("/config/EOL");
        boolean append = conf.getBool("/config/append");
        int kernalSize = conf.getInt("/config/kernalSize");
        double kernalSigma = conf.getDouble("/config/kernalSigma");
        System.out.println("Done");
        
        // Create the Laplacian Kernal
        
        System.out.print("Creating the kernal ... ");
        
        float[] kernalArray = LaplacianFilter.createLaplacianKernal(kernalSigma, kernalSize);
        
        System.out.println("Done");
        
        // Save the Laplacian Kernal
        List<Float> kernalList = ArrayUtils.ArrayToList_Float(kernalArray);
        List<List<Float>> temp = new ArrayList<>();
        temp.add(kernalList);
        List<List<Float>> kernalMatrix = ArrayUtils.reshape(temp, kernalSize, kernalSize);
        TXT.saveMatrix(kernalMatrix, Float.class, saveKernalPath, kernalFormatStr, delimiter, EOL, append);
        
        // Load the input image
        System.out.print("Loading the image ... ");
        BufferedImage inputRGBImage = ImageUtils.load(rgbImagePath);
        BufferedImage grayImage = ImageUtils.color2Gray(inputRGBImage);
        System.out.println("Done");
        
        // Run the filter
        System.out.print("Filtering the image ... ");
        BufferedImage outputImage = ImageUtils.convolve(grayImage, kernalSize, kernalArray);
        System.out.println("Done");
        
        // Save the output image
        System.out.print("Saving the image ... ");
        ImageUtils.save(outputImage, saveImagePath);
        System.out.println("Done");
        
    }
}
