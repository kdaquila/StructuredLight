package cameramatrix_development;

import core.ArrayUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class CameraMatrixDecomposition {
    
    private RealMatrix M; // full projection matrix
    private RealMatrix K; // instrinsic camera matrix
    private RealMatrix R; // rotation 
    private RealMatrix T; // translation
    private RealMatrix RT; // extrinsic camera matrix
    private RealMatrix C; // camera center
    
    public CameraMatrixDecomposition(double[][] matrix) {
        M = new Array2DRowRealMatrix(matrix);
    }
    
    public List<List<Double>> getK() {
        return ArrayUtils.ArrayToList_Double2D(K.getData());
    }
    
    public List<List<Double>> getR() {
        return ArrayUtils.ArrayToList_Double2D(R.getData());
    }
    
    public List<List<Double>> getT() {
        return ArrayUtils.ArrayToList_Double2D(T.getData());
    }
    
    public void decompose() {        
        List<List<Double>> M_list = ArrayUtils.ArrayToList_Double2D(M.getSubMatrix(0,2,0,2).getData());
        List<List<Double>> M_list_trans = ArrayUtils.flipud(M_list);
        RealMatrix M_trans = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(M_list_trans));
        QRDecomposition qr = new QRDecomposition(M_trans.transpose());
        List<List<Double>> R_list_trans = ArrayUtils.ArrayToList_Double2D(qr.getR().transpose().getData());        
        List<List<Double>> K_list = ArrayUtils.fliplr(ArrayUtils.flipud(R_list_trans));     
        K = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(K_list));
        double scaling = K.getEntry(2, 2);        
        K = K.scalarMultiply(1/scaling);
        List<List<Double>> Q_list_trans = ArrayUtils.ArrayToList_Double2D(qr.getQ().transpose().getData());
        List<List<Double>> R_list = ArrayUtils.flipud(Q_list_trans); 
        R = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(R_list));
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
