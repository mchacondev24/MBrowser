plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.maxwell.mbrowser"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.maxwell.mbrowser"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0-turbo"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ""
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.webkit:webkit:1.12.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

tasks.withType<Test> {
    useJUnit()
    jvmArgs("-Dfile.encoding=UTF-8")
}

tasks.register<JavaExec>("runUnitTests") {
    dependsOn("compileDebugUnitTestKotlin")
    mainClass.set("com.maxwell.mbrowser.TestRunner")
    classpath = files(
        android.bootClasspath,
        configurations.getByName("debugUnitTestRuntimeClasspath"),
        tasks.named("compileDebugKotlin").get().outputs.files,
        tasks.named("compileDebugUnitTestKotlin").get().outputs.files
    )
    jvmArgs("-Dfile.encoding=UTF-8")
}
