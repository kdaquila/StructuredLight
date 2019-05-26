package testing_delimiter;

import java.util.ArrayList;
import java.util.List;
import structuredlightcommons.TXT;

public class test_toString {

    public static void main(String[] args) {
        test_MatrixToString1();
        test_MatrixToString2();
        test_VectorToString1();
        test_VectorToString2();
    }   
       
    public static void test_MatrixToString1() {
        System.out.println("Testing: MatrixToString(List<List<Double>> ... )");
        List<List<Double>> matrix = new ArrayList<>();
        Double k = 0.0;
        for (int i = 0; i < 3; i++){
            matrix.add(new ArrayList<>());
            for (int j = 0; j < 3; j++){
                matrix.get(i).add(k++);
            }
        }
        String output = TXT.MatrixToString(matrix, "%.1f", ",", "\n");
        System.out.print(output);
        System.out.println();
    }
    
    public static void test_MatrixToString2() {
        System.out.println("Testing: MatrixToString(List<List<Integer>> ... )");
        List<List<Integer>> matrix = new ArrayList<>();
        Integer k = 0;
        for (int i = 0; i < 3; i++){
            matrix.add(new ArrayList<>());
            for (int j = 0; j < 3; j++){
                matrix.get(i).add(k++);
            }
        }
        String output = TXT.MatrixToString(matrix, "%d", ",", "\n");
        System.out.print(output);
        System.out.println();
    }
    
    public static void test_VectorToString1() {
        System.out.println("Testing: VectorToString(List<Double> ... )");
        List<Double> vector = new ArrayList<>();
        Double k = 0.0;
        for (int j = 0; j < 6; j++){
            vector.add(k++);
        }
        
        String output = TXT.VectorToString(vector, "%.1f", ",");
        System.out.print(output);
        System.out.println("\n");
    }
    
    public static void test_VectorToString2() {
        System.out.println("Testing: VectorToString(List<Integer> ... )");
        List<Integer> vector = new ArrayList<>();
        Integer k = 0;
        for (int j = 0; j < 6; j++){
            vector.add(k++);
        }
        String output = TXT.VectorToString(vector, "%d", ",");
        System.out.print(output);
        System.out.println("\n");
    }

}
