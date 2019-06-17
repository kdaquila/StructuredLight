package apps;

import core.ArrayUtils;
import core.Draw;
import core.ImageUtils;
import core.PNG;
import core.TXT;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.List;

public class DrawPointsApp {
    public static void main(String[] args) {
        System.out.println("Running the DrawPointsApp:");
        
        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Please provide one argument which is the path to the XML configuration file");
        } 
        
        // Parse the arguments
        String configPath = args[0];
        
        // Load the configuration variables
        System.out.print("Loading the configuration ... ");
        
        XML conf = new XML(configPath);      
        int circleRadius = conf.getInt("/config/circleRadius");
        int circleLineWidth = conf.getInt("/config/circleLineWidth");
        int circleLineColor = conf.getInt("/config/circleLineColor", 16);
        boolean circleIsFilled = conf.getBool("/config/circleIsFilled");
        String rgbImagePath = conf.getString("/config/rgbImagePath"); 
        String loadDataPath = conf.getString("/config/loadDataPath"); 
        String saveImagePath = conf.getString("/config/saveImagePath"); 
        
        System.out.println("Done");
        
        // Load the image

        System.out.print("Drawing on the RGB image ... ");  
        
        BufferedImage rgbImage = ImageUtils.load(rgbImagePath);
        
        // Load the points
        List<List<Double>> contours = TXT.loadMatrix(loadDataPath, Double.class);
        
        // Convert Double to Int
        List<List<Integer>> contoursINT = ArrayUtils.castArrayDouble_To_Integer(contours);
        
        // Draw the image
        BufferedImage drawing = Draw.drawCircles(rgbImage, contoursINT, 
                                                 circleRadius, circleLineWidth, 
                                                 circleLineColor, circleIsFilled);
        
        System.out.println("Done");
        
        // Save the image        
        System.out.print("Loading the RGB image ... "); 
        
        PNG png = new PNG();
        png.save(drawing, saveImagePath);
        
        System.out.println("Done");
        
    }
}
