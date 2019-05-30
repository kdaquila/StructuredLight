package testing_images;

import java.awt.image.BufferedImage;
import core.ImageUtils;

public class test_adaptiveThreshold {

    public static void main(String[] args) {
        testing_AdaptiveThresh();
    }
    
    public static void testing_AdaptiveThresh()
    {
        System.out.println("Testing: AdaptiveThresh");
        
        // define file
        String load_folder = "F:\\kdaquila_SoftwareLibraries\\Java\\kdaquila\\StructuredLight-Commons\\Test_Resources";
        String load_filename = "Image4.png";
        
        // load the rgb
        BufferedImage rgbImage = ImageUtils.load(load_folder, load_filename);
        
        // convert to gray
        BufferedImage grayImage = ImageUtils.color2Gray(rgbImage);
        
        ImageUtils.fillBoundary(grayImage, 0);
        
        // save the bw image
        String save_folder = "F:\\kdaquila_SoftwareLibraries\\Java\\kdaquila\\StructuredLight-Commons\\Test_Resources\\trash";
        ImageUtils.save(grayImage, save_folder, "Image4_gray.png");
        
        // adaptive threshold
        BufferedImage bwImage = ImageUtils.adaptiveThreshold(grayImage, 21, 21);      
                
        // save the bw image
        ImageUtils.save(bwImage, save_folder, "Image4_bw.png");
        
        System.out.println("\n");
    }

}
