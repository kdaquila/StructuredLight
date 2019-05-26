package core;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;

public class XML {

    public static Element loadXML(String path) {
        File inputFile = new File(path);
        SAXReader reader = new SAXReader();
        Element output;
        try {
            org.dom4j.Document doc = reader.read( inputFile );
            output = doc.getRootElement();
        }
        catch (DocumentException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Could not open the XML configuration document");
        }
        return output;
    }
}