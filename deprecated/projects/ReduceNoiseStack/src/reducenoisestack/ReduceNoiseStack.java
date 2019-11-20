package reducenoisestack;

import core.ImageStackUtils;
import core.ImageUtils;
import static core.Print.println;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.Map;

public class ReduceNoiseStack {
    
    Map<String,Object> config;  
    
    public ReduceNoiseStack(String configPath) {
        // Load the XML configuration file
        XML xml = new XML(configPath);        
        
        // Prepend the base directory to all relative sub directories
        xml.prependBaseDir();
        
        // Get the configuration data as a map
        config = xml.map;   
    }

    public static void main(String[] args) {
        println("Running the Reduce Noise Stack App:");

        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Needs exactly one argument which is the path to the XML configuration file.");
        } 

        // Parse the arguments
        String configAbsPath = args[0];
        ReduceNoiseStack app = new ReduceNoiseStack(configAbsPath);
        
        // Load the images
        String imagesDir = (String) app.config.get("imagesDir");
        Map<String,BufferedImage> imgStack = ImageUtils.load_batch(imagesDir);
        
        // Convert the stack to gray
        Map<String,BufferedImage> grayStack = ImageUtils.color2Gray_batch(imgStack);
        
        // Compute the mean image
        println("Computing the mean image:");
        BufferedImage meanImage = ImageStackUtils.meanStack(grayStack);
        
        // Save the mean image
        String reducedNoiseImageDir = (String) app.config.get("reducedNoiseImageDir");
        String reducedNoiseImageFilename = (String) app.config.get("reducedNoiseImageFilename");
        ImageUtils.save(meanImage, reducedNoiseImageDir, reducedNoiseImageFilename);
    }
    
}
