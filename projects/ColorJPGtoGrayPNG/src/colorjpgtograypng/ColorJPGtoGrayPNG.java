package colorjpgtograypng;

import core.ImageUtils;
import static core.ImageUtils.save;
import gui.ProgressBar;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.SortedMap;
import javax.swing.JFileChooser;

public class ColorJPGtoGrayPNG {

    public static void main(String[] args) {
        // Get the input directory
        String dirname;
        if (args.length >= 2) {
            throw new IllegalArgumentException("Must provide exactly 0-1 arguments. Found " + args.length);
        } else if (args.length == 1) {
            dirname = args[0];
        } else {
            JFileChooser f = new JFileChooser();
            f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            f.setCurrentDirectory(new File(System.getProperty("user.home")));
            int result = f.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                dirname = f.getSelectedFile().getAbsolutePath();
            } else {
                System.out.println("Ended by user");
                return;
            }
        }
        System.out.println("Loading images from: " + dirname);
        
        ProgressBar progressBar = new ProgressBar();
        
        // Load the images
        progressBar.updateLabel("Loading");
        SortedMap<String, BufferedImage> rgbImageStack = ImageUtils.load_batch(dirname);
        
        // Convert the images
        progressBar.updateLabel("Converting");
        SortedMap<String, BufferedImage> grayImageStack = ImageUtils.color2Gray_batch(rgbImageStack);
        
        // Save the images
        String saveDirName = dirname + "\\gray";
        int counter = 0;
        int counterInc = 100/grayImageStack.size();
        for (String imgName: grayImageStack.keySet()) {
            
            progressBar.updateProgressBar(counter);
            progressBar.updateLabel("Saving: " + imgName);
            counter += counterInc;
            
            String fileName = imgName + ".png";
            BufferedImage img = grayImageStack.get(imgName);
            save(img, saveDirName, fileName);
        }
        
        // Close the gui
        progressBar.dispatchEvent(new WindowEvent(progressBar, WindowEvent.WINDOW_CLOSING));
        
    }
    
}
