package hcmute.edu.vn.selfalarmproject.models;

import java.io.Serializable;

public class SongModel implements Serializable {
    private int songFileID;
    private int imgID;
    private String title;
    private String author;

    public int getSongFileID() {
        return songFileID;
    }

    public SongModel(int songFileID, int imgID, String title, String author, String duration) {
        this.songFileID = songFileID;
        this.imgID = imgID;
        this.title = title;
        this.author = author;
        this.duration = duration;
    }

    private String duration;

    public SongModel(String title, String author, String duration) {
        this.title = title;
        this.author = author;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getDuration() {
        return duration;
    }

    public int getImgID() {
        return imgID;
    }
}
