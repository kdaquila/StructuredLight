package core;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import org.dom4j.Node;

public class XML {
    
    Element root;
    
    public XML(String path) {
        root = XML.loadXML(path);
    }
    
    public int getInt(String xPath) {
        int radix = 10;
        return getInt(xPath, radix);
    }
    
    public int getInt(String xPath, int radix) {
        
        Node valueNode = root.selectSingleNode(xPath);            
        if (valueNode == null) {
            throw new RuntimeException("Could not find " + xPath + " in the configuration file");
        }        
        return Integer.parseInt(valueNode.getText(), radix);
    }
    
    public String getString(String xPath) {
        
        Node valueNode = root.selectSingleNode(xPath);            
        if (valueNode == null) {
            throw new RuntimeException("Could not find " + xPath + " in the configuration file");
        }        
        return valueNode.getText();
    }
    
    public boolean getBool(String xPath) {
        
        Node valueNode = root.selectSingleNode(xPath);            
        if (valueNode == null) {
            throw new RuntimeException("Could not find " + xPath + " in the configuration file");
        }        
        return Boolean.parseBoolean(valueNode.getText());
    }
    
    public double getDouble(String xPath) {
        
        Node valueNode = root.selectSingleNode(xPath);            
        if (valueNode == null) {
            throw new RuntimeException("Could not find " + xPath + " in the configuration file");
        }        
        return Double.parseDouble(valueNode.getText());
    }

    public static Element loadXML(String path) {
        File inputFile = new File(path);
        SAXReader reader = new SAXReader();
        Element rootElement;
        try {
            org.dom4j.Document doc = reader.read( inputFile );
            rootElement = doc.getRootElement();
        }
        catch (DocumentException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Could not open the XML configuration document from " + path);
        }
        return rootElement;
    }
}