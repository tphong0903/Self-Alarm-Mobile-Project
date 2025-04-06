package hcmute.edu.vn.selfalarmproject.utils.cloud;

import com.cloudinary.Cloudinary;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {
    public static Cloudinary cloudinary;

    public static Cloudinary getInstance() {
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
