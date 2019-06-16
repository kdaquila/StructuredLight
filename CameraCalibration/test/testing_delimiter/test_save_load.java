package testing_delimiter;

import java.util.ArrayList;
import java.util.List;
import core.TXT;

public class test_save_load {

    public static void main(String[] args) {
        test_saveMatrix_double();
        test_loadMatrix_double();
        test_saveMatrix_int();
        test_loadMatrix_int();
        
        
    }

    public static void test_saveMatrix_double(){
        System.out.println("Testing: saveMatrix(List<List<Double>> ...");
        List<List<Double>> matrix = new ArrayList<>();
        Double k = 0.0;
        for (int i = 0; i < 3; i++){
            matrix.add(new ArrayList<>());
            for (int j = 0; j < 3; j++){
                matrix.get(i).add(k++);
            }
        }
        String folder = "C:\\Users\\kfd18\\Downloads";
        String filename = "matrix_double.txt";
        TXT.saveMatrix(matrix, Double.class, folder, filename, "%.3f", ",", "\n", false);
        System.out.println("---matrix saved successfully.\n");
    }
    
    public static void test_saveMatrix_int(){
        System.out.println("Testing: saveMatrix(List<List<Integer>> ...");
        List<List<Integer>> matrix = new ArrayList<>();
        Integer k = 0;
        for (int i = 0; i < 3; i++){
            matrix.add(new ArrayList<>());
            for (int j = 0; j < 3; j++){
                matrix.get(i).add(k++);
            }
        }
        String folder = "C:\\Users\\kfd18\\Downloads";
        String filename = "matrix_integer.txt";
        TXT.saveMatrix(matrix, Integer.class, folder, filename, "%d", ",", "\n", false);
        System.out.println("---matrix saved successfully.\n");
    }
    
    public static void test_loadMatrix_int(){
        System.out.println("Testing: loadMatrix(List<List<Integer>> ...");
        String folder = "C:\\Users\\kfd18\\Downloads\\";
        String filename = "matrix_integer.txt";
        String path = folder + "\\" + filename;
        List<List<Integer>> matrix = TXT.loadMatrix(path, Integer.class, ",", "\n");
        System.out.println(TXT.MatrixToString(matrix, "%d", ",", "\n"));
        System.out.println();
    }
    
    public static void test_loadMatrix_double(){
        System.out.println("Testing: loadMatrix(List<List<Double>> ...");
        String folder = "C:\\Users\\kfd18\\Downloads\\";
        String filename = "matrix_double.txt";
        String path = folder + "\\" + filename;
        List<List<Double>> matrix = TXT.loadMatrix(path, Double.class, ",", "\n");
        System.out.println(TXT.MatrixToString(matrix, "%.2f", ",", "\n"));
        System.out.println();
    }
}
