package tif16tofits64app;

import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;

public class TIF16toFITS64 {

    public static void main(String[] args) {

        System.out.println("Running the TIFF to FITS Batch App");

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

        // Get all the file names
        File dirFile = new File(dirname);
        String[] fileNames = dirFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.toLowerCase().endsWith(".tif") || name.toLowerCase().endsWith(".tiff"));
            }
        });
        if (fileNames == null) throw new RuntimeException("did not find any filenames in the folder");

        GUI gui = new GUI();
        gui.updateProgressBar(0);

        // Convert TIFF to FITS
        for (int file_num = 0; file_num < fileNames.length; file_num++) {

            // Get the filename
            String filename = fileNames[file_num];

            System.out.println("Now converting image: " + filename);

            // update the progress bar
            int percentProgress = (int)(100.0*file_num/fileNames.length);
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    gui.updateProgressBar(percentProgress);
                    gui.updateLabel(filename);
                }
            });

            TIFFtoFITS app = new TIFFtoFITS(dirname, filename);

            // Change the file name extension to FITS
            String baseFileName = FilenameUtils.removeExtension(filename);
            String saveFileName = baseFileName + ".FITS";

            // compute the gray image data
            double[][] grayImage = app.getGrayImage();

            // save the gray image data
            System.out.println("Saving images to: " + dirname_save);
            FITS.saveData(grayImage, dirname_save, saveFileName);
        }

        // Close the gui
        gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING));

    }
}
