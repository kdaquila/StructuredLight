package markers;

public class Markers {
    
    public static void main(String[] args) {
        find();        
    }    
    
    public static void draw() {
        String save_path = "C:\\Users\\kfd18\\kfd18_Downloads\\img.png";
        RingGrid grid = new RingGrid(10,10);
        grid.draw(save_path);
    }
    
    public static void find() {
        String rgbImgPath = "C:\\Users\\kfd18\\OneDrive\\kdaquila_SoftwareDev\\Structured-Light\\StructuredLight-Commons\\Test_Resources\\RingGrid_Images\\2300.png";
        String savePath = "C:\\Users\\kfd18\\OneDrive\\kdaquila_SoftwareDev\\Structured-Light\\StructuredLight-Commons\\Test_Resources\\RingGrid_Points\\imagePoints.txt";
        RingGrid grid = new RingGrid(13,17);
        grid.find(rgbImgPath, savePath);
    }
}
