package cameracalibration;

import core.ArrayUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class IntrinsicMatrix {
        
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
        
        // Build vector B
        RealVector B = MatrixUtils.createRealVector(new double[2*nViews]);
        for (int view_num = 0; view_num < nViews; view_num++) {
            int i = view_num*2;
            B.setEntry(i, 0.0);
            B.setEntry(i+1, 0.0);            
        }
        
        // Compute vector X (AX=B)
        SingularValueDecomposition decomp = new SingularValueDecomposition(A);
        RealMatrix V = decomp.getV();
        RealVector X = V.getColumnVector(V.getColumnDimension() - 1);    
        double[] x = X.toArray();
        
        // Compute the intrinsic matrix values
        RealMatrix K = MatrixUtils.createRealMatrix(3, 3);
        
        double w = x[0]*x[2]*x[5] - Math.pow(x[1],2)*x[5] - x[0]*Math.pow(x[4],2) + 2*x[1]*x[3]*x[4] - x[2]*Math.pow(x[3],2);
        double d = x[0]*x[2] - Math.pow(x[1], 2);
        
        double alpha = Math.sqrt(w/(d*x[0]));
        double beta  = Math.sqrt(w/Math.pow(d,2)*x[0]);
        double gamma = Math.sqrt(w/(Math.pow(d,2)*x[0]))*x[1];
        double uc = (x[1]*x[4] - x[2]*x[3])/d;
        double vc = (x[1]*x[3] - x[0]*x[4])/d;
        
        K.setEntry(0, 0, alpha);
        K.setEntry(0, 1, gamma);
        K.setEntry(0, 2, uc);
        K.setEntry(1, 0, 0.0);
        K.setEntry(1, 1, beta);
        K.setEntry(1, 2, vc);
        K.setEntry(2, 0, 0.0);
        K.setEntry(2, 1, 0.0);
        K.setEntry(2, 2, 1.0);
        
        // TODO remove after debug
        System.out.println("Matrix A: ");
        ArrayUtils.printList_Double2D(ArrayUtils.ArrayToList_Double2D(A.getData()), "%+07e");
        
        System.out.println("Matrix K: ");
        ArrayUtils.printList_Double2D(ArrayUtils.ArrayToList_Double2D(K.getData()), "%+010.3f");
        
        
        // Report to console        
        System.out.println("The condition number is : " + String.format("%.3e", decomp.getConditionNumber()));
        System.out.println(X);
        
        
        return new ArrayList<>();
    }
}
