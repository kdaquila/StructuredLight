package cameracalibration;

import core.ArrayUtils;
import core.CoordinateSystems;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class Projection {
    
    public static List<List<Double>> projectPoints(List<List<Double>> xyzPts, List<List<Double>> K_matrix, List<List<Double>> RT_matrix) {
        int nPts = xyzPts.size();

        // Convert to homogenous coordinates
        List<List<Double>> xyzPts_homog = new ArrayList<>(nPts);
        for (List<Double> pt: xyzPts) {
            xyzPts_homog.add(CoordinateSystems.toHomog(pt));
        }                

        // Set XYZ points as Matrix
        RealMatrix XYZ_homog = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(xyzPts_homog));
        RealMatrix XYZ_homog_tr = XYZ_homog.transpose();     
        
        // Store the matrices
        RealMatrix RT = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(RT_matrix));
        RealMatrix K = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(K_matrix));

        // Do the projection        
        RealMatrix UV_homog_project_trans = K.multiply(RT.multiply(XYZ_homog_tr));
        RealMatrix UV_homog_project = UV_homog_project_trans.transpose();

        // Convert to cartesian coordinates
        List<List<Double>> uvPts_proj = new ArrayList<>(nPts);
        for (double[] pt: UV_homog_project.getData()) {
            List<Double> pt_list = ArrayUtils.ArrayToList_Double(pt);
            uvPts_proj.add(CoordinateSystems.toCartesian(pt_list));
        }  
        
        return uvPts_proj;
    }

}
