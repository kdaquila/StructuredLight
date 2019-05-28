package cameramatrix_development;

import java.util.List;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;

public class RealisticCameraProjection implements MultivariateJacobianFunction {
    
    double[] M;     // full calibration matrix (3x4)
    double[] d;     // distortion coefficients (1x4)
    double[][] xyzList;
    double[][] stList;
    double[][] uvList;
    double[][] jacobianFull;
    int nPts;
    
    public RealisticCameraProjection(List<List<Double>> xyz) {
        nPts = xyz.size();
        int i = 0;
        xyzList = new double[nPts][3];
        for (List<Double> pt: xyz) {
            xyzList[i][0] = pt.get(0);
            xyzList[i][1] = pt.get(1);
            xyzList[i][2] = pt.get(2);
            i++;
        }        
    }
    
    public double[] XYZtoST(double[] xyz) {
        double x = xyz[0];
        double y = xyz[1];
        double z = xyz[2];
        double s = (x*M[0] + y*M[1] + z*M[2] + M[3]) / (x*M[8] + y*M[9] + z*M[10] + 1d );
        double t = (x*M[4] + y*M[5] + z*M[6] + M[7]) / (x*M[8] + y*M[9] + z*M[10] + 1d );
        double[] st = new double[]{s, t};
        return st;
    }
    
    public void allXYZtoST() {
        stList = new double[nPts][2];
        for (int i = 0; i < nPts; i++) {
            stList[i] = XYZtoST(xyzList[i]);
        } 
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
    
    
    public double[][] computeJacobian(double[] st, double[] xyz) {
        double s = st[0];
        double t = st[1];
        double x = xyz[0];
        double y = xyz[1];
        double z = xyz[2];
        
        // derivatives of u
        double du_ds = ( 1 + d[0]*pow(t,2) + 3*d[0]*pow(s,2) + 5*d[1]*pow(s,4) + d[1]*pow(t,4) + 6*d[1]*pow(t,2)*pow(s,2) + 2*d[2]*t + 6*d[3]*s );
        double du_dt = ( 2*d[0]*s*t + 4*d[1]*s*pow(t,3) + 4*d[1]*pow(s,3)*t + 2*d[2]*s + 2*d[3]*t );
        double du_dd0 = ( s*pow(t,2) + pow(s,3) );
        double du_dd1 = ( pow(s,5) + s*pow(t,4) + 2*pow(s,3)*pow(t,2) );
        double du_dd2 = ( 2*s*t );
        double du_dd3 = ( 3*pow(s,2) + pow(t,2));
        
        // derivatives of v
        double dv_ds = ( 2*d[0]*t*s + 4*d[1]*t*pow(s,3) + 4*d[1]*pow(t,3)*s + 2*d[3]*t + 2*d[2]*s );
        double dv_dt = ( 1 + d[0]*pow(s,2) + 3*d[0]*pow(t,2) + 5*d[1]*pow(t,4) + d[1]*pow(s,4) + 6*d[1]*pow(s,2)*pow(t,2) + 2*d[3]*s + 6*d[2]*t );
        double dv_dd0 = ( t*pow(s,2) + pow(t,3) );
        double dv_dd1 = ( t*pow(s,4) + pow(t,5) + 2*pow(t,3)*pow(s,2) );
        double dv_dd2 = ( 3*pow(t,2) + pow(s,2) );
        double dv_dd3 = ( 2*s*t );
        
        // common factors
        double s_numerator = x*M[0] + y*M[1] + z*M[2] + M[3];
        double st_denominator = x*M[8] + y*M[9] + z*M[10] + 1;
        double t_numerator = x*M[4] + y*M[5] + z*M[6] + M[7];
        
        //derivates of s
        double ds_dM0 = x/st_denominator;
        double ds_dM1 = y/st_denominator;
        double ds_dM2 = z/st_denominator;
        double ds_dM3 = 1/st_denominator;
        double ds_dM8 = -x*(s_numerator)/pow(st_denominator, 2);
        double ds_dM9 = -y*(s_numerator)/pow(st_denominator, 2);
        double ds_dM10 = -z*(s_numerator)/pow(st_denominator, 2);
        
        //derivates of t
        double dt_dM4 = ds_dM0;
        double dt_dM5 = ds_dM1;
        double dt_dM6 = ds_dM2;
        double dt_dM7 = ds_dM3;
        double dt_dM8 = -x*(t_numerator)/pow(st_denominator, 2);
        double dt_dM9 = -y*(t_numerator)/pow(st_denominator, 2);
        double dt_dM10 = -z*(t_numerator)/pow(st_denominator, 2);
        
        double[][] J = new double[2][15];
        J[0][0] = du_ds*ds_dM0;
        J[0][1] = du_ds*ds_dM1;
        J[0][2] = du_ds*ds_dM2;
        J[0][3] = du_ds*ds_dM3;
        J[0][4] = du_dt*dt_dM4;
        J[0][5] = du_dt*dt_dM5;
        J[0][6] = du_dt*dt_dM6;
        J[0][7] = du_dt*dt_dM7;
        J[0][8] = du_ds*ds_dM8 + du_dt*dt_dM8;
        J[0][9] = du_ds*ds_dM9 + du_dt*dt_dM9;
        J[0][10] = du_ds*ds_dM10 + du_dt*dt_dM10;
        J[0][11] = du_dd0;
        J[0][12] = du_dd1;
        J[0][13] = du_dd2;
        J[0][14] = du_dd3;
        J[1][0] = dv_ds*ds_dM0;
        J[1][1] = dv_ds*ds_dM1;
        J[1][2] = dv_ds*ds_dM2;
        J[1][3] = dv_ds*ds_dM3;
        J[1][4] = dv_dt*dt_dM4;
        J[1][5] = dv_dt*dt_dM5;
        J[1][6] = dv_dt*dt_dM6;
        J[1][7] = dv_dt*dt_dM7;
        J[1][8] = dv_ds*ds_dM8 + dv_dt*dt_dM8;
        J[1][9] = dv_ds*ds_dM9 + dv_dt*dt_dM9;
        J[1][10] = dv_ds*ds_dM10 + dv_dt*dt_dM10;
        J[1][11] = dv_dd0;
        J[1][12] = dv_dd1;
        J[1][13] = dv_dd2;
        J[1][14] = dv_dd3;
        
        return J;
    }
    
    public void computeAllJacobians() {
        jacobianFull = new double[nPts*2][15];
        for (int i = 0, j = 0; i < nPts; i++) {
            double[][] jacobianUV = computeJacobian(stList[i], xyzList[i]);
            jacobianFull[j] = jacobianUV[0];
            j++;
            jacobianFull[j] = jacobianUV[1];
            j++;
        } 
    }
    
    private double pow(double a, double b) {
        return Math.pow(a, b);
    }
    
    private double[] flattenArrayOfPairs(double[][] input) {
        double[] output = new double[nPts*2];
        for (int i = 0, j = 0; i < nPts; i++) {
            double x = input[i][0];
            double y = input[i][1];
            output[j] = x;
            j++;
            output[j] = y;
            j++;
        }
        return output;
    }
    
    @Override
    public Pair<RealVector, RealMatrix> value(RealVector guessParameters) {
        // Organize input parameters
        RealVector MVector = new ArrayRealVector(12, 1.0d);
        MVector.setSubVector(0, guessParameters.getSubVector(0, 11));
        M = MVector.toArray();
        d = guessParameters.getSubVector(11, 4).toArray();
        
        // Compute UV
        allXYZtoST();
        allSTtoUV();
        RealVector uvPts = new ArrayRealVector(flattenArrayOfPairs(uvList));
        
        // Compute Jacobian
        computeAllJacobians();
        RealMatrix jacobian = new Array2DRowRealMatrix(jacobianFull);
        
        Pair<RealVector, RealMatrix> output = new Pair<>(uvPts, jacobian);
        
        return output;
    }

}
