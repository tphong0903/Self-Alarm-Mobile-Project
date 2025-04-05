package hcmute.edu.vn.selfalarmproject.cloud;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {
    public static Cloudinary cloudinary;
    public static Cloudinary getInstance(){
        if (cloudinary == null) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dwijkd4xi");
            config.put("api_key", "862117432415768");
            config.put("api_secret", "G5q1TlioVtStYHATJnPFokJkvIg");
            cloudinary = new Cloudinary(config);
        }
        return cloudinary;
    }
}
