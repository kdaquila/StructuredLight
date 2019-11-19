package cameracalibration;

import core.ArrayUtils;
import java.util.List;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class IntrinsicMatrix {
        
    /**
     * This computes the camera intrinsic calibration matrix using a list of homography matrices.
     * @param symmetricMatrix The symmetric matrix "B" formed from the intrinsic camera matrix K 
     * according to [K^-t]*[K^-1]
     * @return The intrinsic camera calibration matrix "K"
     */
    public static List<List<Double>> compute(List<List<Double>> symmetricMatrix) {      
        
        RealMatrix B = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(symmetricMatrix));
                
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
