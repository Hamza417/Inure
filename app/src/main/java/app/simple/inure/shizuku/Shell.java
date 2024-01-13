package app.simple.inure.shizuku;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;

public interface Shell {
    
    boolean isAvailable();
    
    Result exec(Command command);
    
    Result exec(Command command, InputStream inputPipe);
    
    String makeLiteral(String arg);
    
    class Command implements Parcelable {
        public static final Creator <Command> CREATOR = new Creator <Command>() {
            @Override
            public Command createFromParcel(Parcel in) {
                return new Command(in);
            }
            
            @Override
            public Command[] newArray(int size) {
                return new Command[size];
            }
        };
        
        public Command(String command, String... args) {
            this.args.add(command);
            this.args.addAll(Arrays.asList(args));
        }
        
        private ArrayList <String> args = new ArrayList <>();
        
        protected Command(Parcel in) {
            args = in.createStringArrayList();
        }
        
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeStringList(args);
        }
        
        @Override
        public int describeContents() {
            return 0;
        }
        
        public String[] toStringArray() {
            String[] array = new String[args.size()];
            
            for (int i = 0; i < args.size(); i++)
                array[i] = args.get(i);
            
            return array;
        }
        
        @NonNull
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            
            for (int i = 0; i < args.size(); i++) {
                String arg = args.get(i);
                sb.append(arg);
                if (i < args.size() - 1) {
                    sb.append(" ");
                }
            }
            
            return sb.toString();
        }
        
        public static class Builder {
            private final Command command;
            
            public Builder(String command, String... args) {
                this.command = new Command(command, args);
            }
            
            public Builder addArg(String argument) {
                command.args.add(argument);
                return this;
            }
            
            public Command build() {
                return command;
            }
        }
    }
    
    class Result {
        public int exitCode;
        public String out;
        public String err;
        Command cmd;
        
        protected Result(Command cmd, int exitCode, String out, String err) {
            this.cmd = cmd;
            this.exitCode = exitCode;
            this.out = out;
            this.err = err;
        }
        
        public boolean isSuccess() {
            return exitCode == 0;
        }
        
        @SuppressLint ("DefaultLocale")
        @NonNull
        @Override
        public String toString() {
            return String.format("Command: %s\nExit code: %d\nOut:\n%s\n=============\nErr:\n%s", cmd, exitCode, out, err);
        }
    }
    
}