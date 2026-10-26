// Standalone app build, reconstructed 2026-10-26 (finalize track B).
// This REPLACES the old stub, which only copied a prebuilt APK into the
// build outputs and defined no Android plugin, dependencies, or BuildConfig.
// That stub is why the repo could not compile from source.
//
// Before the first build, create local.properties (never committed) with:
//   GEMINI_API_KEY=<your key>
// The secrets plugin turns it into BuildConfig.GEMINI_API_KEY. Do NOT put
// a real key in .env, gradle.properties, or any committed file. Before a
// public release, Gemini calls must move behind a server-side proxy;
// a key baked into any app build can be extracted from the APK.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.secrets)
    alias(libs.plugins.google.services)
}

android {
    // BuildConfig and R live under com.example (all code imports them so).
    namespace = "com.example"
    compileSdk = 36

    defaultConfig {
        // AI Studio package, kept deliberately: changing applicationId
        // later breaks updates for users who already installed the app.
        applicationId = "com.aistudio.ideaintel.vqzmtk"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            // Debug signing is automatic; no signingConfig line here.
            // (The old README line about removing a debugConfig signing
            // line applied to the AI Studio build only.)
        }
        release {
            // First standalone release: no shrinking, so nothing is
            // stripped by mistake. Enable minify + shrinkResources only
            // after a verified release build, with matching ProGuard rules.
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Release signing is NOT in this repo. Sign with your upload
            // key in Android Studio (Build > Generate Signed Bundle/APK),
            // or add a signingConfigs block pointing at a keystore that
            // lives outside git.
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

secrets {
    // local.properties is the plugin default; stated explicitly so the
    // key source is obvious. sdk.dir in that file is ignored by the plugin.
    defaultPropertiesFileName = "local.properties"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.sqlite.framework)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.coil.compose)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.core)
    implementation(libs.accompanist.permissions)
    implementation(libs.play.services.location)
    implementation(libs.androidx.datastore.preferences)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.ai)
    implementation(libs.firebase.appcheck.recaptcha)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
