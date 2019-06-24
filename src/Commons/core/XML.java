package core;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Node;

public class XML {
    
    public Element root;
    public Map<String, Object> map;
    
    public XML(String path) {
        root = XML.loadXML(path);
        map = XML.loadMap(path, "config");
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
    
    public List<String> getStringList(String xPath) {
        
        
        List<Node> nodes = root.selectNodes(xPath);
        if (nodes.isEmpty()) {
            throw new RuntimeException("Could not find " + xPath + " in the configuration file");
        } 
        List<String> outputStrings = new ArrayList<>(nodes.size());
        for (Node n: nodes) {
            outputStrings.add(n.getText());
        }

        return outputStrings;
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
    
    public static Map<String, Object> loadMap(String path, String rootTag) {        
        Element root = loadXML(path);
        Map<String, Object> map = new HashMap<>();
        List<Node> nodes = root.selectNodes("/" + rootTag + "/*");
        for (Node node: nodes) {
            Element e = (Element) node;
            String typeValue = e.attributeValue("type");            
            String stringValue = node.getText();
            String tagName = node.getName();
            
            if (typeValue == null) {
                throw new RuntimeException("The XML element '" + tagName + "' is missing a type attribute");
            }
            
            Object parsedValue;
            switch (typeValue) {
                case "int":
                    parsedValue = Integer.parseInt(stringValue, 10);
                    break;
                case "hex":
                    parsedValue = Integer.parseInt(stringValue, 16);
                    break;
                case "double":
                    parsedValue = Double.parseDouble(stringValue);
                    break;
                case "boolean":
                    parsedValue = Boolean.parseBoolean(stringValue);
                    break;
                case "string":
                    parsedValue = stringValue;
                    break;
                default:
                    parsedValue = null;
            }
            map.put(tagName, parsedValue);
        }
        return map;        
    }
    
    public void prependBaseDir() {
        Node baseDirNode = root.selectSingleNode("/config/baseDir");
        List<Node> subDirNodes = root.selectNodes("/config/*[@role='subDir']");
        for (Node node: subDirNodes) {
            String tagName = node.getName();
            map.put(tagName, baseDirNode.getText() + "\\" + (String) map.get(tagName));
        }
    }
}