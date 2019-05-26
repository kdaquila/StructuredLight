package core;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;


public class ImageUtil {
    
    public static BufferedImage load(String folder, String filename)
    {
        BufferedImage output;
        try {
            output = ImageIO.read(new File(folder + "\\" + filename));
        }
        catch (IOException exp) {
            for (StackTraceElement elem: exp.getStackTrace()) {
                System.out.println(elem);
            }
            throw new RuntimeException("Can't read the images");
        }
        return output;
    }
        
    public static void save(BufferedImage inputImage, String folder, String name)
    {        
        try {
            // create the folder if necessary
            File folderDir = new File(folder);
            if (!folderDir.exists()) {
                FileUtils.forceMkdir(folderDir);
            }
            // write the image
            File outputFile = new File (folderDir + "\\" + name);
            ImageIO.write(inputImage, "png", outputFile);
        }
        catch (IOException exp) {
            for (StackTraceElement elem: exp.getStackTrace()) {
                System.out.println(elem);
            }
            throw new RuntimeException("Could not save the image");
        } 
    }
    
    /**
     * Returns a 3D array of RGB values ordered by row, column, channel.
     * 
     * @param inputImage
     * @return 3D array
     */
    public static List<List<List<Double>>> ColorImageToList(BufferedImage inputImage)
    {        
        // draw input to TYPE_INT_RGB
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        int rgbType = BufferedImage.TYPE_INT_RGB;
        BufferedImage rgbImage = new BufferedImage(width, height, rgbType);
        Graphics2D g = rgbImage.createGraphics();
        g.drawImage(inputImage, 0, 0, null);
        g.dispose();
        
        // get the 1D data buffer        
        int[] rgbData = ((DataBufferInt) rgbImage.getRaster().getDataBuffer()).getData(); 
                
        // make 3D array
        int nCols = inputImage.getWidth();
        int nRows = inputImage.getHeight();
        int nCh = 3;
        double[][][] rgb_Double3D = new double[nRows][nCols][nCh];
        List<List<List<Double>>> rgb_Double3DList = new ArrayList<>(nRows);        
           
        for (int row_num = 0; row_num < nRows; row_num++)
        {
            List<List<Double>> newRow = new ArrayList<>(nCols);
            for (int col_num = 0; col_num < nCols; col_num++)
            {
                List<Double> newChannel = new ArrayList<>(3);
                for (int ch_num = 0; ch_num < nCh; ch_num++)
                {
                    // compute position within linear array
                    int offset = row_num*nCols + col_num;
                    
                    // store the color value
                    switch (ch_num)
                    {                    
                        case 0:
                            int red = (rgbData[offset]>>16)&0xFF;
                            rgb_Double3D[row_num][col_num][ch_num] = (double)red;
                            newChannel.add((double)red);
                            break;                    
                        case 1:
                            int green = (rgbData[offset]>>8)&0xFF;
                            rgb_Double3D[row_num][col_num][ch_num] = (double)green;
                            newChannel.add((double)green);
                            break;
                        case 2:
                            int blue = (rgbData[offset])&0xFF;
                            rgb_Double3D[row_num][col_num][ch_num] = (double)blue;
                            newChannel.add((double)blue);
                            break;
                        default:
                            throw new RuntimeException("Image can't have more than 3 channels");
                    }                                    
                }
                newRow.add(newChannel);
            }
            rgb_Double3DList.add(newRow);
        }        
        
        return rgb_Double3DList;
    }
    
    /**
     * Returns a 2D array of Gray values ordered by row and column.
     * 
     * @param inputImage
     * @return
     */
    public static List<List<Double>> GrayImageToList(BufferedImage inputImage)
    {        
        // get the 1D data buffer
        byte[] gray_Byte1D = ((DataBufferByte) inputImage.getRaster().getDataBuffer()).getData();
                
        // make 2D array
        int nCols = inputImage.getWidth();
        int nRows = inputImage.getHeight();
        double[][] gray_Double2D = new double[nRows][nCols];
        for (int row_num = 0; row_num < nRows; row_num++)
        {
            for (int col_num = 0; col_num < nCols; col_num++)
            {
                // compute position within linear array
                int offset = row_num*nCols + col_num;
                // cast byte to int's least significant bits
                int castedInt = (int)(gray_Byte1D[offset]&0xFF);
                // store in double array
                gray_Double2D[row_num][col_num] = (double)castedInt;                
            }
        }
        
        // convert to list
        List<List<Double>> output = ArrayUtils.ArrayToList_Double2D(gray_Double2D);
        
        return output;
    }
    
    /**
     * Returns a BufferedImage given a 3D array of colors ordered by rows,
     *  column, and channel
     * @param array
     * @return
     */
    public static BufferedImage ListToColorImage(List<List<List<Double>>> array)
    {        
        // create the output image
        int width = array.size();
        int height = array.get(0).size();
        int type = BufferedImage.TYPE_INT_RGB;
        BufferedImage rgbImage = new BufferedImage(width, height, type);
        
        // get the 1D data buffer        
        int[] rgbData = ((DataBufferInt) rgbImage.getRaster().getDataBuffer()).getData();
        
        // initialize the 1d data buffer
        for (int index = 0; index < width*height; index++)
        {
            rgbData[index] = 0;
        }
                
        // make 3D array
        int nCols = width;
        int nRows = height;
        int nCh = 3;
        
        for (int row_num = 0; row_num < nRows; row_num++)
        {
            for (int col_num = 0; col_num < nCols; col_num++)
            {
                // compute position within linear array
                int offset = row_num*nCols + col_num;
                
                for (int ch_num = 0; ch_num < nCh; ch_num++)
                {
                    // get the color value
                    double value = array.get(row_num).get(col_num).get(ch_num);

                    // clamp between 0.0 - 255.0
                    if (value > 255.0) value = 255.0;
                    else if (value < 0.0) value = 0.0;

                    // store the color value as integer
                    switch (ch_num)
                    {                    
                        case 0:                            
                            rgbData[offset] |= ((int)value<<16);
                            break;                    
                        case 1:
                            rgbData[offset] |= ((int)value<<8);
                            break;
                        case 2:
                            rgbData[offset] |= ((int)value);
                            break;
                        default:
                            throw new RuntimeException("Image can't have more than 3 channels");
                    } 
                }
            }
        }              
        
        return rgbImage;
    }
    
    /**
     * Returns a BufferedImage given a 2D array of gray values ordered by
     * rows, and column. This function assumes that the Double values
     * have already been normalized to the range 0-255. Values outside
     * this range will be clamped.
     * @param array
     * @return
     */
    public static BufferedImage ListToGrayImage_Double(List<List<Double>> array)
    {        
        // create the output image
        int width = array.size();
        int height = array.get(0).size();
        int type = BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage grayImage = new BufferedImage(width, height, type);
        
        // get the 1D data buffer        
        byte[] grayData = ((DataBufferByte) grayImage.getRaster().getDataBuffer()).getData();
        
        // initialize the 1d data buffer
        for (int index = 0; index < width*height; index++)
        {
            grayData[index] = 0;
        }
                
        // store the values
        int nCols = width;
        int nRows = height;
        for (int row_num = 0; row_num < nRows; row_num++)
        {
            for (int col_num = 0; col_num < nCols; col_num++)
            {
                // compute position within linear array
                int offset = row_num*nCols + col_num;

                // get the gray value
                double value = array.get(row_num).get(col_num);

                // clamp between 0.0 - 255.0
                if (value > 255.0) value = 255.0;
                else if (value < 0.0) value = 0.0;
                
                // cast double to int
                int valueInt = (int)value;
                
                // cast int to byte using only least significant bits
                byte valueByte = (byte)(valueInt&0xFF);
                
                // store the color value as byte
                grayData[offset] = valueByte;                                
            }
        }               
        
        return grayImage;
    }
    
    public static BufferedImage ListToGrayImage_Integer(List<List<Integer>> array)
    {        
        // create the output image
        int height = array.size();
        int width = array.get(0).size();
        int type = BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage grayImage = new BufferedImage(width, height, type);
        
        // get the 1D data buffer        
        byte[] grayData = ((DataBufferByte) grayImage.getRaster().getDataBuffer()).getData();
        
        // initialize the 1d data buffer
        for (int index = 0; index < width*height; index++)
        {
            grayData[index] = 0;
        }
                
        // store the values
        int nCols = width;
        int nRows = height;
        for (int row_num = 0; row_num < nRows; row_num++)
        {
            for (int col_num = 0; col_num < nCols; col_num++)
            {
                // compute position within linear array
                int offset = row_num*nCols + col_num;

                // get the gray value
                int value = array.get(row_num).get(col_num);

                // clamp between 0 - 255
                if (value > 255) value = 255;
                else if (value < 0) value = 0;
                
                // cast int to byte using only least significant bits
                byte valueByte = (byte)(value & 0xFF);
                
                // store the color value as byte
                grayData[offset] = valueByte;                                
            }
        }               
        
        return grayImage;
    }
    
    public static BufferedImage color2Gray (BufferedImage inputImage)
    {
        // draw input to TYPE_INT_RGB
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        int rgbType = BufferedImage.TYPE_INT_RGB;
        BufferedImage rgbImage = new BufferedImage(width, height, rgbType);
        Graphics2D rgbGraphics = rgbImage.createGraphics();
        rgbGraphics.drawImage(inputImage, 0, 0, null);
        rgbGraphics.dispose();
        
        // convert TYPE_INT_RGB to TYPE_BYTE_GRAY        
        int grayType = BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage grayImage = new BufferedImage(width, height, grayType);
        Graphics2D grayGraphics = grayImage.createGraphics();
        grayGraphics.drawImage(rgbImage, 0, 0, null);
        grayGraphics.dispose();   
        
        return grayImage;
    }
    
    public static BufferedImage color2Gray(BufferedImage inputImage, float rMult, float gMult, float bMult)
    {
        // draw input to TYPE_INT_RGB
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        int rgbType = BufferedImage.TYPE_INT_RGB;
        BufferedImage rgbImage = new BufferedImage(width, height, rgbType);
        Graphics2D g = rgbImage.createGraphics();
        g.drawImage(inputImage, 0, 0, null);
        g.dispose();
        
        // convert TYPE_INT_RGB to TYPE_BYTE_GRAY
        int grayType = BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage grayImage = new BufferedImage(width, height, grayType);
        int[] rgbData = ((DataBufferInt) rgbImage.getRaster().getDataBuffer()).getData();
        byte[] grayData = ((DataBufferByte) grayImage.getRaster().getDataBuffer()).getData();        
        for (int i = 0; i < width*height; i++)
        {
            int rgbPix = rgbData[i];
            int red = (rgbPix >> 16)&0xFF;
            int green = (rgbPix >> 8)&0xFF;
            int blue = (rgbPix)&0xFF;
            grayData[i] = (byte)((int)(rMult*red) + (int)(gMult*green) + (int)(bMult*blue));
        }          
        
        return grayImage;
    }
    
    
    public static BufferedImage transpose(BufferedImage inputImage)
    {
        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        int type = inputImage.getType();
        BufferedImage outputImage = new BufferedImage(height, width, type);
        AffineTransform transform = new AffineTransform();
        transform.concatenate(AffineTransform.getQuadrantRotateInstance(1));
        transform.concatenate(AffineTransform.getScaleInstance(1.0, -1.0));
        Graphics2D g = outputImage.createGraphics();
        g.transform(transform);
        g.drawImage(inputImage, 0, 0, null);
        g.dispose();
        return outputImage;
    }
    
    public static BufferedImage meanFilter(BufferedImage inputGrayImage, int radius)
    {
        BufferedImage outputImage;        
        outputImage = ImageUtil.meanFilterRows(inputGrayImage, radius);
        outputImage = ImageUtil.meanFilterRows(outputImage, radius);
        outputImage = ImageUtil.meanFilterRows(outputImage, radius);
        outputImage = ImageUtil.transpose(outputImage);
        outputImage = ImageUtil.meanFilterRows(outputImage, radius);
        outputImage = ImageUtil.meanFilterRows(outputImage, radius);
        outputImage = ImageUtil.meanFilterRows(outputImage, radius);
        outputImage = ImageUtil.transpose(outputImage);
        
        return outputImage;
    }
    
    private static BufferedImage meanFilterRows(BufferedImage inputGrayImage, int radius)
    {                
        // Initialize the output image
        int width = inputGrayImage.getWidth();
        int height = inputGrayImage.getHeight();
        int type = BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage blurImage = new BufferedImage(width, height, type);
        
        // Get the buffers
        byte[] grayData = ((DataBufferByte)inputGrayImage.getData().getDataBuffer()).getData();
        byte[] blurData = ((DataBufferByte)blurImage.getRaster().getDataBuffer()).getData();
        
        // Define kernal value
        float kernalWt = 1.0f/(2*radius + 1);
              
        // Filter along each row
        for (int row = 0; row < height; row++)
        {
            float rowConvSum = 0;
            for (int col = 0; col < width; col++)
            {
                // Define position in linear array
                int bufferPos = row*width + col;
                
                // Sum initial terms for rowConvSum and for edgeConvSum
                // the kernal weights are special here, but constant
                if (col == 0)
                {
                    float edgeConvSum = 0;
                    float edgeKernalWt = 1.0f/(radius + 1);
                    for (int offset = 0; offset < (radius+1); offset++ )
                    {
                        int gray = grayData[bufferPos + offset] & 0xFF;
                        rowConvSum += kernalWt*gray;  
                        edgeConvSum += edgeKernalWt*gray;
                    }
                    blurData[bufferPos] = (byte) edgeConvSum;                     
                }
                // Only add to rowConvSum, but sum terms for edgeConvSum
                // the kernal weights are special here, and not constant
                else if (col <= radius)
                {
                    // update rowConvSum
                    int gray = grayData[bufferPos + radius] & 0xFF;
                    rowConvSum += kernalWt*gray;
                    
                    // update edgeConvSum
                    float edgeConvSum = 0;
                    float edgeKernalWt = 1.0f/(col + radius + 1);
                    for (int offset = -col; offset <= radius; offset++ )
                    {
                        gray = grayData[bufferPos + offset] & 0xFF;
                        edgeConvSum += edgeKernalWt*gray;
                    }
                    blurData[bufferPos] = (byte) edgeConvSum; 
                }
                // Add and subtract from convSum
                // the kernal weights are regular here, and constant
                else if (col > radius && col < (width - radius))
                {
                    int newGray = grayData[bufferPos + radius] & 0xFF;
                    rowConvSum += kernalWt*newGray;
                    int oldGray = grayData[bufferPos - radius - 1] & 0xFF;
                    rowConvSum -= kernalWt*oldGray;
                    blurData[bufferPos] = (byte) rowConvSum; 
                }
                // Sum terms for edgeConvSum
                // the kernal weights are special here, and not constant
                else if ((col >= width - radius) && (col < (width - 1)))
                {
                    float edgeConvSum = 0;
                    float edgeKernalWt = 1.0f/(width -1 - col + radius);
                    for (int offset = -radius; offset < width - 1 - col ; offset++ )
                    {
                        int gray = grayData[bufferPos + offset] & 0xFF;
                        edgeConvSum += edgeKernalWt*gray;
                    }
                    blurData[bufferPos] = (byte) edgeConvSum; 
                }  
                // Sum terms for final edgeConvSum
                // the kernal weights are special here, and constant
                else if (col == (width-1))
                {
                    float edgeConvSum = 0;
                    float edgeKernalWt = 1.0f/(radius + 1);
                    for (int offset = -radius; offset <= 0; offset++ )
                    {
                        int gray = grayData[bufferPos + offset] & 0xFF;
                        edgeConvSum += edgeKernalWt*gray;
                    }
                    blurData[bufferPos] = (byte) edgeConvSum;                     
                }
            }
        }      
        return blurImage;
    }
    
    public static BufferedImage adaptiveThreshold(BufferedImage grayImage, int windowSize, int offset)
    {        
        // Declare the output image        
        int nCols = grayImage.getWidth();
        int nRows = grayImage.getHeight();
        int type = BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage bwImage = new BufferedImage(nCols, nRows, type);
        for (int index =0; index < nRows*nCols; index++)
        {
            
        }
        
        // Blur
        BufferedImage blurImage = ImageUtil.meanFilter(grayImage, windowSize);
        
        // get buffers
        byte[] grayData = ((DataBufferByte)grayImage.getData().getDataBuffer()).getData();
        byte[] blurData = ((DataBufferByte)blurImage.getData().getDataBuffer()).getData();
        byte[] bwData = ((DataBufferByte)bwImage.getRaster().getDataBuffer()).getData();
        
        // do thresholding
        for (int row_num =0; row_num < nRows; row_num++)
        {
            for (int col_num = 0; col_num < nCols; col_num++)
            {                
                // compute the offset
                int pos = row_num*nCols + col_num;
                
                // get the gray image value
                int grayValue = (int)(grayData[pos] & 0xFF);
                
                // get the blur image value
                int blurValue = (int)(blurData[pos] & 0xFF);
                
                // evaluate black white image value
                if (grayValue > (blurValue + offset))
                {
                    int value = (255 & 0xFF);
                    byte valueByte = (byte)value;
                    bwData[pos] = valueByte;
                }
                else
                {
                    bwData[pos] = 0;
                }                                
            }
        }
        
        return bwImage;
    }
    
    public static void fillBoundary(BufferedImage grayImage, int value) 
    {
        // get buffers
        byte[] grayData = ((DataBufferByte)grayImage.getRaster().getDataBuffer()).getData();
        
        // get dimensions
        int nRows = grayImage.getHeight();
        int nCols = grayImage.getWidth();
        
        // do the filling
        for (int row_num = 0; row_num < nRows; row_num++)
        {
            for (int col_num = 0; col_num < nCols; col_num++)
            {
                // compute offset
                int offset = row_num*nCols + col_num;
                
                // detect boundaries
                if ((row_num == 0) || (col_num == 0) || 
                    (row_num == (nRows-1)) || (col_num == (nCols-1)) ) 
                {
                    grayData[offset] = (byte)(value & 0xFF);
                }
            }    
        }
    }
    
    public static void fillImage(BufferedImage grayImage, int value) 
    {
        // get buffers
        byte[] grayData = ((DataBufferByte)grayImage.getRaster().getDataBuffer()).getData();
        
        // get dimensions
        int nRows = grayImage.getHeight();
        int nCols = grayImage.getWidth();
        
        // do the filling
        for (int row_num = 0; row_num < nRows; row_num++)
        {
            for (int col_num = 0; col_num < nCols; col_num++)
            {
                // compute offset
                int offset = row_num*nCols + col_num;
                
                grayData[offset] = (byte)(value & 0xFF);                
            }    
        }
    }
    
    public static List<Double> interpolate_1D(BufferedImage grayImage, List<List<Double>> points)
    {
        // get buffer
        int nRows = grayImage.getHeight();
        int nCols = grayImage.getWidth();
        byte[] grayData = ((DataBufferByte)grayImage.getData().getDataBuffer()).getData();
        
        List<Double> interpValues = new ArrayList<>(points.size());
        for (List<Double> point: points)
        {
            interpValues.add(interpolate_0D(grayImage, point));                    
        }   
        return interpValues;
    }
    
    public static double interpolate_0D(BufferedImage grayImage, List<Double> xy)
    {
        // get buffer
        int nRows = grayImage.getHeight();
        int nCols = grayImage.getWidth();
        byte[] grayData = ((DataBufferByte)grayImage.getData().getDataBuffer()).getData();
        
        // get center coordinates
        double centerX = xy.get(0);
        double centerY = xy.get(1);

        // get offsets to neighboring point
        int offsetNE = (int)Math.floor(centerY) * nCols + (int)Math.ceil(centerX);
        int offsetNW = (int)Math.floor(centerY) * nCols + (int)Math.floor(centerX);
        int offsetSE = (int)Math.ceil(centerY) * nCols + (int)Math.ceil(centerX);
        int offsetSW = (int)Math.ceil(centerY) * nCols + (int)Math.floor(centerX);

        // get displacement from center
        double xDisp = (centerX-Math.floor(centerX));
        double yDisp = (centerY-Math.floor(centerY));

        // linear interpolation
        double interpValue = xDisp * (1-yDisp) * (int)(grayData[offsetNE]&0xFF) + 
                           (1-xDisp) * (1-yDisp) * (int)(grayData[offsetNW]&0xFF) +
                           xDisp * yDisp * (int)(grayData[offsetSE]&0xFF) +
                           (1-xDisp) * yDisp * (int)(grayData[offsetSW]&0xFF);

        return interpValue;          
    }
    
}