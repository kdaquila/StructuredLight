package calibrationpattern.rings;

import core.ImageUtil;
import core.TXT;
import java.awt.image.BufferedImage;
import java.util.List;

public class ImagePointsApp {
    
    public static void main(String[] args) {
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
