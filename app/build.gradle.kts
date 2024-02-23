plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kapt)
    alias(libs.plugins.hilt)
}

val tomtomApiKey: String by project

android {
    namespace = "io.pascucci"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.pascucci"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        buildConfig = true
        dataBinding = true
    }
    buildTypes.configureEach {
        buildConfigField("String", "TOMTOM_API_KEY", "\"$tomtomApiKey\"")
        resValue("string", "tom_tom_api_key", "\"$tomtomApiKey\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging {
        jniLibs.pickFirsts.add("lib/**/libc++_shared.so")
    }
}

dependencies {

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.legacy.support.v4)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.hilt.android.core)
    kapt(libs.hilt.compiler)
    implementation(libs.timber)
    implementation(libs.slidinguppanel) {
//        exclude(group = "com.android.support", module = "support-compat")
//        exclude(group = "com.android.support", module = "support-media-compat")
    }

    implementation(libs.tom.map)
    implementation(libs.tom.route)
    implementation(libs.tom.location.android)
    implementation(libs.tom.location.simulation)
    implementation(libs.tom.location.matched)
    implementation("com.tomtom.sdk.location:provider-map-matched:$version")
    implementation(libs.tom.search)
    implementation(libs.tom.navigation)
    implementation(libs.tom.navigation.route)
    implementation(libs.tom.navigation.ui)
    implementation(libs.tom.navigation.store)

}