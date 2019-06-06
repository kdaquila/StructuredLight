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
    
    public static List<List<Double>> projectPoints(List<List<Double>> xyzPts, List<List<Double>> K_matrix, List<List<Double>> RT_matrix, List<Double> radialDistCoeffs) {
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
        
        // Project to camera coordinates
        RealMatrix XY_unDistored_homog = RT.multiply(XYZ_homog_tr).transpose();
        
        // Convert to cartesian coordinates
        List<List<Double>> XY_unDistorted_cart = new ArrayList<>();
        for (double[] pt: XY_unDistored_homog.getData()) {
            XY_unDistorted_cart.add(CoordinateSystems.toCartesian(ArrayUtils.ArrayToList_Double(pt)));
        }
        
        // Apply distortions
        List<List<Double>> XY_distored_cart = new ArrayList<>();
        for (List<Double> pt: XY_unDistorted_cart) {
            double u = pt.get(0);
            double v = pt.get(1);
            double rSqr = Math.pow(u, 2) + Math.pow(v, 2);
            double D = radialDistCoeffs.get(0)*rSqr + radialDistCoeffs.get(1)*Math.pow(rSqr, 2);            
            double distortedU = u*(1 + D);
            double distortedV = v*(1 + D);
            List<Double> distortedPt = new ArrayList<>();
            distortedPt.add(distortedU);
            distortedPt.add(distortedV);
            XY_distored_cart.add(distortedPt);
        }
        
        // Convert to homogenous coordinates
        List<List<Double>> XY_distored_homog = new ArrayList<>(nPts);
        for (List<Double> pt: XY_distored_cart) {
            XY_distored_homog.add(CoordinateSystems.toHomog(pt));
        } 
        RealMatrix XY_distored_homog_trans = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(XY_distored_homog)).transpose();

        // Do the projection        
        RealMatrix UV_homog_project_trans = K.multiply(XY_distored_homog_trans);
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
