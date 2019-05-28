package apps;

import calibrationpattern.rings.ImagePoints;
import core.ImageUtil;
import core.TXT;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.List;

public class FindRingsApp {
    
    public static void main(String[] args) {
        
        // Validate arguments
        if (args.length == 0) {
            throw new IllegalArgumentException("Please provide one argument which is the path to the XML configuration file");
        } 
        
        // Parse the arguments
        String configPath = args[0];
        
        // Load the configuration variables
        System.out.print("Loading the configuration ... ");
        
        XML conf = new XML(configPath);      
        int nRows = conf.getInt("/config/nRows");
        int nCols = conf.getInt("/config/nCols");
        int borderWidth = conf.getInt("/config/borderWidth");
        int shapeRadius = conf.getInt("/config/shapeRadius");
        int backgroundColor = conf.getInt("/config/backgroundColor", 16);        
        int borderColor = conf.getInt("/config/borderColor", 16);        
        int ringColor = conf.getInt("/config/ringColor", 16);
        int ringOuterRadius = conf.getInt("/config/ringOuterRadius");
        int ringInnerRadius = conf.getInt("/config/ringInnerRadius");
        String savePath = conf.getString("/config/savePath"); 
        
        System.out.println("Done");
        
        // Load the image        
        String rgbImgPath = ".\\Test_Resources\\RingGrid_Images\\2300.png";
        BufferedImage rgbImage = ImageUtil.load(rgbImgPath);
        
        // Find the rings centers       
        int nRings = 221;
        List<List<Double>> ringCenters = ImagePoints.find(rgbImage, nRings);
        
        // Save the point data
        String saveDataPath = ".\\Test_Resources\\RingGrid_Points\\imagePoints.txt";
        String formatString = "%.3f";
        TXT.saveMatrix(ringCenters, Double.class, saveDataPath, formatString);
    }

}
