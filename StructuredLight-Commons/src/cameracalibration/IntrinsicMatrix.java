package cameracalibration;

import core.ArrayUtils;
import homography.NormalizationMatrix;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class IntrinsicMatrix {
        
    public static List<List<Double>> compute(List<List<List<Double>>> homographies_norm, List<List<Double>> N_uv_inv) {
        
        int nViews = homographies_norm.size();
        
        // Build matrix A
        RealMatrix A = MatrixUtils.createRealMatrix(2*nViews, 6);
        for (int view_num = 0; view_num < nViews; view_num++) {
            
            // Reformat the homography as 3x3
            double[][] H = ArrayUtils.ListToArray_Double2D(ArrayUtils.reshape(homographies_norm.get(view_num), 3, 3));
            
            int i = view_num*2;
            
            A.setEntry(i, 0, H[0][0]*H[0][1]);
            A.setEntry(i, 1, H[0][0]*H[1][1] + H[1][0]*H[0][1]);
            A.setEntry(i, 2, H[1][0]*H[1][1]);
            A.setEntry(i, 3, H[2][0]*H[0][1] + H[0][0]*H[2][1]);
            A.setEntry(i, 4, H[2][0]*H[1][1] + H[1][0]*H[2][1]);
            A.setEntry(i, 5, H[2][0]*H[2][1]);
            
            A.setEntry(i+1, 0, (H[0][0]*H[0][0]) - (H[0][1]*H[0][1]));
            A.setEntry(i+1, 1, (H[0][0]*H[1][0] + H[1][0]*H[0][0]) - (H[0][1]*H[1][1] + H[1][1]*H[0][1]));
            A.setEntry(i+1, 2, (H[1][0]*H[1][0]) - (H[1][1]*H[1][1]));
            A.setEntry(i+1, 3, (H[2][0]*H[0][0] + H[0][0]*H[2][0]) - (H[2][1]*H[0][1] + H[0][1]*H[2][1]));
            A.setEntry(i+1, 4, (H[2][0]*H[1][0] + H[1][0]*H[2][0]) - (H[2][1]*H[1][1] + H[1][1]*H[2][1]));
            A.setEntry(i+1, 5, (H[2][0]*H[2][0]) - (H[2][1]*H[2][1]));
        }
        
        // Compute vector X (AX=zero)
        SingularValueDecomposition decomp = new SingularValueDecomposition(A);
        RealMatrix V = decomp.getV();
        RealVector X_norm = V.getColumnVector(V.getColumnDimension() - 1);    
        List<List<Double>> X_list_norm = ArrayUtils.reshape(ArrayUtils.addDim_1Dto2D(ArrayUtils.ArrayToList_Double(X_norm.toArray())), 3, 3);
        RealMatrix B_norm = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(X_list_norm));
        
        // Report to console        
        System.out.println("The condition number is : " + String.format("%.3e", decomp.getConditionNumber())); 
        
        // Denormalize B
        RealMatrix N_inv = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(N_uv_inv));    
        RealMatrix B = N_inv.transpose().multiply(B_norm.multiply(N_inv));
        
        // Get B's data in array for convenience
        double[] b = new double[6];
        b[0] = B.getEntry(0, 0);
        b[1] = B.getEntry(0, 1);
        b[2] = B.getEntry(1, 1);
        b[3] = B.getEntry(0, 2);
        b[4] = B.getEntry(1, 2);
        b[5] = B.getEntry(2, 2);
        
        // Compute the intrinsic matrix parameters  
        double w = b[0]*b[2]*b[5] - Math.pow(b[1],2)*b[5] - b[0]*Math.pow(b[4],2) + 2*b[1]*b[3]*b[4] - b[2]*Math.pow(b[3],2);
        double d = b[0]*b[2] - Math.pow(b[1], 2);        
        double alpha = Math.sqrt(w/(d*b[0]));
        double beta  = Math.sqrt(w/Math.pow(d,2)*b[0]);
        double gamma = Math.sqrt(w/(Math.pow(d,2)*b[0]))*b[1];
        double uc = (b[1]*b[4] - b[2]*b[3])/d;
        double vc = (b[1]*b[3] - b[0]*b[4])/d;            
        
        // Compute the intrinsic matrix values
        RealMatrix K = MatrixUtils.createRealMatrix(3, 3); 
        K.setEntry(0, 0, alpha);
        K.setEntry(0, 1, gamma);
        K.setEntry(0, 2, uc);
        K.setEntry(1, 0, 0.0);
        K.setEntry(1, 1, beta);
        K.setEntry(1, 2, vc);
        K.setEntry(2, 0, 0.0);
        K.setEntry(2, 1, 0.0);
        K.setEntry(2, 2, 1.0);

        return ArrayUtils.ArrayToList_Double2D(K.getData());
    }
}
