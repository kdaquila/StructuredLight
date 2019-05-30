package curvefitting;

import core.ArrayUtils;
import core.HomogCoords;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    public Map<String,Double> fit(int centerX, int centerY, int w, int h) {
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

        // compute the means for normalization
        double xSum = 0;
        double ySum = 0;
        double iSum = 0;
        for (int k = 0; k < w*h; k++) {
            xSum += xArray[k];
            ySum += yArray[k];
            iSum += iArray[k];
        }
        double xMean = xSum/(w*h);
        double yMean = ySum/(w*h);
        double iMean = iSum/(w*h);
        
        // compute the standard deviations for normalization
        double xSumSqr = 0;
        double ySumSqr = 0;
        double iSumSqr = 0;
        for (int k = 0; k < w*h; k++) {
            xSumSqr += Math.pow(xArray[k] - xMean, 2);
            ySumSqr += Math.pow(yArray[k] - yMean, 2);
            iSumSqr += Math.pow(iArray[k] - iMean, 2);
        }
        double xVar= xSumSqr/(w*h);
        double yVar = ySumSqr/(w*h);
        double iVar= iSumSqr/(w*h);
        double xStd = Math.sqrt(xVar);
        double yStd = Math.sqrt(yVar);
        double iStd = Math.sqrt(iVar);
        
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
        double condNum = 0;
        try {
            SingularValueDecomposition svd = new SingularValueDecomposition(A_norm);            
            condNum = svd.getConditionNumber();
            X_norm = svd.getSolver().solve(B);
        }
        catch (SingularMatrixException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Could not fit the paraboloid.");
        }
        
        // Compute the error
        RealVector B_compute = A_norm.operate(X_norm);
        RealVector errorVec = B.subtract(B_compute);     
        double residual = errorVec.getNorm();
        double rSqr = 1 - Math.pow(residual, 2)/iVar;

                
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
        
        // store the output
        Map<String, Double> output = new HashMap<>();
        output.put("A", coeffs.get(0));
        output.put("B", coeffs.get(1));
        output.put("x0", coeffs.get(2));
        output.put("y0", coeffs.get(3));
        output.put("condNum", condNum);
        output.put("residual", residual);
        output.put("rSqr", rSqr);
        
        return output;
    }

}
