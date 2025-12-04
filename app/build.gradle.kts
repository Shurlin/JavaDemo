plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "xyz.shurlin.demo2"
    compileSdk = 34

    defaultConfig {
        applicationId = "xyz.shurlin.demo2"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "0.3.3"

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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            // cast 为 ApkVariantOutputImpl 才能设置文件名
            val outputImpl = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl

            // app 名称（可自定义）
            val appName = "cdcpp"

            // 自动获取 versionName、versionCode、buildType
            val vName = variant.versionName
            val bType = variant.buildType.name

            outputImpl.outputFileName = "${appName}_v${vName}_${bType}.apk"
        }
    }

}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    implementation(libs.cardview)
    implementation(libs.fragment)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.retrofit.scalars)
    implementation(libs.retrofit.moshi)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}