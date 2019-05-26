package testing_delimiter;

import java.util.List;
import structuredlightcommons.TXT;

public class test_fromString {

    public static void main(String[] args) {
        test_VectorFromString1();
        test_VectorFromString2();
        test_MatrixFromString1();
        test_MatrixFromString2();
    }
    
    public static void test_VectorFromString1(){
    System.out.println("Testing: VectorFromString(List<Double> ...)");
        String input = "1.0,2.0,3.0,4.0,5.0,6.0";
        List<Double> output = TXT.StringToVector(input, Double.class, ",");
        System.out.println(output);
    }
    
    public static void test_VectorFromString2(){
    System.out.println("Testing: VectorFromString(List<Integer> ...)");
        String input = "1,2,3,4,5,6";
        List<Integer> output = TXT.StringToVector(input, Integer.class, ",");
        System.out.println(output);
    }
    
    public static void test_MatrixFromString1(){
    System.out.println("Testing: MatrixFromString(List<List<Double>> ...");
        String input = "1.0,2.0,3.0,\n4.0,5.0,6.0,\n7.0,8.0,9.0,\n";
        List<List<Double>> output = TXT.StringToMatrix(input, Double.class, ",","\n");
        System.out.println(output);
    }
    
    public static void test_MatrixFromString2(){
    System.out.println("Testing: MatrixFromString(List<List<Integer>> ...");
        String input = "1,2,3,\n4,5,6,\n7,8,9,\n";
        List<List<Integer>> output = TXT.StringToMatrix(input, Integer.class, ",","\n");
        System.out.println(output);
    }
    
    
}
