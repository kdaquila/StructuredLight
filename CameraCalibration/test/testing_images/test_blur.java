package testing_images;

import java.awt.image.BufferedImage;
import java.util.List;
import core.ImageUtils;

public class test_blur {

    public static void main(String[] args) {
        testing_blur();
    }

    public static void testing_blur()
    {
        System.out.println("Testing: Blur");
        
        // define file
        String load_folder = "F:\\kdaquila_SoftwareLibraries\\Java\\kdaquila\\StructuredLight-Commons\\Test_Resources";
        String load_filename = "Image3_gray.png";
        
        // load the gray
        BufferedImage grayImage = ImageUtils.load(load_folder, load_filename);
        
        // do blur
        BufferedImage blurImage =ImageUtils.meanFilter(grayImage, 1);
                       
        // save the blur image
        String save_folder = "F:\\kdaquila_SoftwareLibraries\\Java\\kdaquila\\StructuredLight-Commons\\Test_Resources\\trash";
        String save_filename = "Image3_blur.png";
        ImageUtils.save(blurImage, save_folder, save_filename);
        
        System.out.println("\n");
    }
}
