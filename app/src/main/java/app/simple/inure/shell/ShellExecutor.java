package app.simple.inure.shell;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ShellExecutor {
    
    public String executor(String command) {
        
        StringBuilder output = new StringBuilder();
        Process process;
        
        try {
            process = Runtime.getRuntime().exec(command);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("n");
            }
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
        
        return output.toString();
    }
}
