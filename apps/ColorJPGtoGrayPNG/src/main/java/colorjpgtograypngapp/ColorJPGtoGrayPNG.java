package colorjpgtograypngapp;

import core.ImageUtils;
import static core.ImageUtils.save;
import gui.ProgressBar;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.SortedMap;
import javax.swing.JFileChooser;
import org.apache.commons.io.FilenameUtils;

public class ColorJPGtoGrayPNG {

    public static void main(String[] args) {
        // Get the input directory
        String dirname;
		String dirname_save;
        if (args.length != 0 && args.length != 2) {
            throw new IllegalArgumentException("Must provide exactly 0 or 2 arguments. Found " + args.length);
        } else if (args.length == 2) {
            dirname = args[0];
			dirname_save = args[1];
        } else {
			// Get the load directory
            JFileChooser f_load = new JFileChooser();
			f_load.setDialogTitle("Load image directory");
            f_load.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            f_load.setCurrentDirectory(new File(System.getProperty("user.home")));
            if (f_load.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                dirname = f_load.getSelectedFile().getAbsolutePath();
            } else {
                System.out.println("Ended by user");
                return;
            }
			
			// Get the save directory
			JFileChooser f_save = new JFileChooser();
			f_save.setDialogTitle("Save image directory");
            f_save.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            f_save.setCurrentDirectory(new File(System.getProperty("user.home")));
            if (f_save.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                dirname_save = f_save.getSelectedFile().getAbsolutePath();
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
        int counter = 0;
        int counterInc = 100/grayImageStack.size();
        for (String imgName: grayImageStack.keySet()) {
            
            progressBar.updateProgressBar(counter);
            progressBar.updateLabel("Saving: " + imgName);
            counter += counterInc;
            
            String fileName = imgName + ".png";
            BufferedImage img = grayImageStack.get(imgName);
            
			
            // String saveDir = FilenameUtils.getFullPathNoEndSeparator(dirname) + "\\PNG_8bitGray";
			save(img, dirname_save, fileName);
        }
        
        // Close the gui
        progressBar.dispatchEvent(new WindowEvent(progressBar, WindowEvent.WINDOW_CLOSING));
        
    }
    
}
