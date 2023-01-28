package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "terminal_commands")
public class TerminalCommand implements Parcelable {
    
    @ColumnInfo (name = "command")
    private String command;
    @ColumnInfo (name = "arguments")
    private String arguments;
    
    public static final Creator <TerminalCommand> CREATOR = new Creator <TerminalCommand>() {
        @Override
        public TerminalCommand createFromParcel(Parcel in) {
            return new TerminalCommand(in);
        }
        
        @Override
        public TerminalCommand[] newArray(int size) {
            return new TerminalCommand[size];
        }
    };
    @ColumnInfo (name = "description")
    private String description;
    @PrimaryKey (autoGenerate = true)
    @ColumnInfo (name = "date_created")
    private long dateCreated;
    @ColumnInfo (name = "label")
    private String label;
    
    public TerminalCommand(String command, String arguments, String label, String description, long dateCreated) {
        this.command = command;
        this.arguments = arguments;
        this.label = label;
        this.description = description;
        this.dateCreated = dateCreated;
    }
    
    protected TerminalCommand(Parcel in) {
        command = in.readString();
        arguments = in.readString();
        label = in.readString();
        description = in.readString();
        dateCreated = in.readLong();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(command);
        dest.writeString(arguments);
        dest.writeString(label);
        dest.writeString(description);
        dest.writeLong(dateCreated);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public String getCommand() {
        return command;
    }
    
    public void setCommand(String command) {
        this.command = command;
    }
    
    public String getArguments() {
        return arguments;
    }
    
    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public long getDateCreated() {
        return dateCreated;
    }
    
    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
}
