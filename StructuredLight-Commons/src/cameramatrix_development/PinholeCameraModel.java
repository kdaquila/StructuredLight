package cameramatrix_development;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class PinholeCameraModel extends AbstractCameraModel {
    
    RealMatrix M; // full projection matrix    
    List<List<Double>> imagePts;
    List<List<Double>> worldPts;
    
    public PinholeCameraModel(List<List<Double>> imagePts, List<List<Double>> worldPts) {        
        M = new Array2DRowRealMatrix(3, 4);
        this.imagePts = imagePts;
        this.worldPts = worldPts;
    }
    
    @Override
    public void computeParameters() {
        
        int nPts = imagePts.size();
        
        // unpack the (u,v) points
        List<Double> uPts = new ArrayList<>(nPts);
        List<Double> vPts = new ArrayList<>(nPts);
        for (List<Double> pt: imagePts) {
            uPts.add(pt.get(0));
            vPts.add(pt.get(1));
        }
        
        // upack the (x,y,z) points
        List<Double> xPts = new ArrayList<>(nPts);
        List<Double> yPts = new ArrayList<>(nPts);
        List<Double> zPts = new ArrayList<>(nPts);
        for (List<Double> pt: worldPts) {
            xPts.add(pt.get(0));
            yPts.add(pt.get(1));
            zPts.add(pt.get(2));
        }
        
        // build matrix A and vector B
        RealVector B = new ArrayRealVector(2*nPts);
        RealMatrix A = new Array2DRowRealMatrix(2*nPts, 11);
        for (int i = 0; i < nPts; i++) {
            Double ui = uPts.get(i);
            Double vi = vPts.get(i);
            Double xi = xPts.get(i);
            Double yi = yPts.get(i);
            Double zi = zPts.get(i);
            
            // set vector B elements
            B.setEntry(i, ui);
            B.setEntry(i+nPts, vi);           
            
            // set matrix A elements
            A.setEntry(i, 0, xi);
            A.setEntry(i, 1, yi);
            A.setEntry(i, 2, zi);
            A.setEntry(i, 3, 1);
            A.setEntry(i+nPts, 4, xi);
            A.setEntry(i+nPts, 5, yi);
            A.setEntry(i+nPts, 6, zi);
            A.setEntry(i+nPts, 7, 1);
            A.setEntry(i, 8, -ui*xi);
            A.setEntry(i, 9, -ui*yi);
            A.setEntry(i, 10, -ui*zi);
            A.setEntry(i+nPts, 8, -vi*xi);
            A.setEntry(i+nPts, 9, -vi*yi);
            A.setEntry(i+nPts, 10, -vi*zi);
        }
        
        // Solve for X
        QRDecomposition qr = new QRDecomposition(A);
        DecompositionSolver solver = qr.getSolver();
        RealVector X = solver.solve(B);

        // Reshape into matrix M
        X = X.append(1.0d);
        for (int i = 0; i < 3; i++) {
           M.setRowVector(i, X.getSubVector(4*i, 4));
        }        
    }   
    
    @Override
    public List<Double> project3DTo2D(List<Double> worldPt) {
        List<Double> worldPt_homogCoord = new ArrayList<>(worldPt);
        worldPt_homogCoord.add(1.0d);
        RealVector xyzw = new ArrayRealVector(4);
        for (int i = 0; i < 4; i++) {
            xyzw.setEntry(i, worldPt_homogCoord.get(i));
        }
        RealVector abc = M.operate(xyzw);
        List<Double> output = new ArrayList<>(2);
        output.add(abc.getEntry(0)/abc.getEntry(2));
        output.add(abc.getEntry(1)/abc.getEntry(2));
        return output;
    }
    
    public void exportToCSV(String path){
        File f = new File(path);
        try (FileWriter writer = new FileWriter(f);) {            
            writer.write("# Pinhole Camera Projection Matrix (3x4) for 3D_homog to 2D_homog\n");
            double[][] MArray = M.getData();
            for (int row = 0; row < 3; row++) {  
                for (int col = 0; col < 4; col++) {
                    writer.write(String.valueOf(MArray[row][col]));
                    writer.write(",");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            // do nothing
        }       
    }



}
