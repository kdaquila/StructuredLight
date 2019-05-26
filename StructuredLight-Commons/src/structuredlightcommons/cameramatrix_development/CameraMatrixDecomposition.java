package cameracalibration;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class CameraMatrixDecomposition {
    
    RealMatrix M; // full projection matrix
    RealMatrix K; // instrinsic camera matrix
    RealMatrix R; // rotation 
    RealMatrix T; // translation
    RealMatrix RT; // extrinsic camera matrix
    RealMatrix C; // camera center
    
    public CameraMatrixDecomposition(double[][] matrix) {
        M = new Array2DRowRealMatrix(matrix);
    }
    
    public void decompose() {
        QRDecomposition qr = new QRDecomposition(MatrixUtil.flipud(M.getSubMatrix(0, 2, 0, 2)).transpose());
        K = MatrixUtil.fliplr(MatrixUtil.flipud(qr.getR().transpose()));                
        double scaling = K.getEntry(2, 2);        
        K = K.scalarMultiply(1/scaling);
        R = MatrixUtil.flipud(qr.getQ().transpose()); 
        R = R.scalarMultiply(scaling);        
        T = (new QRDecomposition(K)).getSolver().getInverse().multiply(M.getSubMatrix(0, 2, 3, 3));
        RT = new Array2DRowRealMatrix(3, 4);
        RT.setSubMatrix(R.getData(), 0, 0);
        RT.setSubMatrix(T.getData(), 0, 3);        
        C = (new QRDecomposition(R)).getSolver().getInverse().multiply(T.scalarMultiply(-1));
    }
    
    public double getFocalLengthX_px() {
        return K.getEntry(0, 0);
    }
    
    public double getFocalLengthY_px() {
        return K.getEntry(1, 1);
    }
    
    public double getPrincipalPointX_px() {
        return K.getEntry(0, 2);
    }
    
    public double getPrincipalPointY_px() {
        return K.getEntry(1, 2);
    }
    
    public List<Double> getCameraPos_mm() {
        List<Double> output = new ArrayList<>(3);
        for (int i = 0; i < C.getRowDimension(); i++) {
            output.add(C.getEntry(i, 0));
        }
        return output;
    }


}
