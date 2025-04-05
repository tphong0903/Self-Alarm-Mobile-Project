package hcmute.edu.vn.selfalarmproject.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class AudioUtils {

    public static String getAudioFilePath(ContentResolver contentResolver, Uri uri) {
        String[] projection = {MediaStore.Audio.Media.DATA};
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);

        if (cursor != null) {
            Log.d("Path", "Path exists");
            cursor.moveToFirst();
            String[] names = cursor.getColumnNames();
            for(String s : names){
                Log.d("Column", s);
            }
            int columnIndex = cursor.getColumnIndexOrThrow("_data");
            String audioFilePath = cursor.getString(columnIndex);
            Log.d("Path", audioFilePath);
            cursor.close();
            return audioFilePath;
        }

        return null;
    }
}
