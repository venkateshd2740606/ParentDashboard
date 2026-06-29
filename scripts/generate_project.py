#!/usr/bin/env python3
"""Generate ParentDashboard Android project files."""
import os
from pathlib import Path

ROOT = Path(r"d:\Work\Games\ParentDashboard")
APP = ROOT / "app"
MAIN = APP / "src" / "main"
JAVA = MAIN / "java" / "com" / "parentdashboard"
TEST = APP / "src" / "test" / "java" / "com" / "parentdashboard"
ANDROID_TEST = APP / "src" / "androidTest" / "java" / "com" / "parentdashboard"

def write(path: Path, content: str):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8", newline="\n")
    print(f"  {path.relative_to(ROOT)}")

files = {}

# ============ GRADLE ============
files[ROOT / "settings.gradle.kts"] = '''pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\\\.android.*")
                includeGroupByRegex("com\\\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ParentDashboard"
include(":app")
'''

files[ROOT / "build.gradle.kts"] = '''plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}
'''

files[APP / "build.gradle.kts"] = '''plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.parentdashboard"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.parentdashboard.game"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"

        ksp { arg("room.schemaLocation", "$projectDir/schemas") }
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "keystore/release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            buildConfigField("Boolean", "ENABLE_ADS", "true")
            buildConfigField("String", "ADMOB_BANNER_ID", "\\"ca-app-pub-3940256099942544/9214589741\\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\\"ca-app-pub-3940256099942544/1033173712\\"")
            buildConfigField("String", "ADMOB_REWARDED_ID", "\\"ca-app-pub-3940256099942544/5224354917\\"")
            buildConfigField("String", "ADMOB_REWARDED_INTERSTITIAL_ID", "\\"ca-app-pub-3940256099942544/5354046379\\"")
            buildConfigField("String", "ADMOB_APP_OPEN_ID", "\\"ca-app-pub-3940256099942544/9257395921\\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("Boolean", "ENABLE_ADS", "true")
            buildConfigField("String", "ADMOB_BANNER_ID", "\\"ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX\\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\\"ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX\\"")
            buildConfigField("String", "ADMOB_REWARDED_ID", "\\"ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX\\"")
            buildConfigField("String", "ADMOB_REWARDED_INTERSTITIAL_ID", "\\"ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX\\"")
            buildConfigField("String", "ADMOB_APP_OPEN_ID", "\\"ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX\\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
    bundle {
        language { enableSplit = true }
        density { enableSplit = true }
        abi { enableSplit = true }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.navigation.compose)
    implementation(libs.coroutines.android)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.remote.config)
    implementation(libs.admob)
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)
    implementation(libs.splash.screen)
    implementation(libs.coil.compose)
    implementation(libs.lottie.compose)
    implementation(libs.gson)
    implementation(libs.security.crypto)
    implementation(libs.play.integrity)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
'''

files[APP / "proguard-rules.pro"] = '''-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keep class com.google.firebase.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-keep class com.google.gson.** { *; }
-keep class com.google.android.gms.ads.** { *; }
-keep class com.parentdashboard.data.** { *; }
-keep class com.parentdashboard.domain.model.** { *; }
-keep class com.google.android.play.core.integrity.** { *; }
'''

files[APP / "google-services.json"] = '''{
  "project_info": {
    "project_number": "000000000001",
    "project_id": "parentdashboard-game",
    "storage_bucket": "parentdashboard-game.firebasestorage.app"
  },
  "client": [{
    "client_info": {
      "mobilesdk_app_id": "1:000000000001:android:0000000000000000000001",
      "android_client_info": { "package_name": "com.parentdashboard.game" }
    },
    "oauth_client": [],
    "api_key": [{ "current_key": "AIzaSyDummyKeyForBuildOnly000000000" }],
    "services": { "appinvite_service": { "other_platform_oauth_client": [] } }
  }],
  "configuration_version": "1"
}
'''

if __name__ == "__main__":
    print("Generating ParentDashboard project files...")
    for path, content in files.items():
        write(path, content)
    print(f"Done: {len(files)} files written (part 1)")
