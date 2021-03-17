package app.simple.inure.shell;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ShellExecutor {
    
    public ShellExecutor() {
    
    }
    
    public String executor(String command) {
        
        StringBuilder output = new StringBuilder();
        
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            
            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line).append("n");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}
