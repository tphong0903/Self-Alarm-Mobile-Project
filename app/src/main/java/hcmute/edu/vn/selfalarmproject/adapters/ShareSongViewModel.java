package hcmute.edu.vn.selfalarmproject.adapters;

import android.media.MediaPlayer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import hcmute.edu.vn.selfalarmproject.models.SongModel;

public class ShareSongViewModel extends ViewModel {
    private final MutableLiveData<SongModel> data = new MutableLiveData<>();
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
    public void setSong(SongModel value) {
        data.setValue(value);
    }

    public LiveData<SongModel> getSong() {
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
