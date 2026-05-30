import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    id("com.android.application")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

/*
* password: Ruchitech@2024
* alias: myAppProductionKey
* */

android {
    namespace = "com.ruchitech.quicklinkcaller"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ruchitech.quicklinkcaller"
        minSdk = 23
        targetSdk = 36
        versionCode = 30
        versionName = "1.30"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    applicationVariants.all {
        outputs.all {
            val SEP = "_"
            val flavor = flavorName
            val buildType = buildType.name
            val version = "${versionCode}(${versionName})"
            val date = Date()
            val formattedDate = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.US).format(date)

            val newApkName =
                "Business  Call Manager${SEP}$flavor${SEP}$buildType${SEP}$version${SEP}$formattedDate.apk"
            File(newApkName)
        }
    }

    flavorDimensions("environment")

    productFlavors {
        create("Dev") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://businesscallmanager.onrender.com/\"")
        }

        create("Prod") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://businesscallmanager.onrender.com/\"")
        }
        create("Local") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://businesscallmanager.onrender.com/\"")
        }
    }


    signingConfigs {
        create("release") {
            storeFile = file("businesscallmanager.keystore")
            storePassword = "BCM@Secure2026"
            keyAlias = "businesscallmanager"
            keyPassword = "BCM@Secure2026"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("com.google.firebase:firebase-messaging:23.4.0")
    implementation("com.google.firebase:firebase-crashlytics:18.6.0")
    implementation("com.google.firebase:firebase-analytics:21.5.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    val compose_version = "1.8.3"
    val nav_version = "2.7.6"
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-service:2.5.1")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.navigation:navigation-compose:$nav_version")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.compose.ui:ui:$compose_version")
    implementation("androidx.compose.ui:ui-graphics:$compose_version")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose_version")
    debugImplementation("androidx.compose.ui:ui-tooling:$compose_version")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_version")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_version")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.lifecycle:lifecycle-service:2.6.2")
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-android-compiler:2.48.1")
    implementation("com.auth0:java-jwt:3.18.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.github.dawidraszka.compose-permission-handler:core:1.5.0")
    // Only required if you want to use utils package
    implementation("com.github.dawidraszka.compose-permission-handler:utils:1.5.0")
    // Room
    val room_version = "2.6.0"
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // dialog sheets
    implementation("com.maxkeppeler.sheets-compose-dialogs:core:1.0.4")
    implementation("com.maxkeppeler.sheets-compose-dialogs:input:1.0.4")
    implementation("com.maxkeppeler.sheets-compose-dialogs:state:1.0.4")
    implementation("com.maxkeppeler.sheets-compose-dialogs:clock:1.0.4")
    implementation("com.maxkeppeler.sheets-compose-dialogs:calendar:1.0.4")

    //gson
    implementation("com.google.code.gson:gson:2.10.1")
    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    //OkHttp
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")
    implementation("com.itextpdf:itext7-core:7.1.15")
    /*implementation("javax.xml.stream:stax-api:1.0")
    implementation("com.fasterxml:aalto-xml:1.1.0")
    implementation("org.apache.poi:poi:4.0.0")
    implementation("org.apache.poi:poi-ooxml:4.0.0") {
        exclude(group = "org.apache.xmlbeans", module = "xmlbeans")
    }
*/
    implementation("org.apache.poi:poi-ooxml:3.17")
    implementation("org.apache.xmlbeans:xmlbeans:3.1.0")
    implementation("javax.xml.stream:stax-api:1.0")
    implementation("com.fasterxml:aalto-xml:1.2.2")
    implementation("androidx.hilt:hilt-work:1.1.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.23.0")
    implementation("com.google.accompanist:accompanist-pager:0.25.0")
    implementation("io.michaelrocks:libphonenumber-android:8.13.48")
    implementation ("com.googlecode.libphonenumber:libphonenumber:8.13.5")

    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")
    /*    implementation("com.github.ozcanalasalvar.picker:wheelview:2.0.7")
        implementation("com.github.ozcanalasalvar.picker:datepicker:2.0.7")*/


}