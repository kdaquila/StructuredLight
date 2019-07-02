package core;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;

public class BufferedImageFactory {   
    
    public static BufferedImage build_8bit_Gray(int[][] array) {

        int nRows = array.length;
        int nCols = array[0].length;
        BufferedImage outputImage = new BufferedImage(nCols, nRows, BufferedImage.TYPE_BYTE_GRAY);
        byte[] data = ((DataBufferByte) outputImage.getRaster().getDataBuffer()).getData();
        int nPixels = data.length;
        for (int pix_num = 0; pix_num < nPixels; pix_num++ ) {
            int row_num = pix_num/nCols;
            int col_num = pix_num - row_num*nCols;
            int value = array[row_num][col_num];
            if (value > 255 || value < 0) {
                throw new IllegalArgumentException("the array values are outside the range (0,255)");
            } else {
                data[pix_num] = (byte) array[row_num][col_num];
            }            
        }        
        return outputImage;
    }

    public static BufferedImage build_10bit_Gray(int[][] array) {
        int nRows = array.length;
        int nCols = array[0].length;
        BufferedImage outputImage = new BufferedImage(nCols, nRows, BufferedImage.TYPE_USHORT_GRAY);
        short[] data = ((DataBufferUShort) outputImage.getRaster().getDataBuffer()).getData();
        int nPixels = data.length;
        for (int pix_num = 0; pix_num < nPixels; pix_num++ ) {
            int row_num = pix_num/nCols;
            int col_num = pix_num - row_num*nCols;
            int value = array[row_num][col_num];
            if (value > 1023 || value < 0) {
                throw new IllegalArgumentException("the array values are outside the range (0,1023)");
            } else {
                data[pix_num] = (short) array[row_num][col_num];
            }
            
        }        
        return outputImage;
    }
    
    public static BufferedImage build_16bit_Gray(int[][] array) {
        int nRows = array.length;
        int nCols = array[0].length;
        BufferedImage outputImage = new BufferedImage(nCols, nRows, BufferedImage.TYPE_USHORT_GRAY);
        short[] data = ((DataBufferUShort) outputImage.getRaster().getDataBuffer()).getData();
        int nPixels = data.length;
        for (int pix_num = 0; pix_num < nPixels; pix_num++ ) {
            int row_num = pix_num/nCols;
            int col_num = pix_num - row_num*nCols;
            int value = array[row_num][col_num];
            if (value > 65535 || value < 0) {
                throw new IllegalArgumentException("the array values are outside the range (0,65535)");
            } else {
                data[pix_num] = (short) array[row_num][col_num];
            }            
        }        
        return outputImage;
    }
}
