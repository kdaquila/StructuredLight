package apps;

import core.ArrayUtils;
import core.Draw;
import core.ImageUtil;
import core.PNG;
import core.TXT;
import core.XML;
import java.awt.image.BufferedImage;
import java.util.List;

public class DrawPointsApp {
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
        int nRings = conf.getInt("/config/nRings");
        String formatString = conf.getString("/config/formatString"); 
        String rgbImagePath = conf.getString("/config/rgbImagePath"); 
        String loadDataPath = conf.getString("/config/loadDataPath"); 
        
        System.out.println("Done");
        
        // Load the image        
        BufferedImage rgbImage = ImageUtil.load(rgbImagePath);
        
        // Load the pointss
        String delimiter = ",";
        String EOL = "\n";
        List<List<Double>> contours = TXT.loadMatrix(loadDataPath, Double.class, delimiter, EOL);
        
        // Convert Double to Int
        List<List<Integer>> contoursINT = ArrayUtils.castArrayDouble_To_Integer(contours);
        
        // Draw the image
        int radius = 3;
        int lineWidth = 1;
        int colorHex = 0xFF0000;
        boolean isFill = true;
        BufferedImage drawing = Draw.drawCircles(rgbImage, contoursINT, radius, lineWidth, colorHex, isFill);
        
        // Save the image
        String drawImgSavePath = ".\\Test_Resources\\Debug\\drawing.png";
        PNG png = new PNG();
        png.save(drawing, drawImgSavePath);
        
    }
}
