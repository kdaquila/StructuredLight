package tif16tofits64app;

import ij.ImagePlus;
import ij.io.FileInfo;
import ij.io.ImageReader;
import ij.io.Opener;
import ij.io.TiffDecoder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class TIFFtoFITS {

    private final String directoryName;
    private final String fileName;
    int nRows;
    int nCols;
    int nBands;

    public TIFFtoFITS(String dir, String filename) {
        this.directoryName = dir;
        this.fileName = filename;
        nRows = 0;
        nCols = 0;
        nBands = 3;
    }

    public double[][] getGrayImage() {
        double redWt = 0.21;
        double greenWt = 0.72;
        double blueWt = 0.07;
        return getGrayImage(redWt, greenWt, blueWt);
    }

    public double[][] getGrayImage(double redWt, double greenWt, double blueWt) {
        // Get the data
        short[][] dataShorts_viaOpener = getRawData();

        // Convert to doubles
        double[][] dataDoubles = convertShort2DToDouble2D(dataShorts_viaOpener);

        // Compute the gray image
        double[][] grayImage = new double[nRows][nCols];
        for (int row_num = 0; row_num < nRows; row_num++) {
            for (int col_num = 0; col_num < nCols; col_num++) {
                int i = row_num*nCols + col_num;
                double redValue = dataDoubles[0][i];
                double greenValue = dataDoubles[0][i];
                double blueValue = dataDoubles[0][i];
                double grayValue = redWt*redValue + greenWt*greenValue + blueWt*blueValue;
                grayImage[row_num][col_num] = grayValue;
            }
        }

        return grayImage;
    }

    public double[][][] getRGBImageStack() {
        // Get the data
        short[][] dataShorts_viaOpener = getRawData();

        // Convert to doubles
        double[][] dataDoubles = convertShort2DToDouble2D(dataShorts_viaOpener);

        // Build the rgb image stack
        double[][][] images = new double[nBands][nRows][nCols];
        for (int band_num = 0; band_num < nBands; band_num++) {
            images[band_num] = reshape1Dto2D(dataDoubles[band_num], nRows, nCols);
        }

        return images;
    }

    public short[][] getRawData() {
        return getRawData_viaImageReader();
    }

    public short[][] getRawData_viaOpener() {
        Opener opener = new Opener();
        ImagePlus imagePlus = opener.openImage(directoryName, fileName);
        int[] dims = imagePlus.getDimensions();
        nCols = dims[0];
        nRows = dims[1];
        int nPixelsPerBand = nCols*nRows;
        short[][] dataStack =  new short[3][nPixelsPerBand];
        for (int band_num = 0; band_num < 3; band_num++) {
            dataStack[band_num] = (short[]) imagePlus.getStack().getPixels(band_num+1);
        }
        return dataStack;
    }

    public short[][] getRawData_viaImageReader() {
        // Open the TIFF Decoder
        TiffDecoder tiffDecoder = new TiffDecoder(directoryName, fileName);

        // Get the TIFF File Info List
        FileInfo[] fileInfos;
        try {
            fileInfos = tiffDecoder.getTiffInfo();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        // Find the main file's info
        FileInfo mainFileInfo = new FileInfo();
        boolean foundMain = false;
        for (FileInfo fileInfo: fileInfos) {
            if (fileInfo.fileType==FileInfo.RGB48) {
                mainFileInfo = fileInfo;
                foundMain = true;
                break;
            }
        }
        if (!foundMain) throw new RuntimeException("Could not find the RGB48 image inside the given file");

        nCols = mainFileInfo.width;
        nRows = mainFileInfo.height;

        // Read the Data as 16-bit integers
        String inputString = directoryName + "\\" + fileName;
        InputStream inputStream;
        try {
             inputStream = new FileInputStream(inputString);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }

        ImageReader imageReader = new ImageReader(mainFileInfo);

        return (short[][]) imageReader.readPixels(inputStream);
    }

    public double[][] convertShort2DToDouble2D(short[][] inputs) {
        // Convert to 32-bit floats
        int dim1 = inputs.length;
        int dim2 = inputs[0].length;
        double[][] dataDoubles = new double[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            for (int j = 0; j < dim2; j++) {
                // cast short to int, but interpret the shorts as unsigned, so apply a bit-mask
                int intValue = (int) inputs[i][j]&0xFFFF;
                // cast int to double
                dataDoubles[i][j] = intValue;
            }
        }
        return dataDoubles;
    }

    public double[][] reshape1Dto2D(double[] input, int nRows, int nCols) {
        double[][] output = new double[nRows][nCols];
        for (int row_num = 0; row_num < nRows; row_num++) {
            for (int col_num = 0; col_num < nCols; col_num++) {
                int index = row_num*nCols + col_num;
                output[row_num][col_num] = input[index];
            }
        }
        return output;
    }
}
