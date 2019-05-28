package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Quad
{    
    public static List<List<Integer>> findMaxAreaQuad(List<List<Integer>> hull)
    {
        /**
         * This function requires that the convex hull points were sorted in clockwise
         * direction so that area formula computes positive values.
         */
        List<List<Integer>> quad = new ArrayList<>();
        int nPts = hull.size();

        if (nPts < 4)
        {
            return quad;
        }

        int a = 0;
        int b = 1;
        int c = 2;
        int d = 3;
        int best_a = 0;
        int best_b = 1;
        int best_c = 2;
        int best_d = 3;

        while(true) // loop A
        {
            while(true) // loop B
            {
                while(true) // loop C
                {
                    while(true)
                    {
                        Double currentArea = findQuadArea(hull.get(a), hull.get(b), hull.get(c), hull.get(d));
                        Double nextArea = findQuadArea(hull.get(a), hull.get(b), hull.get(c), hull.get((d+1)%nPts)); 
                        if(nextArea<currentArea) break;
                        else d = (d+1)%nPts; // advance d
                    }

                    if(findQuadArea(hull.get(a), hull.get(b), hull.get((c+1)%nPts), hull.get(d)) >= 
                       findQuadArea(hull.get(a), hull.get(b), hull.get(c), hull.get(d)))
                    {
                        c  = (c+1)%nPts; // advance c
                    }
                    else
                    {
                        break; // loop C
                    }
                }

                if(findQuadArea(hull.get(a), hull.get((b+1)%nPts), hull.get(c), hull.get(d)) >= 
                   findQuadArea(hull.get(a), hull.get(b), hull.get(c), hull.get(d)))
                {
                    b  = (b+1)%nPts; //advance b
                }
                else
                {
                    break; // loop B
                }
            }

            if (findQuadArea(hull.get(a), hull.get(b), hull.get(c), hull.get(d)) > 
                findQuadArea(hull.get(best_a), hull.get(best_b), hull.get(best_c), hull.get(best_d)))
            {
                best_a = a;
                best_b = b;
                best_c = c;
                best_d = d;
            }

            a = (a+1)%nPts; // advance a

            if (a == b)
            {
                b = (b+1)%nPts; // avoid collision
            }
            if (b == c)
            {
                c = (c+1)%nPts; // avoid collision
            }
            if (c == d)
            {
                d = (d+1)%nPts; // avoid collision
            }
            if (a == 0)
            {
                break; // loop A
            }
        }

        // store the points
        quad.add(Arrays.asList(hull.get(best_a).get(0), hull.get(best_a).get(1)));
        quad.add(Arrays.asList(hull.get(best_b).get(0), hull.get(best_b).get(1)));
        quad.add(Arrays.asList(hull.get(best_c).get(0), hull.get(best_c).get(1)));
        quad.add(Arrays.asList(hull.get(best_d).get(0), hull.get(best_d).get(1)));
        
        // rotate list entries so it begins with point closest to (0,0)
        quad = sortCorners(quad);        
        
        return quad;        
    }
    
    public static Double findQuadArea(List<Integer> pt1, List<Integer> pt2, List<Integer> pt3, List<Integer> pt4)
    {
        /** This function uses the "shoelace formula" to compute the area
            of a quadrilateral given the Cartesian corner points
        */
        Integer x1 = pt1.get(0);
        Integer x2 = pt2.get(0);
        Integer x3 = pt3.get(0);
        Integer x4 = pt4.get(0);
        Integer y1 = pt1.get(1);
        Integer y2 = pt2.get(1);
        Integer y3 = pt3.get(1);
        Integer y4 = pt4.get(1);
        return 0.5*(x1*y2 + x2*y3 + x3*y4 + x4*y1 - x2*y1 - x3*y2 - x4*y3 - x1*y4);
    }
    
    public static List<List<Integer>> sortCorners(List<List<Integer>> corners)
    {
        boolean isDone = false;
        int n = corners.size();
        if (n != 4)
        {
            throw new RuntimeException("There must be exactly four corners");
        }        
        
        // compute distances
        List<Double> distances = new ArrayList<>();
        List<Integer> originPoint = Arrays.asList(0,0);
        for (int i = 0; i < n; i++)
        {
            distances.add(distance(corners.get(i),originPoint));
        }
            
        // find position of minimum distance
        int offset = distances.indexOf(Collections.min(distances));
        
        // rotate the corners
        Collections.rotate(corners, -offset);
        
        return corners;
    }
    
    public static Double distance(List<Integer> pt1, List<Integer> pt2)
    {
        Double x1 = new Double(pt1.get(0));
        Double y1 = new Double(pt1.get(1));
        Double x2 = new Double(pt2.get(0));
        Double y2 = new Double(pt2.get(1));
        return Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2)); 
    }
    
}
