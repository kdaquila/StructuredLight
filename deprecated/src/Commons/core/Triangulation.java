package core;

import java.util.List;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class Triangulation {
    
    public static List<Double> computeCameraPosition(List<List<Double>> externalR,
                                                     List<Double> externalT)
    {
        // convert to rotation matrix
        double[][] R_Array2D = ArrayUtils.ListToArray_Double2D(externalR);
        RealMatrix R = MatrixUtils.createRealMatrix(R_Array2D);
        
        // convert to translation vector
        double[] T_Array1D = ArrayUtils.ListToArray_Double(externalT);
        RealVector T = MatrixUtils.createRealVector(T_Array1D);
        
        // solve for camera position (use decomposition)
//        DecompositionSolver solver = new SingularValueDecomposition(R).getSolver();
//        RealVector X = solver.solve(T.mapMultiply(-1));

        // solve for camera position (use inverse of R as just transpose of R)
        // X = [R^-1]*[-T]
        RealVector XYZ = R.transpose().operate(T.mapMultiply(-1));
        
        // convert to list
        double[] XYZ_Array = XYZ.toArray();
        List<Double> XYZ_List = ArrayUtils.ArrayToList_Double(XYZ_Array);
        
        return XYZ_List;
    }
    
    public static List<Double> computeCameraRay(List<Double> pixelPos,
                                                List<List<Double>> internalK, 
                                                List<List<Double>> externalR)
    {
        // compute the inverse of K
        double[][] K_Array2D = ArrayUtils.ListToArray_Double2D(internalK);
        RealMatrix K = MatrixUtils.createRealMatrix(K_Array2D);
        RealMatrix K_inv = MatrixUtils.createRealMatrix(3, 3);
        double fx = K.getEntry(0,0);
        double fy = K.getEntry(1,1);
        double cx = K.getEntry(0,2);
        double cy = K.getEntry(1,2);
        K_inv.setEntry(0, 0, 1.0/fx);
        K_inv.setEntry(0, 1, 0.0);
        K_inv.setEntry(0, 2, -cx/fx);
        K_inv.setEntry(1, 0, 0.0);
        K_inv.setEntry(1, 1, 1.0/fy);
        K_inv.setEntry(1, 2, -cy/fy);
        K_inv.setEntry(2, 0, 0.0);
        K_inv.setEntry(2, 1, 0.0);
        K_inv.setEntry(2, 2, 1.0);
        
        // compute the inverse of R
        // [R^-1] = [R^T]
        double[][] R_Array2D = ArrayUtils.ListToArray_Double2D(externalR);
        RealMatrix R = MatrixUtils.createRealMatrix(R_Array2D);
        RealMatrix R_inv = R.transpose();
        
        // store the pixel in homogenous coordinates
        double[] pixel_Array = ArrayUtils.ListToArray_Double(pixelPos);
        double[] pixel_Array_homog = new double[pixel_Array.length+1];
        pixel_Array_homog[2] = 1;
        RealVector pixel = MatrixUtils.createRealVector(pixel_Array_homog);
        
        // compute the direction vector of the ray passing through the pixel
        // [XYZ] = [R^-1][K^-1][uv1]
        RealVector XYZ = R_inv.multiply(K_inv).operate(pixel);

        // convert to list
        double[] XYZ_Array1D = XYZ.toArray();
        List<Double> XYZ_List = ArrayUtils.ArrayToList_Double(XYZ_Array1D);
        
        return XYZ_List;
    }       
    
    public static List<Double> computeLineIntersect(List<Double> point1, List<Double> ray1,
                                                    List<Double> point2, List<Double> ray2)
    {   
        // define points and rays as vectors
        Vector3D r1 = new Vector3D(ArrayUtils.ListToArray_Double(ray1));
        Vector3D r2 = new Vector3D(ArrayUtils.ListToArray_Double(ray2));
        Vector3D p1 = new Vector3D(ArrayUtils.ListToArray_Double(point1));
        Vector3D p2 = new Vector3D(ArrayUtils.ListToArray_Double(point2));
        Vector3D r3 = p2.subtract(p1);
        
        // compute line parameter "a"
        // such that pt = a*ray1 + point1
        double a = (r1.dotProduct(r2) * r2.dotProduct(r3) - r1.dotProduct(r3) * r2.dotProduct(r2))/
                   (r1.dotProduct(r1) * r2.dotProduct(r2) - r1.dotProduct(r2) * r1.dotProduct(r2));
                
        // compute intersection point
        // such that pt = a*ray1 + point1
        Vector3D XYZ = r1.scalarMultiply(a).add(p1);
        
        // store as List
        List<Double> XYZ_List = ArrayUtils.ArrayToList_Double(XYZ.toArray());
        
        return XYZ_List;
    }

}
