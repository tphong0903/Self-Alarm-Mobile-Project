package hcmute.edu.vn.selfalarmproject.utils.cloud;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.utils.ObjectUtils;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadFile {
    public interface UploadCallback {
        void onSuccess(String url);

        void onFailure(Exception e);
    }

    public static void uploadFile(Context context, Uri fileUri, UploadCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // Configure Cloudinary

                // Get file input stream
                ContentResolver resolver = context.getContentResolver();
                InputStream inputStream = resolver.openInputStream(fileUri);

                // Upload file
                Map uploadResult = CloudinaryManager.getInstance().uploader().upload(inputStream, ObjectUtils.asMap(
                        "resource_type", "auto"
                ));

                // Get the secure URL
                String uploadedUrl = (String) uploadResult.get("secure_url");
                Log.d("CloudinaryUpload", "Upload Success: " + uploadedUrl);

                // Return the URL using the callback
                if (callback != null) {
                    callback.onSuccess(uploadedUrl);
                }

            } catch (Exception e) {
                Log.e("CloudinaryUpload", "Upload Failed", e);
                if (callback != null) {
                    callback.onFailure(e);
                }
            }
        });
    }
}
