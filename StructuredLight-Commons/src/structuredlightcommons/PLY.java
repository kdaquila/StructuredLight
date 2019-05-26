package structuredlightcommons;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;

public class PLY
{    
    public static <T> void saveVerts(List<List<Double>> verts, String folder, String filename, boolean append)
    {        
        try
        {
            // create the folder if needed
            File dir = new File(folder);
            if (!dir.exists())
            {
                FileUtils.forceMkdir(dir);
            }
            
            // open the write
            File newFile = new File(folder + "\\" + filename);
            FileWriter writer = new FileWriter(newFile, append);
            BufferedWriter buffWriter = new BufferedWriter(writer);
            
            // write the header
            buffWriter.write("ply\n");
            buffWriter.write("format ascii 1.0\n");
            buffWriter.write("comment this is a point cloud\n");
            buffWriter.write("element vertex " + verts.size() + "\n");
            buffWriter.write("property double x\n");
            buffWriter.write("property double y\n");
            buffWriter.write("property double z\n");
            buffWriter.write("end_header\n");
            
            // convert data to string
            String dataString = TXT.MatrixToString(verts, "%.3f", " ", "\n");
            
            // write the data string
            buffWriter.write(dataString);
            
            // close the writer
            buffWriter.close();
        }
        catch (IOException exp)
        {
            throw new RuntimeException("Could not save to the file");
        }        
    }
}
