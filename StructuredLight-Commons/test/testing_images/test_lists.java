package testing_images;

import java.awt.image.BufferedImage;
import java.util.List;
import core.ImageUtil;

public class test_lists {

    public static void main(String[] args) {        
        testing_ColorImageToList();
        testing_GrayImageToList();
        testing_ListToColorImage();
        testing_ListToGrayImage();
    }

    public static void testing_ColorImageToList()
    {
        System.out.println("Testing: ColorImageToList");
        
        // define file
        String load_folder = "F:\\kdaquila_SoftwareLibraries\\Java\\kdaquila\\StructuredLight-Commons\\Test_Resources";
        String load_filename = "Image1.png";
        
        // load the rgb
        BufferedImage rgbImage = ImageUtil.load(load_folder, load_filename);
        
        System.out.println(ImageUtil.ColorImageToList(rgbImage));
        
        System.out.println("\n");
    }
    
    public static void testing_GrayImageToList()
    {
        System.out.println("Testing: GrayImageToList");
        
        // define file
        String load_folder = "F:\\kdaquila_SoftwareLibraries\\Java\\kdaquila\\StructuredLight-Commons\\Test_Resources";
        String load_filename = "Image1_gray.png";
        
        // load the gray
        BufferedImage grayImage = ImageUtil.load(load_folder, load_filename);
        
        System.out.println(ImageUtil.GrayImageToList(grayImage));
        
        System.out.println("\n");
    }
    
    public static void testing_ListToColorImage()
    {
        System.out.println("Testing: ListToColorImage");
        
        // define file
        String load_folder = "F:\\kdaquila_SoftwareLibraries\\Java\\kdaquila\\StructuredLight-Commons\\Test_Resources";
        String load_filename = "Image1.png";
        
        // load the rgb
        BufferedImage rgbImage = ImageUtil.load(load_folder, load_filename);
        
        // get the list
        List<List<List<Double>>> rgbList =ImageUtil.ColorImageToList(rgbImage);
        
        // modify the list
        int nCols = rgbImage.getWidth();
        int nRows = rgbImage.getHeight();
        int nCh = 3;
        
        for (int row_num = 0; row_num < nRows; row_num++)
        {
            for (int col_num = 0; col_num < nCols; col_num++)
            {
                for (int ch_num = 0; ch_num < nCh; ch_num++)
                {
                    if (row_num > 0)
                    {
                        rgbList.get(row_num).get(col_num).set(ch_num, 0.0);
                    }
                    
                    if ((row_num == 6 || col_num == 6) && ch_num == 0)
                    {
                        rgbList.get(row_num).get(col_num).set(ch_num, 255.0);
                    }
                }
            }
        }
        
        // convert list to image
        BufferedImage modifiedRGBImage = ImageUtil.ListToColorImage(rgbList);
        
        // save the color image
        String save_folder = "F:\\kdaquila_SoftwareLibraries\\Java\\kdaquila\\StructuredLight-Commons\\Test_Resources\\trash";
        String save_filename = "Image2_rgb.png";
        ImageUtil.save(modifiedRGBImage, save_folder, save_filename);
        
        System.out.println("\n");
    }
    
    public static void testing_ListToGrayImage()
    {
        System.out.println("Testing: ListToGrayImage");
        
        // define file
        String load_folder = "F:\\kdaquila_SoftwareLibraries\\Java\\kdaquila\\StructuredLight-Commons\\Test_Resources";
        String load_filename = "Image1_gray.png";
        
        // load the gray
        BufferedImage grayImage = ImageUtil.load(load_folder, load_filename);
        
        // get the list
        List<List<Double>> grayList =ImageUtil.GrayImageToList(grayImage);
        
        // modify the list
        int nCols = grayImage.getWidth();
        int nRows = grayImage.getHeight();
        
        for (int row_num = 0; row_num < nRows; row_num++)
        {
            for (int col_num = 0; col_num < nCols; col_num++)
            {
                if (row_num > 0)
                {
                    grayList.get(row_num).set(col_num, 0.0);
                }

                if ((row_num == 6 || col_num == 6))
                {
                    grayList.get(row_num).set(col_num, 255.0);
                }
                
            }
        }
        
        // convert list to image
        BufferedImage modifiedGrayImage = ImageUtil.ListToGrayImage_Double(grayList);
        
        // save the gray image
        String save_folder = "F:\\kdaquila_SoftwareLibraries\\Java\\kdaquila\\StructuredLight-Commons\\Test_Resources\\trash";
        String save_filename = "Image2_gray.png";
        ImageUtil.save(modifiedGrayImage, save_folder, save_filename);
        
        System.out.println("\n");
    }
}
