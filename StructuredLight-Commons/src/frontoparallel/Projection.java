package frontoparallel;

import core.ArrayUtils;
import core.CoordinateSystems;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class Projection {
    
    public static List<List<Double>> toImagePlane(List<List<Double>> xyPts, List<List<Double>> K_matrix, List<List<Double>> RT_matrix, List<Double> radialDistCoeffs) {
        int nPts = xyPts.size();
        
        // Convert to homogenous coordinates
        List<List<Double>> xyPts_homog = new ArrayList<>(nPts);
        for (List<Double> pt: xyPts) {
            xyPts_homog.add(CoordinateSystems.toHomog(pt));
        }                

        // Set XYZ points as Matrix
        RealMatrix XY_homog = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(xyPts_homog));
        RealMatrix XY_homog_tr = XY_homog.transpose();     
        
        // Store the matrices
        RealMatrix RT_full = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(RT_matrix));
        RealMatrix K = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(K_matrix));
        
        // Remove the third column of R
        RealMatrix RT = MatrixUtils.createRealMatrix(3, 3);
        RT.setColumnVector(0, RT_full.getColumnVector(0));
        RT.setColumnVector(1, RT_full.getColumnVector(1));
        RT.setColumnVector(2, RT_full.getColumnVector(3));
        
        // Project to camera coordinates
        RealMatrix XY_unDistored_homog = RT.multiply(XY_homog_tr).transpose();
        
        // Convert to cartesian coordinates
        List<List<Double>> XY_unDistorted_cart = new ArrayList<>();
        for (double[] pt: XY_unDistored_homog.getData()) {
            XY_unDistorted_cart.add(CoordinateSystems.toCartesian(ArrayUtils.ArrayToList_Double(pt)));
        }
        
        // Apply distortions
        List<List<Double>> XY_distored_cart = new ArrayList<>();
        for (List<Double> pt: XY_unDistorted_cart) {
            double x = pt.get(0);
            double y = pt.get(1);
            double rSqr = Math.pow(x, 2) + Math.pow(y, 2);
            double k0 = radialDistCoeffs.get(0);
            double k1 = radialDistCoeffs.get(1);
            double D = k0*rSqr + k1*Math.pow(rSqr, 2);            
            double distortedX = x*(1 + D);
            double distortedY = y*(1 + D);
            List<Double> distortedPt = new ArrayList<>();
            distortedPt.add(distortedX);
            distortedPt.add(distortedY);
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
            uvPts_proj.add(CoordinateSystems.toCartesian(ArrayUtils.ArrayToList_Double(pt)));
        }  
        
        return uvPts_proj;
    }
    
    public static List<List<Double>> toWorldPlane(List<List<Double>> uvPts, List<List<Double>> K_matrix, List<List<Double>> RT_matrix, List<Double> radialDistCoeffs) {
        int nPts = uvPts.size();
        
        // Convert to homogenous coordinates
        List<List<Double>> uvPts_homog = new ArrayList<>(nPts);
        for (List<Double> pt: uvPts) {
            uvPts_homog.add(CoordinateSystems.toHomog(pt));
        }
        RealMatrix uvPts_homog_vec = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(uvPts_homog));
                 
        // Project to Camera coordinates
        RealMatrix K = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(K_matrix));
        SingularValueDecomposition svd_K = new SingularValueDecomposition(K);
        RealMatrix K_inv = svd_K.getSolver().getInverse();
        RealMatrix xy_distored_homog = K_inv.multiply(uvPts_homog_vec.transpose()).transpose();
        
        // Convert to cartesian coordinates
        List<List<Double>> xy_distorted_cart = new ArrayList<>();
        for (double[] pt: xy_distored_homog.getData()) {
            xy_distorted_cart.add(CoordinateSystems.toCartesian(ArrayUtils.ArrayToList_Double(pt)));
        }
        
        // Remove distortions
        List<List<Double>> xy_unDistored_cart = new ArrayList<>();
        for (List<Double> pt: xy_distorted_cart) {
            double x_dist = pt.get(0);
            double y_dist = pt.get(1);
            double r_dist = Math.sqrt(Math.pow(x_dist, 2) + Math.pow(y_dist, 2)); 
            double k0 = radialDistCoeffs.get(0);
            double k1 = radialDistCoeffs.get(1);
            
            double[] polyCoeffs = {-r_dist, 1, 0, k0, 0, k1};
            NewtonRaphsonSolver newton = new NewtonRaphsonSolver();
            PolynomialFunction polyFunc = new PolynomialFunction(polyCoeffs);
            int maxEval = 20;
            double r = newton.solve(maxEval, polyFunc, r_dist);            
            
            double x = x_dist*r/r_dist;
            double y = y_dist*r/r_dist;
            List<Double> unDistortedPt = new ArrayList<>();
            unDistortedPt.add(x);
            unDistortedPt.add(y);
            xy_unDistored_cart.add(unDistortedPt);
        }
        
        // Convert to homogenous coordinates
        List<List<Double>> xy_unDistored_homog = new ArrayList<>(nPts);
        for (List<Double> pt: xy_unDistored_cart) {
            xy_unDistored_homog.add(CoordinateSystems.toHomog(pt));
        }
        RealMatrix xy_unDistored_homog_vec = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(xy_unDistored_homog));
        
        // Project to World Coordinates
        RealMatrix RT_full = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(RT_matrix));
        RealMatrix RT = MatrixUtils.createRealMatrix(3, 3);
        RT.setColumnVector(0, RT_full.getColumnVector(0));
        RT.setColumnVector(1, RT_full.getColumnVector(1));
        RT.setColumnVector(2, RT_full.getColumnVector(3));
        SingularValueDecomposition svd_RT = new SingularValueDecomposition(RT);
        RealMatrix RT_inv = svd_RT.getSolver().getInverse();
        RealMatrix xy_world_homog_vec = RT_inv.multiply(xy_unDistored_homog_vec.transpose()).transpose();
        List<List<Double>> xy_world_homog = ArrayUtils.ArrayToList_Double2D(xy_world_homog_vec.getData());
        
        // Convert to cartesian coordinates
        List<List<Double>> xy_world_cart = new ArrayList<>();
        for (List<Double> pt: xy_world_homog) {
            xy_world_cart.add(CoordinateSystems.toCartesian(pt));
        }
        
        return xy_world_cart;
    }
}
