package core;

import java.awt.image.BufferedImage;
import java.util.List;

public class DrawPointsApp {
    public static void main(String[] args) {
        // Load the image        
        String rgbImgPath = ".\\Test_Resources\\RingGrid_Images\\2300.png";
        BufferedImage rgbImage = ImageUtil.load(rgbImgPath);
        
        // Load the points
        String loadDataPath = ".\\Test_Resources\\RingGrid_Points\\imagePoints.txt";
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
        int dpi = 300;
        png.setDPI(dpi);
        png.save(drawing, drawImgSavePath);
        
    }
}
