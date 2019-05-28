package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

public class TXT {
        
    public static <T> List<List<T>> loadMatrix(String path, Class<T> type, String delimiter, String EOL)
    {
        List<List<T>> output = new ArrayList<>();
        try {
            File newFile = new File(path);
            FileReader reader = new FileReader(newFile);
            BufferedReader buffReader = new BufferedReader(reader);
            for (String newLine; (newLine = buffReader.readLine()) != null; ) {            
                output.add(StringToVector(newLine, type, delimiter));
            }
        }
        catch(IOException exp){
            throw new RuntimeException("Could not read a line from the file");
        }
        return output;
    }
    
    public static <T> List<T> loadVector(String path, Class<T> type, String delimiter, String EOL)
    {
        List<T> output = new ArrayList<>();
        try {
            File newFile = new File(path);
            FileReader reader = new FileReader(newFile);
            BufferedReader buffReader = new BufferedReader(reader);
            String newLine = buffReader.readLine();
            output = StringToVector(newLine, type, delimiter);            
        }
        catch(IOException exp){
            throw new RuntimeException("Could not read a line from the file");
        }
        return output;
    }
    
    public static <T> void saveMatrix(List<List<T>> matrix, Class<T> type, 
                                            String path, String formatString) {
        String delimiter = ",";
        String EOL = "\n";
        boolean append = false;
        saveMatrix(matrix, type, path, formatString, delimiter, EOL, append);
    }
    
    public static <T> void saveMatrix(List<List<T>> matrix, Class<T> type, 
                                            String path, String formatString, 
                                            String delimiter, String EOL, 
                                            boolean append){
        try {
            // open the file
            File newFile = new File(path);
            
            // open the writer
            FileWriter writer = new FileWriter(newFile, append);
            BufferedWriter buffWriter = new BufferedWriter(writer);
            
            // write the string
            String matrixString = MatrixToString(matrix, formatString, delimiter, EOL);            
            buffWriter.write(matrixString);
            
            // close the writer
            buffWriter.close();
        }
        catch (IOException exp){
            throw new RuntimeException("Could not save to the file");
        }
        
    }
    
    public static <T> void saveMatrix(List<List<T>> matrix, Class<T> type, 
                                            String folder, String filename, 
                                            String formatString) {
        createFolder(folder);
        String path = folder + "\\" + filename;
        String delimiter = ",";
        String EOL = "\n";
        boolean append = false;
        saveMatrix(matrix, type, path, formatString, delimiter, EOL, append);
    }
    
    
    public static <T> void saveMatrix(List<List<T>> matrix, Class<T> type, 
                                            String folder, String filename, 
                                            String formatString, 
                                            String delimiter, String EOL, 
                                            boolean append){
        createFolder(folder);
        String path = folder + "\\" + filename;
        saveMatrix(matrix, type, path, formatString, delimiter, EOL, append);
    }
    
    public static <T> void saveVector(List<T> vector, Class<T> type, 
                                            String folder, String filename, boolean append, 
                                            String formatString, 
                                            String delimiter){
        try {
            // create the folder if needed
            File dir = new File(folder);
            if (!dir.exists())
            {
                FileUtils.forceMkdir(dir);
            }
            
            // open the writer
            File newFile = new File(folder + "\\" + filename);
            FileWriter writer = new FileWriter(newFile, append);
            BufferedWriter buffWriter = new BufferedWriter(writer);
            
            // write the string
            String vectorString = VectorToString(vector, formatString, delimiter);            
            buffWriter.write(vectorString);
            
            // close the writer
            buffWriter.close();
        }
        catch (IOException exp){
            throw new RuntimeException("Could not save to the file");
        }
    }
        
    public static <T> List<List<T>> StringToMatrix(String input, Class<T> type, String delimiter, String EOL){
        List<List<T>> out = new ArrayList<>();
        String[] rowStrings = input.split(EOL);
        for (String rowString: rowStrings){
            out.add(StringToVector(rowString, type, delimiter));
        }
        return out;
    }  
    
    public static <T> List<T> StringToVector(String input, Class<T> type, String delimiter){
        String[] inputParts = input.split(delimiter);
        List<T> out = new ArrayList<>();
        String type_name = type.getSimpleName();
        for (String part: inputParts) {            
            if (type_name.matches("Integer")){
                out.add(type.cast(Integer.parseInt(part)));
            }
            else if (type_name.matches("Double")){
                out.add(type.cast(Double.parseDouble(part)));
            }
            else if (type_name.matches("Float")){
                out.add(type.cast(Float.parseFloat(part)));
            }
            else {
                throw new RuntimeException("could not convert the string");
            }            
        }
        return out;
    }
    
    public static <T> String MatrixToString(List<List<T>> matrix, String formatString, String delimiter, String EOL) {
        // build delimter separated list of values
        StringWriter writer = new StringWriter();
        for (List<T> row: matrix) {
            for (T item: row) {                
                 writer.append(String.format(formatString + delimiter, item));
            }
            // end of line character
             writer.append(EOL);
        }        
        return writer.toString();
    }
    
    public static <T> String VectorToString(List<T> vector, String formatString, String delimiter) {
        // build delimter separated list of values
        StringWriter writer = new StringWriter();
        for (T item: vector) {            
             writer.append(String.format(formatString + delimiter, item));
        }              
        return writer.toString();
    }
    
    public static void createFolder(String folder) {
        try {            
            // create the folder if needed
            File dir = new File(folder);
            if (!dir.exists())
            {
                FileUtils.forceMkdir(dir);
            }            
            
        }
        catch (IOException exp){
            throw new RuntimeException("Could not create new folder");
        }
    }
    

}
