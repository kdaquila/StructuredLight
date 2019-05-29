package curvefitting;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class Paraboloid {
    
    BufferedImage grayImg;
    
    public Paraboloid(BufferedImage grayImg) {
        this.grayImg = grayImg;
    }
    
    public List<Double> fit(int x, int y, int w, int h) {
        // Get the data from region of interest
        Raster regionRaster = grayImg.getData(new Rectangle(x, y, w, h));
        byte[] regionBytes = ((DataBufferByte) regionRaster.getDataBuffer()).getData();

        // Create Matrix A and Vector B
        double[][] a = new double[w*h][5];
        double[] b = new double[w*h];        
        for (int i = x; i < w + x; i++) {
            for (int j = y; j < h + y; j++) {
                int linInd = (j-y)*w + (i-x);
                int I = regionBytes[linInd] & 0xFF;
                a[linInd][0] = x*x;
                a[linInd][1] = y*y;
                a[linInd][2] = x;
                a[linInd][3] = y;
                a[linInd][4] = 1;
                b[linInd] = I;
            }
        }
        RealMatrix A = MatrixUtils.createRealMatrix(a);
        RealVector B = MatrixUtils.createRealVector(b);
        
        // Compute Vector X (from AX = B)
        RealVector X;
        try {
            DecompositionSolver solver = new SingularValueDecomposition(A).getSolver();            
            X = solver.solve(B);
            System.out.println("the condition number is: " + new SingularValueDecomposition(A).getConditionNumber());
        }
        catch (SingularMatrixException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Could not fit the paraboloid.");
        }
        
        // Extract the paraboloid parameters (A, B, x0, y0 from A(x-x0)^2 + B(y-y0)^2 = I)
        List<Double> coeffs = new ArrayList<>();
        coeffs.set(0, X.getEntry(0));
        coeffs.set(1, X.getEntry(1));
        coeffs.set(2, -X.getEntry(2)/(2*X.getEntry(0)));
        coeffs.set(3, -X.getEntry(3)/(2*X.getEntry(1)));
        
        return coeffs;
    }

}
