package core;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.IIOImage;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.stream.ImageOutputStream;
import java.util.Iterator;
import java.io.File;
import java.io.IOException;

public class PNG {

    String formatName;
    int formatType;
    String metaDataFormat;
    IIOMetadata metadata;
    ImageWriter writer;
    
    public PNG()
    {        
        formatName = "png";
        formatType = BufferedImage.TYPE_INT_RGB;
        metaDataFormat = "javax_imageio_1.0";
                 
        // Get a writer
        Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName);
        writer = iw.next();
        
        // Get the default metadata
        ImageTypeSpecifier typeSpec = ImageTypeSpecifier.createFromBufferedImageType(formatType);
        metadata = writer.getDefaultImageMetadata(typeSpec, null);
    }
    
    private void setDPI(float dpi)
    {
        float pixelSize_mm = dpi/25.4f;
        
        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Float.toString(pixelSize_mm));
        
        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Float.toString(pixelSize_mm));
        
        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);
        
        IIOMetadataNode root = new IIOMetadataNode(metaDataFormat);
        root.appendChild(dim);
        
        try
        {
            metadata.mergeTree(metaDataFormat, root);
        }
        catch (IIOInvalidTreeException e)
        {
            System.out.println("Could not set the dpi.");
        }       
    }   
    
    public void save(BufferedImage image, String  path) {
        int dpi = 300;
        save(image, path, dpi);
    }
    
    public void save(BufferedImage image, String path, int dpi)
    {
        File outputFile = new File(path);
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(outputFile))
        {
            writer.setOutput(stream);
            setDPI(dpi);
            IIOImage imgOut = new IIOImage(image, null, metadata);
            writer.write(imgOut);
        }
        catch (IOException e)
        {
            System.out.println("Could not open a stream to the file");
        }
        
    }

}
