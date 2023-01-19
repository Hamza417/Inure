package app.simple.inure.models;

import android.os.Parcel;
import android.os.Parcelable;

public class AudioModel implements Parcelable {
    
    private String name;
    private String title;
    private String artist;
    private String album;
    private String artUri;
    private String fileUri;
    private String path;
    private String mimeType;
    
    private int track;
    private int year;
    private int size;
    private int bitrate;
    
    private long duration;
    private long id;
    private long dateAdded;
    private long dateModified;
    private long dateTaken;
    
    public AudioModel() {
    }
    
    protected AudioModel(Parcel in) {
        name = in.readString();
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        artUri = in.readString();
        fileUri = in.readString();
        path = in.readString();
        mimeType = in.readString();
        track = in.readInt();
        year = in.readInt();
        size = in.readInt();
        bitrate = in.readInt();
        duration = in.readLong();
        id = in.readLong();
        dateAdded = in.readLong();
        dateModified = in.readLong();
        dateTaken = in.readLong();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeString(artUri);
        dest.writeString(fileUri);
        dest.writeString(path);
        dest.writeString(mimeType);
        dest.writeInt(track);
        dest.writeInt(year);
        dest.writeInt(size);
        dest.writeInt(bitrate);
        dest.writeLong(duration);
        dest.writeLong(id);
        dest.writeLong(dateAdded);
        dest.writeLong(dateModified);
        dest.writeLong(dateTaken);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <AudioModel> CREATOR = new Creator <AudioModel>() {
        @Override
        public AudioModel createFromParcel(Parcel in) {
            return new AudioModel(in);
        }
        
        @Override
        public AudioModel[] newArray(int size) {
            return new AudioModel[size];
        }
    };
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getArtists() {
        return artist;
    }
    
    public void setArtists(String artist) {
        this.artist = artist;
    }
    
    public String getAlbum() {
        return album;
    }
    
    public void setAlbum(String album) {
        this.album = album;
    }
    
    public String getArtUri() {
        return artUri;
    }
    
    public void setArtUri(String artUri) {
        this.artUri = artUri;
    }
    
    public String getFileUri() {
        return fileUri;
    }
    
    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public void setDuration(long duration) {
        this.duration = duration;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getDateAdded() {
        return dateAdded;
    }
    
    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }
    
    public long getDateModified() {
        return dateModified;
    }
    
    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }
    
    public long getDateTaken() {
        return dateTaken;
    }
    
    public void setDateTaken(long dateTaken) {
        this.dateTaken = dateTaken;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public int getTrack() {
        return track;
    }
    
    public void setTrack(int track) {
        this.track = track;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    public int getBitrate() {
        return bitrate;
    }
    
    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }
    
    @Override
    public String toString() {
        return "AudioModel{" +
                "name='" + name + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", artUri='" + artUri + '\'' +
                ", fileUri='" + fileUri + '\'' +
                ", path='" + path + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", track=" + track +
                ", year=" + year +
                ", size=" + size +
                ", duration=" + duration +
                ", id=" + id +
                ", dateAdded=" + dateAdded +
                ", dateModified=" + dateModified +
                ", dateTaken=" + dateTaken +
                '}';
    }
}
