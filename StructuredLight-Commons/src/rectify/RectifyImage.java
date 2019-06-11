package rectify;

import core.ArrayUtils;
import core.ImageUtils;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.analysis.interpolation.BicubicInterpolatingFunction;
import org.apache.commons.math3.analysis.interpolation.BicubicInterpolator;
import org.apache.commons.math3.linear.MatrixUtils;

public class RectifyImage {
    
    List<List<Double>> K_matrix;
    List<Double> radialCoeffs;
    
    public RectifyImage(List<List<Double>> K_matrix, List<Double> radialCoeffs) {
        this.K_matrix = K_matrix;
        this.radialCoeffs = radialCoeffs;
    }
    
    public BufferedImage rectify(BufferedImage inputImg,List<List<Double>> RT_matrix) {
        
        // Store the original image corners
        int vMax = inputImg.getHeight();
        int uMax = inputImg.getWidth();
        double[][] imageCornersArray = new double[][] {{0.0, 0.0}, {uMax, 0.0}, {uMax, vMax}, {0.0, vMax}};
        List<List<Double>> imageCorners = ArrayUtils.ArrayToList_Double2D(imageCornersArray);
        
        // Project the corners to world space
        List<List<Double>> worldCorners = Projection.toWorldPlane(imageCorners, K_matrix, RT_matrix, radialCoeffs);        
        
        // Convert RGB to gray
        BufferedImage grayImg = ImageUtils.color2Gray(inputImg);
        
        // Get the gray data
        List<List<Double>> grayImgData2D = ImageUtils.GrayImageToList(grayImg);
        double[][] imageGrid_F = ArrayUtils.ListToArray_Double2D(grayImgData2D);
        double[][] imageGrid_F_trans = MatrixUtils.createRealMatrix(imageGrid_F).transpose().getData();
        
        // Create the image grid
        double[] imageGrid_U = new double[uMax];        
        for (int u = 0; u < uMax; u++) {
            imageGrid_U[u] = u;
        }
        double[] imageGrid_V = new double[vMax];        
        for (int v = 0; v < vMax; v++) {
            imageGrid_V[v] = v;
        }
        
        // Create the interpolant
        BicubicInterpolator interp = new BicubicInterpolator();
        BicubicInterpolatingFunction interpFunc = interp.interpolate(imageGrid_U, imageGrid_V, imageGrid_F_trans);
        
        // Compute interpolated colors for each world space grid point
        double xMax = worldCorners.get(1).get(0);
        double xMin = worldCorners.get(0).get(0);
        double yMax = worldCorners.get(3).get(1);
        double yMin = worldCorners.get(0).get(1);
        double xInc = (xMax - xMin)/uMax;
        double yInc = (yMax - yMin)/vMax;
        List<List<Integer>> grayInterp = new ArrayList<>(uMax*vMax);
        for (double y = yMin; y < yMax; y += yInc) {
            List<Integer> grayInterpRow = new ArrayList<>();
            for (double x = xMin; x < xMax; x += xInc) {
                
                List<List<Double>> xyPt_list = new ArrayList<>();
                List<Double> xyPt = new ArrayList<>(2);
                xyPt.add(x);
                xyPt.add(y);
                xyPt_list.add(xyPt);
                
                List<Double> uvPt = Projection.toImagePlane(xyPt_list, K_matrix, RT_matrix, radialCoeffs).get(0);
                
                double u = uvPt.get(0);
                double v = uvPt.get(1);
                double grayInterpVal = 0;
                if (!(u < 0 || u > uMax-1 || v < 0 || v > vMax-1)) {
                    grayInterpVal = interpFunc.value(u, v);
                }
                
                grayInterpRow.add((int) grayInterpVal);
            }
            grayInterp.add(grayInterpRow);
        }
        
        BufferedImage rectifiedImage = ImageUtils.ListToGrayImage_Integer(grayInterp);
        
        
        return rectifiedImage;
    }

}
