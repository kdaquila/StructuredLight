package cameracalibration;

import core.ArrayUtils;
import java.util.List;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class SymmetricMatrix {
    
    public static List<List<Double>> compute(List<List<List<Double>>> homographies) {
        
        int nViews = homographies.size();
        
        // Build matrix A
        RealMatrix A = MatrixUtils.createRealMatrix(2*nViews, 6);
        for (int view_num = 0; view_num < nViews; view_num++) {
            
            // Reformat the homography as 3x3
            double[][] H = ArrayUtils.ListToArray_Double2D(ArrayUtils.reshape(homographies.get(view_num), 3, 3));
            
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
        RealVector X = V.getColumnVector(V.getColumnDimension() - 1);   
        RealMatrix B = MatrixUtils.createRealMatrix(3,3);
        B.setEntry(0, 0, X.getEntry(0));
        B.setEntry(0, 1, X.getEntry(1));
        B.setEntry(0, 2, X.getEntry(3));
        B.setEntry(1, 0, X.getEntry(1));
        B.setEntry(1, 1, X.getEntry(2));
        B.setEntry(1, 2, X.getEntry(4));
        B.setEntry(2, 0, X.getEntry(3));
        B.setEntry(2, 1, X.getEntry(4));
        B.setEntry(2, 2, X.getEntry(5));
        
        // Report to console        
        System.out.println("The condition number for Symmetric Matrix is : " + String.format("%.3e", decomp.getConditionNumber()));             

        return ArrayUtils.ArrayToList_Double2D(B.getData());
    }
    
}
