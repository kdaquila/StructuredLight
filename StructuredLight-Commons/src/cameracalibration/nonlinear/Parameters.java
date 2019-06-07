package cameracalibration.nonlinear;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class Parameters {
    
    public final List<RealMatrix> RT_allViews;
    public final RealMatrix K;
    public final List<Double> radialCoeffs;
    
    public Parameters(List<Double> X) {
        
        int nViews;
        if ((X.size()-7)%6 == 0) {
            nViews = (X.size()-7)/6;
        } else {
            throw new IllegalArgumentException("The parameters list must be of the form 6*M+7, with M as the number of views");
        }
        
        double alpha_refined = X.get(0);
        double beta_refined = X.get(1);
        double gamma_refined = X.get(2);
        double uC_refined = X.get(3);
        double vC_refined = X.get(4);
        K = MatrixUtils.createRealMatrix(3, 3);
        K.setEntry(0, 0, alpha_refined);
        K.setEntry(1, 1, beta_refined);
        K.setEntry(0, 1, gamma_refined);
        K.setEntry(0, 2, uC_refined);
        K.setEntry(1, 2, vC_refined);
        K.setEntry(2, 2, 1.0);
        double r0_refined = X.get(5);
        double r1_refined = X.get(6);
        radialCoeffs = new ArrayList<>(2);
        radialCoeffs.add(r0_refined);
        radialCoeffs.add(r1_refined);        
        RT_allViews = new ArrayList<>();
        for (int view_num = 0; view_num < nViews; view_num++) {
            RealMatrix RT = MatrixUtils.createRealMatrix(3, 4);
            int i = view_num*6 + 7;
            double rotVec0 = X.get(i+0);
            double rotVec1 = X.get(i+1);
            double rotVec2 = X.get(i+2);
            double tVec0 = X.get(i+3);
            double tVec1 = X.get(i+4);
            double tVec2 = X.get(i+5);
            Vector3D rotVec = new Vector3D(rotVec0, rotVec1, rotVec2);
            double angle = rotVec.getNorm();
            Vector3D axisVec = rotVec.normalize();
            Rotation newRotMatrix = new Rotation(axisVec, angle, RotationConvention.FRAME_TRANSFORM);
            RealMatrix R = MatrixUtils.createRealMatrix(newRotMatrix.getMatrix());
            RealVector T = MatrixUtils.createRealVector(new double[] {tVec0, tVec1, tVec2});
            RT.setColumnVector(0, R.getColumnVector(0));
            RT.setColumnVector(1, R.getColumnVector(1));
            RT.setColumnVector(2, R.getColumnVector(2));
            RT.setColumnVector(3, T);
            RT_allViews.add(RT);
        }           
    }
}
