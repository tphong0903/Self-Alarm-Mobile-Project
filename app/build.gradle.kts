import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "hcmute.edu.vn.selfalarmproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "hcmute.edu.vn.selfalarmproject"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation("com.google.firebase:firebase-database-ktx:21.0.0")

    implementation("com.google.android.gms:play-services-auth:21.0.0")

    implementation("com.google.api-client:google-api-client-android:1.33.0")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
