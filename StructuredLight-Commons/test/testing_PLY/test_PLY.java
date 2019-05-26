package testing_PLY;

import java.util.ArrayList;
import java.util.List;
import structuredlightcommons.PLY;

public class test_PLY {

    public static void main(String[] args) {
        test_saveVerts();
    }
    
    public static void test_saveVerts()
    {
        System.out.println("Testing: saveVerts(List<List<Double>> ... )");
        
        // create some data
        List<List<Double>> matrix = new ArrayList<>();
        Double k = 0.0;
        for (int i = 0; i < 4; i++){
            matrix.add(new ArrayList<>());
            for (int j = 0; j < 3; j++){
                matrix.get(i).add(k++);
            }
        }
        
        // write as PLY
        String folder = "C:\\Users\\kfd18\\Downloads";
        String filename = "data.ply";
        PLY.saveVerts(matrix, folder, filename, true);
    }    

}
