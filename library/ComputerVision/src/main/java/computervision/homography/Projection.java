package homography;

import core.ArrayUtils;
import core.CoordinateSystems;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class Projection {
    
    public static List<List<Double>> projectPoints(List<List<Double>> xyPts, List<List<Double>> homographyMatrix) {
        int nPts = xyPts.size();

        // Convert to homogenous coordinates
        List<List<Double>> xyPts_homog = new ArrayList<>(nPts);
        for (List<Double> pt: xyPts) {
            xyPts_homog.add(CoordinateSystems.toHomog(pt.subList(0, 2)));
        }                

        // Set XY points as Matrix
        RealMatrix XY_homog = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(xyPts_homog));
        RealMatrix XY_homog_tr = XY_homog.transpose();        

        // Do the projection
        RealMatrix H = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(homographyMatrix));
        RealMatrix UV_homog_project_trans = H.multiply(XY_homog_tr);
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
