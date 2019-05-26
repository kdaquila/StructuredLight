package structuredlightcommons;

import java.util.List;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class FundamentalMatrix
{   
    public RealMatrix F;
    
    public FundamentalMatrix(List<List<Double>> internalK1, List<List<Double>> externalR1, List<Double> externalT1,
                             List<List<Double>> internalK2, List<List<Double>> externalR2, List<Double> externalT2)
    {
        // store as vectors and matrices
        RealMatrix K1 = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(internalK1));
        RealMatrix R1 = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(externalR1));
        RealVector T1 = MatrixUtils.createRealVector(ArrayUtils.ListToArray_Double(externalT1));
        RealMatrix K2 = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(internalK2));
        RealMatrix R2 = MatrixUtils.createRealMatrix(ArrayUtils.ListToArray_Double2D(externalR2));
        RealVector T2 = MatrixUtils.createRealVector(ArrayUtils.ListToArray_Double(externalT2));
        
        // compute camera 1 position C in homogenous coordinates
        RealVector C1 =  R1.transpose().operate(T1.mapMultiply(-1));        
        double[] C1_Array = C1.toArray();
        double[] C1_Array_homog = new double[C1_Array.length+1];
        C1_Array_homog[3] = 1;
        RealVector C1_homog = MatrixUtils.createRealVector(C1_Array_homog);
        
        // compute the reverse projection for camera 1 (i.e. camera to world)
        RealMatrix M1 = MatrixUtils.createRealMatrix(3, 3);
        M1.setColumn(0, R1.getColumn(0));
        M1.setColumn(1, R2.getColumn(1));
        M1.setColumn(2, R2.getColumn(2));
        M1.setColumn(3, T1.toArray());
        RealMatrix P1 = K1.multiply(M1);        
        DecompositionSolver solver = new SingularValueDecomposition(P1).getSolver();
        RealMatrix P1rev = solver.getInverse();
        
        // compute the forward projection for camera 2 (i.e. world to camera)
        RealMatrix M2 = MatrixUtils.createRealMatrix(3, 4);
        M2.setSubMatrix(R2.getData(), 0, 0);
        M2.setColumn(3, T2.toArray());
        RealMatrix P2 = K2.multiply(M2);
        
        // setup the "skew symmetric matrix" P2C1x        
        RealVector P2C1 = P2.operate(C1_homog);
        RealMatrix P2C1x = MatrixUtils.createRealMatrix(3, 3);
        P2C1x.setEntry(0, 0, 0.0);
        P2C1x.setEntry(0, 1, -P2C1.getEntry(2));
        P2C1x.setEntry(0, 2, P2C1.getEntry(1));
        P2C1x.setEntry(1, 0, P2C1.getEntry(2));
        P2C1x.setEntry(1, 1, 0.0);
        P2C1x.setEntry(1, 2, -P2C1.getEntry(0));
        P2C1x.setEntry(2, 0, -P2C1.getEntry(1));
        P2C1x.setEntry(2, 1, P2C1.getEntry(0));
        P2C1x.setEntry(2, 2, 0.0);
        
        // compute the fundamental matrix
        // [P2*C]x * P2 * P1rev
        F = P2C1x.multiply(P2).multiply(P1rev);        
    }
    
    public List<List<Double>> getF()
    {
        // convert to list
        double[][] F_Array = F.getData();
        List<List<Double>> F_List = ArrayUtils.ArrayToList_Double2D(F_Array);
        
        return F_List;
    }
    
    public List<Double> getEpipolarLineCoefficients(List<Double> pixel)
    {
        // multiply fundamental matrix with input point
        RealVector pix = MatrixUtils.createRealVector(ArrayUtils.ListToArray_Double(pixel));
        RealVector coeffs = F.operate(pix);
        
        // convert to list
        List<Double> coeffs_List = ArrayUtils.ArrayToList_Double(coeffs.toArray());
        
        return coeffs_List;
        
    }

}
