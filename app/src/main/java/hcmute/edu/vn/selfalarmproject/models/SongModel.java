package hcmute.edu.vn.selfalarmproject.models;

import org.checkerframework.checker.units.qual.N;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SongModel implements Serializable {
    private String id;
    private String title;
    private String author;
    private String duration;
    private String imageURL;
    private String songURL;

    public SongModel(String title, String author, String duration){
        this.title = title;
        this.author = author;
        this.duration = duration;
    }
}
