package testing_contours;

import java.awt.image.BufferedImage;
import core.ImageUtils;
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
        BufferedImage rgbImage = ImageUtils.load(openFolder, openFilename);
        BufferedImage grayImage = ImageUtils.color2Gray(rgbImage);
        BufferedImage bwImage = ImageUtils.adaptiveThreshold(grayImage, 21, 21);
        ImageUtils.fillBoundary(bwImage, 255);       
        
        String saveFolder = "C:\\Users\\kfd18\\Downloads";
        ImageUtils.save(bwImage, saveFolder, "bw_img.png");
        
        Contours myRegions = new Contours(bwImage);
        List<List<List<Integer>>> contours = myRegions.findContours();
        int nRegionsFound = contours.size();
        System.out.println("Found " + String.valueOf(nRegionsFound) + " regions");
    }

}
