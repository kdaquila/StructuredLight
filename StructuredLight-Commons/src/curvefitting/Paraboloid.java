package curvefitting;

import core.ArrayUtils;
import core.HomogCoords;
import core.ImageUtil;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
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
    
    BufferedImage uShortGrayImg;
    
    public Paraboloid(BufferedImage uShortGrayImg) {
        this.uShortGrayImg = uShortGrayImg;
    }
    
    public List<Double> fit(int centerX, int centerY, int w, int h) {
        // Compute the top left point
        int x = centerX - w/2;
        int y = centerY - h/2;        
       
        // Get the data from region of interest
        Raster regionRaster = uShortGrayImg.getData(new Rectangle(x, y, w, h));
        short[] regionData = ((DataBufferUShort) regionRaster.getDataBuffer()).getData();
        
        // create x, y, and I value lists
        double[] xArray = new double[w*h];
        double[] yArray = new double[w*h];
        double[] iArray = new double[w*h];        
        for (int k = 0; k < w*h; k++) {
            int col = k%w;
            int row = k/w;
            int i = col + x;
            int j = row + y;
            xArray[k] = i;
            yArray[k] = j;
            iArray[k] = regionData[k] & 0xFFFF;            
        }

        // compute the means
        double xSum = 0;
        double ySum = 0;
        for (int k = 0; k < w*h; k++) {
            xSum += xArray[k];
            ySum += yArray[k];
        }
        double xMean = xSum/(w*h);
        double yMean = ySum/(w*h);
        
        // compute the standard deviations
        double xDiff = 0;
        double yDiff = 0;
        for (int k = 0; k < w*h; k++) {
            xDiff += Math.abs(xArray[k] - xMean);
            yDiff += Math.abs(yArray[k] - yMean);
        }
        double xStd = xDiff/(w*h);
        double yStd = yDiff/(w*h);
        
        // Create the XY normalization matrix        
        RealMatrix N = MatrixUtils.createRealMatrix(3, 3);
        double alphaX = Math.sqrt(2)/xStd;
        double alphaY = Math.sqrt(2)/yStd;
        double betaX = -alphaX*xMean;
        double betaY = -alphaY*yMean;
        N.setEntry(0, 0, alphaX);
        N.setEntry(0, 1, 0);
        N.setEntry(0, 2, betaX);
        N.setEntry(1, 0, 0);
        N.setEntry(1, 1, alphaY);
        N.setEntry(1, 2, betaY);
        N.setEntry(2, 0, 0);
        N.setEntry(2, 1, 0);
        N.setEntry(2, 2, 1);
        
        RealMatrix NInv = MatrixUtils.createRealMatrix(3, 3);
        NInv.setEntry(0, 0, 1/alphaX);
        NInv.setEntry(0, 1, 0);
        NInv.setEntry(0, 2, -betaX/alphaX);
        NInv.setEntry(1, 0, 0);
        NInv.setEntry(1, 1, 1/alphaY);
        NInv.setEntry(1, 2, -betaY/alphaY);
        NInv.setEntry(2, 0, 0);
        NInv.setEntry(2, 1, 0);
        NInv.setEntry(2, 2, 1);
                
        // Normalize the x and y arrays        
        double[] xArray_norm = new double[w*h];
        double[] yArray_norm = new double[w*h];
        for (int k = 0; k < w*h; k++) {
            List<Double> xy_cart = new ArrayList<>(2);
            xy_cart.add(xArray[k]);
            xy_cart.add(yArray[k]);
            List<Double> xy_homog = HomogCoords.toHomog_Double1D(xy_cart);
            RealVector XY_homog = MatrixUtils.createRealVector(ArrayUtils.ListToArray_Double(xy_homog));
            RealVector XY_homog_norm = N.operate(XY_homog);
            List<Double> xy_homog_norm = ArrayUtils.ArrayToList_Double(XY_homog_norm.toArray());
            List<Double> xy_cart_norm = HomogCoords.toCartesian_Double1D(xy_homog_norm);
            xArray_norm[k] = xy_cart_norm.get(0);
            yArray_norm[k] = xy_cart_norm.get(1);
        }    

        // Create Matrix A and Vector B
        double[][] a = new double[w*h][5];
        double[] b = new double[w*h];        
        for (int k = 0; k < w*h; k++) {
            a[k][0] = Math.pow(xArray_norm[k], 2);
            a[k][1] = Math.pow(yArray_norm[k], 2);
            a[k][2] = xArray_norm[k];
            a[k][3] = yArray_norm[k];
            a[k][4] = 1;
            b[k] = iArray[k];
        }
        RealMatrix A_norm = MatrixUtils.createRealMatrix(a);
        RealVector B = MatrixUtils.createRealVector(b);        
                
        // Compute Vector X (from AX = B)
        RealVector X_norm;        
        try {
            SingularValueDecomposition svd = new SingularValueDecomposition(A_norm);            
            double condNum = svd.getConditionNumber();
            System.out.println("the condition number is: " + String.format("%.3e", condNum));
            X_norm = svd.getSolver().solve(B);
        }
        catch (SingularMatrixException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Could not fit the paraboloid.");
        }                
                
        // Extract the paraboloid parameters (A, B, x0, y0 from A(x-x0)^2 + B(y-y0)^2 = I)
        List<Double> coeffs_norm = new ArrayList<>();
        coeffs_norm.add(X_norm.getEntry(0));
        coeffs_norm.add(X_norm.getEntry(1));
        coeffs_norm.add(-X_norm.getEntry(2)/(2*X_norm.getEntry(0)));
        coeffs_norm.add(-X_norm.getEntry(3)/(2*X_norm.getEntry(1)));
        
        // Convert back to un-normalized units
        List<Double> coeffs = new ArrayList<>();
        coeffs.add(coeffs_norm.get(0)*Math.pow(alphaX, 2));
        coeffs.add(coeffs_norm.get(1)*Math.pow(alphaY, 2));
        coeffs.add((coeffs_norm.get(2)-betaX)/alphaX);
        coeffs.add((coeffs_norm.get(3)-betaY)/alphaY);
 
// TODO remove debug info       
//        System.out.println("Matrix A: ");
//        ArrayUtils.printList_Double2D(ArrayUtils.ArrayToList_Double2D(A_norm.getData()));
//        System.out.println("Vector B: ");
//        System.out.println(ArrayUtils.ArrayToList_Double(B.toArray()));
//        System.out.println("Vector X: ");
//        System.out.println(ArrayUtils.ArrayToList_Double(X_norm.toArray()));
//        System.out.println("Paraboloid Normalized Coefficients: ");
//        System.out.println(coeffs_norm);
//        System.out.println("Paraboloid Regular Coefficients: ");
//        System.out.println(coeffs);
        
        return coeffs;
    }

}
