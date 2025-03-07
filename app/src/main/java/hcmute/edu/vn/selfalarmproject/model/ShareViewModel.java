package hcmute.edu.vn.selfalarmproject.model;

import android.media.MediaPlayer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ShareViewModel extends ViewModel {
    private final MutableLiveData<Song> data = new MutableLiveData<>();
    private final MutableLiveData<Integer> passTime = new MutableLiveData<>();
    private final MutableLiveData<Integer> remainTime = new MutableLiveData<>();
    private final MutableLiveData<Integer> songDuration = new MutableLiveData<>();
    private final MutableLiveData<Integer> position = new MutableLiveData<>();
    private final MutableLiveData<MediaPlayer> song = new MutableLiveData<>();

    public void setSongMeta(MediaPlayer m){
        song.setValue(m);
    }
    public LiveData<MediaPlayer> getSongMeta(){
        return song;
    }
    public void setSong(Song value) {
        data.setValue(value);
    }

    public LiveData<Song> getSong() {
        return data;
    }
    public void setPassTime(Integer val){
        passTime.setValue(val);
    }
    public LiveData<Integer> getPassTime(){
        return passTime;
    }
    public void setRemainTime(Integer val){
        remainTime.setValue(val);
    }
    public LiveData<Integer> getRemainTime(){
        return remainTime;
    }
    public void setSongDuration(int val){
        songDuration.setValue(val);
    }
    public LiveData<Integer> getSongDuration(){
        return songDuration;
    }
    public void setPosition(int val){
        position.setValue(val);
    }
    public LiveData<Integer> getPosition(){
        return position;
    }

}
