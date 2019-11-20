package tif16tofits64app;

import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.util.BufferedFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

class FITS {

    static void saveData(Object array, String dirname, String filename) {

        // Create the FITS data structure
        Fits f = new Fits();
        try {
            f.addHDU(FitsFactory.HDUFactory(array));
        } catch (FitsException e) {
            throw new RuntimeException(e.getMessage());
        }

        // Create the folder if necessary
        try {
            FileUtils.forceMkdir(new File(dirname));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        // Save the FITS structure
        try {
            BufferedFile bf = new BufferedFile(dirname + "\\" + filename, "rw");
            f.write(bf);
            bf.close();
        } catch (FitsException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
