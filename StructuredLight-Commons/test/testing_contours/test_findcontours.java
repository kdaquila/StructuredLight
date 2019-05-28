package testing_contours;

import java.awt.image.BufferedImage;
import core.ImageUtil;
import core.Contours;
import java.util.List;

public class test_findcontours {

    public static void main(String[] args) {
       testing_findContours();
    }
    
    public static void testing_findContours()
    {
        System.out.println("Testing: findRegions()");
        
        String openFolder = "C:\\Users\\kfd18\\OneDrive\\kdaquila_SoftwareDev\\2018_12_13_CameraCalibrationImages\\Dataset1\\Extracted_Frames\\RGB";
        String openFilename = "9222.png";
        BufferedImage rgbImage = ImageUtil.load(openFolder, openFilename);
        BufferedImage grayImage = ImageUtil.color2Gray(rgbImage);
        BufferedImage bwImage = ImageUtil.adaptiveThreshold(grayImage, 21, 21);
        ImageUtil.fillBoundary(bwImage, 255);       
        
        String saveFolder = "C:\\Users\\kfd18\\Downloads";
        ImageUtil.save(bwImage, saveFolder, "bw_img.png");
        
        Contours myRegions = new Contours(bwImage);
        List<List<List<Integer>>> contours = myRegions.findContours();
        int nRegionsFound = contours.size();
        System.out.println("Found " + String.valueOf(nRegionsFound) + " regions");
    }

}
