package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class Homography {
       
    List<List<Double>> in;
    List<List<Double>> out;
    int n;
    RealMatrix A;
    RealVector B;
    RealVector X;
    double f0, f1, f2, f3, f4, f5, f6, f7, f8;
    double b0, b1, b2, b3, b4, b5, b6, b7, b8;

    public Homography(List<List<Double>> in, List<List<Double>> out)
    {
        this.in = in;
        this.out = out;
        this.n = in.size();
        A = createA(in, out);
        B = createB(out);
        X = computeX(A, B);
        
        // forward projection coefficients
        f0 = X.getEntry(0);
        f1 = X.getEntry(1);
        f2 = X.getEntry(2);
        f3 = X.getEntry(3);
        f4 = X.getEntry(4);
        f5 = X.getEntry(5);
        f6 = X.getEntry(6);
        f7 = X.getEntry(7);
        f8 = 1.0;
        
        // backward projection coefficients
        b0 = f4 - f5*f7;
        b1 = f2*f7 - f1;
        b2 = f1*f5 - f2*f4;
        b3 = f5*f6 - f3;
        b4 = f0 - f2*f6;
        b5 = f2*f3 - f0*f5;
        b6 = f3*f7 - f4*f6;
        b7 = f1*f6 - f0*f7;
        b8 = f0*f4 - f1*f3;
    }
    
    private RealMatrix createA(List<List<Double>> in, List<List<Double>> out)
    {
        RealMatrix matrixA = MatrixUtils.createRealMatrix(2*n, 8);
        for (int i = 0; i < n; i++)
        {
            matrixA.setEntry(i, 0, in.get(i).get(0));
            matrixA.setEntry(i, 1, in.get(i).get(1));
            matrixA.setEntry(i, 2, 1.0);
            matrixA.setEntry(i, 3, 0.0);
            matrixA.setEntry(i, 4, 0.0);
            matrixA.setEntry(i, 5, 0.0);
            matrixA.setEntry(i, 6, -1*in.get(i).get(0)*out.get(i).get(0));
            matrixA.setEntry(i, 7, -1*in.get(i).get(1)*out.get(i).get(0));
            
            matrixA.setEntry(i+n, 0, 0.0);
            matrixA.setEntry(i+n, 1, 0.0);
            matrixA.setEntry(i+n, 2, 0.0);
            matrixA.setEntry(i+n, 3, in.get(i).get(0));
            matrixA.setEntry(i+n, 4, in.get(i).get(1));
            matrixA.setEntry(i+n, 5, 1.0);
            matrixA.setEntry(i+n, 6, -1*in.get(i).get(0)*out.get(i).get(1));
            matrixA.setEntry(i+n, 7, -1*in.get(i).get(1)*out.get(i).get(1));
        }
        return matrixA;
    }
    
    private RealVector createB(List<List<Double>> out)
    {
        double[] bData = new double[2*n];
        for (int i = 0; i < n; i++)
        {
            bData[i] = out.get(i).get(0);
            bData[i+n] = out.get(i).get(1);
        }
        RealVector vectorB = MatrixUtils.createRealVector(bData);
        return vectorB;
    }
    
    private RealVector computeX(RealMatrix A, RealVector B)
    {       
        DecompositionSolver solver = new QRDecomposition(A).getSolver();
        RealVector vectorX = solver.solve(B);        
        return vectorX;
    }
    
    public List<List<Double>> projectForward(List<List<Double>> pts)
    {
        List<List<Double>> outPts = new ArrayList<>(pts.size());
        for (int i = 0; i < pts.size(); i++)
        {
            double x = pts.get(i).get(0);
            double y = pts.get(i).get(1);
            double outX = (f0*x + f1*y + f2)/(f6*x + f7*y + f8);
            double outY = (f3*x + f4*y + f5)/(f6*x + f7*y + f8);
            outPts.add(Arrays.asList(outX, outY));
        }        
        return outPts;
    }
    
    public List<List<Double>> projectBackward(List<List<Double>> pts)
    {
        List<List<Double>> outPts = new ArrayList<>(pts.size());
        for (int i = 0; i < pts.size(); i++)
        {
            double x = pts.get(i).get(0);
            double y = pts.get(i).get(1);
            double outX = (b0*x + b1*y + b2)/(b6*x + b7*y + b8);
            double outY = (b3*x + b4*y + b5)/(b6*x + b7*y + b8);
            outPts.add(Arrays.asList(outX, outY));
        }        
        return outPts;
    }
    
    public double computeForwardProjectionError()
    {
        List<List<Double>> newOut = projectForward(in);
        double errorSum = 0;
        for (int i = 0; i < n; i++)
        {
            Double newOutX = newOut.get(i).get(0);
            Double outX = out.get(i).get(0);
            Double newOutY = newOut.get(i).get(1);
            Double outY = out.get(i).get(1);
            errorSum += Math.sqrt(Math.pow(newOutX - outX, 2) + Math.pow(newOutY - outY,2));
        }
        double avgError = errorSum/n;
        return avgError;
    }
    
    public double computeBackwardProjectionError()
    {
        List<List<Double>> newIn = projectBackward(out);
        double errorSum = 0;
        for (int i = 0; i < n; i++)
        {
            Double newInX = newIn.get(i).get(0);
            Double inX = in.get(i).get(0);
            Double newInY = newIn.get(i).get(1);
            Double inY = in.get(i).get(1);
            errorSum += Math.sqrt(Math.pow(newInX - inX, 2) + Math.pow(newInY - inY,2));
        }
        double avgError = errorSum/n;
        return avgError;
    }
    
}
