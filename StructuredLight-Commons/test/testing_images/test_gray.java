package testing_images;

import java.awt.image.BufferedImage;
import core.ImageUtils;

public class test_gray {

    public static void main(String[] args) {
        testing_gray();
    }

    public static void testing_gray()
    {
        System.out.println("Testing: Load,Gray,Save");
        
        // define file
        String load_folder = "F:\\kdaquila_SoftwareLibraries\\Java\\kdaquila\\StructuredLight-Commons\\Test_Resources";
        String load_filename = "Image1.png";
        
        // load the rgb
        BufferedImage rgbImage = ImageUtils.load(load_folder, load_filename);
        
        // convert to gray
        BufferedImage grayImage = ImageUtils.color2Gray(rgbImage);
        
        // save the gray image
        String save_folder = "F:\\kdaquila_SoftwareLibraries\\Java\\kdaquila\\StructuredLight-Commons\\Test_Resources\\trash";
        String save_filename = "Image1_gray.png";
        ImageUtils.save(grayImage, save_folder, save_filename);
        
        System.out.println("\n");
    }
    
    
}
