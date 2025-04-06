import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}
val configProps = Properties()
val configFile = rootProject.file("local.properties")
if (configFile.exists()) {
    configProps.load(FileInputStream(configFile))
}
android {
    namespace = "hcmute.edu.vn.selfalarmproject"
    compileSdk = 35
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        applicationId = "hcmute.edu.vn.selfalarmproject"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "GOOGLE_API_KEY", "\"${configProps["GOOGLE_API_KEY"]}\"")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.apache.httpcomponents" && requested.name == "httpclient") {
            useTarget("org.apache.httpcomponents.client5:httpclient5:5.2.1")
        }
    }
    exclude(group = "org.apache.httpcomponents", module = "httpclient")
    exclude(group = "org.apache.httpcomponents", module = "httpcore")
}
dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation("com.google.firebase:firebase-database-ktx:21.0.0")
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.api-client:google-api-client-android:1.33.0")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0")
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.lombok)
    implementation(libs.glide)
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation(libs.media3.common)

    annotationProcessor(libs.lombok)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}

