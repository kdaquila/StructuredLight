package cameramatrix_development;

import core.ArrayUtils;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;

public class CameraLensDistortion implements MultivariateJacobianFunction{
    
    double[] d; //distortion coefficients
    double[][] stList;
    double[][] uvList;
    double[][] jacobianFull;
    int nPts;
    
    public CameraLensDistortion (double[][] initialImagePoints ) {
        this.stList = initialImagePoints;
        nPts = initialImagePoints.length;
    }
    
    public double[] STtoUV(double[] st) {
        double s = st[0];
        double t = st[1];
        double u = (s + d[0]*s*pow(t,2) + d[0]*pow(s,3) + d[1]*pow(s,5) + d[1]*s*pow(t,4) + 2*d[1]*pow(s,3)*pow(t,2) + 2*d[2]*s*t + 3*d[3]*pow(s,2) + d[3]*pow(t,2) );
        double v = (t + d[0]*t*pow(s,2) + d[0]*pow(t,3) + d[1]*pow(t,5) + d[1]*t*pow(s,4) + 2*d[1]*pow(t,3)*pow(s,2) + 2*d[3]*s*t + 3*d[2]*pow(t,2) + d[2]*pow(s,2) );
        double[] uv = new double[]{u,v};
        return uv;
    }
    
    public void allSTtoUV() {
        uvList = new double[nPts][2];
        for (int i = 0; i < nPts; i++) {
            uvList[i] = STtoUV(stList[i]);
        } 
    }
    
    public double[][] computeJacobian(double[] st) {
        double s = st[0];
        double t = st[1];
        
        // derivatives of u
        double du_dd0 = ( s*pow(t,2) + pow(s,3) );
        double du_dd1 = ( pow(s,5) + s*pow(t,4) + 2*pow(s,3)*pow(t,2) );
        double du_dd2 = ( 2*s*t );
        double du_dd3 = ( 3*pow(s,2) + pow(t,2));
        
        // derivatives of v
        double dv_dd0 = ( t*pow(s,2) + pow(t,3) );
        double dv_dd1 = ( t*pow(s,4) + pow(t,5) + 2*pow(t,3)*pow(s,2) );
        double dv_dd2 = ( 3*pow(t,2) + pow(s,2) );
        double dv_dd3 = ( 2*s*t );       
        
        double[][] J = new double[2][4];        
        J[0][0] = du_dd0;
        J[0][1] = du_dd1;
        J[0][2] = du_dd2;
        J[0][3] = du_dd3;        
        J[1][0] = dv_dd0;
        J[1][1] = dv_dd1;
        J[1][2] = dv_dd2;
        J[1][3] = dv_dd3;
        
        return J;
    }
    
    public void computeAllJacobians() {
        jacobianFull = new double[nPts*2][4];
        for (int i = 0, j = 0; i < nPts; i++) {
            double[][] jacobianUV = computeJacobian(stList[i]);
            jacobianFull[j] = jacobianUV[0];
            j++;
            jacobianFull[j] = jacobianUV[1];
            j++;
        } 
    }

    @Override
    public Pair<RealVector, RealMatrix> value(RealVector point) {
        // Organize inputs
        d = point.toArray();
        
        // Compute UV
        allSTtoUV();
        RealVector uvPts = new ArrayRealVector();
        
        // Compute Jacobian
        computeAllJacobians();
        RealMatrix jacobian = new Array2DRowRealMatrix(jacobianFull);
        
        Pair<RealVector, RealMatrix> output = new Pair(uvPts, jacobian);
        return output;
    }
    
    private double pow(double a, double b) {
        return Math.pow(a, b);
    }

}
