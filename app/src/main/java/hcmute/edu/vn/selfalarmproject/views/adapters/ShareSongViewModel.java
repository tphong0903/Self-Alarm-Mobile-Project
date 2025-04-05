package hcmute.edu.vn.selfalarmproject.views.adapters;

import android.media.MediaPlayer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import hcmute.edu.vn.selfalarmproject.models.SongModel;

public class ShareSongViewModel extends ViewModel {
    private final MutableLiveData<SongModel> data = new MutableLiveData<>();
    private final MutableLiveData<Integer> passTime = new MutableLiveData<>();
    private final MutableLiveData<Integer> remainTime = new MutableLiveData<>();
    private static final MutableLiveData<Integer> songDuration = new MutableLiveData<>();
    private static final MutableLiveData<Integer> position = new MutableLiveData<>(-100);
    private static final MutableLiveData<Boolean> playStatus = new MutableLiveData<>();

    public static LiveData<Boolean> getPlayStatus() {
        return playStatus;
    }

    public static void setStatus(boolean status) {
        playStatus.setValue(status);
    }

    public void setSong(SongModel value) {
        data.setValue(value);
    }

    public LiveData<SongModel> getSong() {
        return data;
    }

    public void setPassTime(Integer val) {
        passTime.setValue(val);
    }

    public LiveData<Integer> getPassTime() {
        return passTime;
    }

    public void setRemainTime(Integer val) {
        remainTime.setValue(val);
    }

    public LiveData<Integer> getRemainTime() {
        return remainTime;
    }

    public static void setSongDuration(int val) {
        songDuration.setValue(val);
    }

    public LiveData<Integer> getSongDuration() {
        return songDuration;
    }

    public static void setPosition(int val) {
        position.setValue(val);
    }

    public static LiveData<Integer> getPosition() {
        return position;
    }

}
