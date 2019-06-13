package cameracalibration;

import core.ArrayUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class ExtrinsicMatrix {   
    
    public static Map<String,List<List<Double>>> compute_batch(Map<String,List<List<Double>>> homographySet, List<List<Double>> intrinsicMatrix) {
        Map<String,List<List<Double>>> output = new HashMap<>();
        for (String name: homographySet.keySet()) {
            List<List<Double>> H = homographySet.get(name);
            List<List<Double>> RT = compute(H, intrinsicMatrix);
            output.put(name, RT);
        }
        return output;
    }
    
    public static List<List<Double>> compute(List<List<Double>> homographyMatrix, List<List<Double>> intrinsicMatrix) {
        // Store the intrinsic matrix 
        RealMatrix K = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(intrinsicMatrix));

        // Compute inverse of intrinsic matrix
        RealMatrix K_inv = (new LUDecomposition(K)).getSolver().getInverse();
        
        // Store the homography columns
        RealMatrix H = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(homographyMatrix));
        RealVector H0 = H.getColumnVector(0);
        RealVector H1 = H.getColumnVector(1);
        RealVector H2 = H.getColumnVector(2);

        // Compute C
        double C = 1.0/(K_inv.operate(H0).getNorm());
        
        // Compute R0, R1, R2, T
        RealVector R0 = K_inv.operate(H0).mapMultiply(C);
        RealVector R1 = K_inv.operate(H1).mapMultiply(C);
        RealVector R2 = MatrixUtils.createRealVector((new Vector3D(R0.toArray())).crossProduct(new Vector3D(R1.toArray())).toArray());
        RealVector T = K_inv.operate(H2).mapMultiply(C);
        
        // Compute R         
        RealMatrix R = MatrixUtils.createRealMatrix(3, 3);
        R.setColumnVector(0, R0);
        R.setColumnVector(1, R1);
        R.setColumnVector(2, R2);
        SingularValueDecomposition svd = new SingularValueDecomposition(R);
        R = svd.getU().multiply(svd.getVT());
        
        // Build the RT Matrix
        RealMatrix RT = MatrixUtils.createRealMatrix(3, 4);
        RT.setColumnVector(0, R.getColumnVector(0));
        RT.setColumnVector(1, R.getColumnVector(1));
        RT.setColumnVector(2, R.getColumnVector(2));
        RT.setColumnVector(3, T);
        
        return ArrayUtils.ArrayToList_Double2D(RT.getData());
    }

}
