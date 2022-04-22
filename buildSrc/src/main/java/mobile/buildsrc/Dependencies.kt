package mobile.buildsrc

object Versions {
    val ktlint = "0.29.0"
}

object GradlePlugins {
    val androidGradlePlugin = "com.android.tools.build:gradle:3.5.3"
}

object Libs {

    val cameraIntegrator = "com.github.Him-khati:camera_intergrator:0.2.3"
    val timber = "com.jakewharton.timber:timber:4.7.1"
    val junit = "junit:junit:4.12"

    val progressButton = "com.github.razir.progressbutton:progressbutton:2.1.0"

    val mockitoCore = "org.mockito:mockito-inline:3.0.0"
    val mockitoKotlin = "com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0"
    val mockitoInline = "org.mockito:mockito-inline:3.0.0"

    val mpChart = "com.github.PhilJay:MPAndroidChart:v3.1.0"
    val appIntro = "com.github.AppIntro:AppIntro:5.1.0"
    val zoomableImageView = "com.jsibbold:zoomage:1.3.1"
    val leakCanary2 = "com.squareup.leakcanary:leakcanary-android:2.3"
    val coil = "io.coil-kt:coil:0.11.0"
    val localization = "com.akexorcist:localization:1.2.10"
    val statusbarUtil = "com.jaeger.statusbarutil:library:1.5.1"
    val uCrop = "com.github.yalantis:ucrop:2.2.4"
    val easyPermissions = "pub.devrel:easypermissions:3.0.0"

    //Fixes a Crash occurs while showing toast in Android 7
    val toastFix = "com.toastfix:toastcompatwrapper:1.2.0"


    object Vinners {
        val logger = "com.github.vinnersafterwork:core:1.0"
    }

    object Facebook {
        val stetho = "com.facebook.stetho:stetho:1.5.1"
        val shimmer = "com.facebook.shimmer:shimmer:0.5.0@aar"
    }

    object Firebase {
        //Latest version at https://firebase.google.com/docs/android/setup#kotlin+ktx_2

        val crashlyticsGradlePlugin = "com.google.firebase:firebase-crashlytics-gradle:2.3.0"

        val coreKtx = "com.google.firebase:firebase-core:20.1.2"
        val analyticsKtx = "com.google.firebase:firebase-analytics-ktx:20.1.2"
        val messagingKtx = "com.google.firebase:firebase-messaging-ktx:23.0.3"
        val messagingDirectBootKtx = "com.google.firebase:firebase-messaging-directboot:23.0.3"
        val dynamicLinksKtx = "com.google.firebase:firebase-dynamic-links-ktx:21.0.1"
        val inAppMessagingKtx = "com.google.firebase:firebase-inappmessaging-ktx:20.1.2"
        val crashlyticsKtx = "com.google.firebase:firebase-crashlytics-ktx:18.2.9"
        val authKtx = "com.google.firebase:firebase-auth-ktx:21.0.3"
        val firestoreKtx = "com.google.firebase:firebase-firestore-ktx:24.1.1"
        val storageKtx = "com.google.firebase:firebase-storage-ktx:20.0.1"
        val realTimeDatabaseKtx = "com.google.firebase:firebase-database-ktx:20.0.4"
        val remoteConfigKtx = "com.google.firebase:firebase-config-ktx:20.0.3"

        val authUi = "com.firebaseui:firebase-ui-auth:4.3.1"
        val storageUi = "com.firebaseui:firebase-ui-storage:4.3.2"

        val mlKitFaceDetection = "com.google.android.gms:play-services-mlkit-face-detection:16.2.0"


    }

    object Google {

        val material = "com.google.android.material:material:1.3.0"
        val gmsGoogleServicesGradlePlugin = "com.google.gms:google-services:4.3.3"
        val placesLibrary = "com.google.android.libraries.places:places:1.0.0"
        val maps = "com.google.android.gms:play-services-maps:17.0.0"

        val playCore = "com.google.android.play:core:1.7.2"
        val playCoreKtx = "com.google.android.play:core-ktx:1.8.1"
        val playLocation = "com.google.android.gms:play-services-location:17.0.0"
        val playRefer = "com.android.installreferrer:installreferrer:1.1.2"
        val playServicesBase = "com.google.android.gms:play-services-base:17.0.0"
        val authApiPhone = "com.google.android.gms:play-services-auth-api-phone:17.0.0"
        val auth = "com.google.android.gms:play-services-auth:18.0.0"
        val smsRetriver = "com.google.android.gms:play-services-gcm:17.0.0"

        val exoplayerCore  = "com.google.android.exoplayer:exoplayer-core:2.11.5"
        val exoplayerUi  = "com.google.android.exoplayer:exoplayer-ui:2.11.5"

        val zxing = "com.google.zxing:core:3.4.0"
    }

    object Kotlin {
        private const val version = "1.4.0"
        val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"
        val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        val extensions = "org.jetbrains.kotlin:kotlin-android-extensions:$version"

        /**
         * Adds extensiosn for converting Task to Deferred,
         * can be used with firebase to skip onSuccessListener etc callback)
         */
        val playServicesCoroutinesKtx = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.1.1"

    }


    object Android {
        val desugarJdk = "com.android.tools:desugar_jdk_libs:1.1.5"
    }

    object AndroidX {
        val appcompat = "androidx.appcompat:appcompat:1.3.0"
        val browser = "androidx.browser:browser:1.0.0"
        val palette = "androidx.palette:palette:1.0.0"
        val recyclerview = "androidx.recyclerview:recyclerview:1.0.0"
        val emoji = "androidx.emoji:emoji:1.0.0"
        val fragment = "androidx.fragment:fragment:1.0.0"
        val fragmentKtx = "androidx.fragment:fragment-ktx:1.3.5"
        val activityKtx = "androidx.activity:activity-ktx:1.3.1"

        val multiDex = "androidx.multidex:multidex:2.0.1"
        val vectorDrawables = "androidx.vectordrawable:vectordrawable:1.1.0"
        val preference = "androidx.preference:preference:1.1.0-alpha02"
        val constraintlayout = "androidx.constraintlayout:constraintlayout:1.1.3"
        val coreKtx = "androidx.core:core-ktx:1.3.2"
        val archCoreTesting = "androidx.arch.core:core-testing:2.1.0"
        val dataStore = "androidx.datastore:datastore-preferences:1.0.0"
        val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
        val coordinatorLayout = "androidx.coordinatorlayout:coordinatorlayout:1.1.0"
        val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.3"
        val exifInterface = "androidx.exifinterface:exifinterface:1.3.2"


        object Test {
            val core = "androidx.test:core:1.1.0"
            val runner = "androidx.test:runner:1.1.1"
            val rules = "androidx.test:rules:1.1.1"
            val junitTest = "androidx.test.ext:junit:1.1.2"
            val espressoCore = "androidx.test.espresso:espresso-core:3.3.0"
        }

        object Navigation {
            private const val version = "2.3.0"
            val navigationFragment = "androidx.navigation:navigation-fragment:$version"
            val navigationFragmentKtx = "androidx.navigation:navigation-fragment-ktx:$version"
            val navigationUi = "androidx.navigation:navigation-ui:$version"
            val navigationUiKtx = "androidx.navigation:navigation-ui-ktx:$version"
        }

        object Paging {
            private const val version = "2.1.0"
            val common = "androidx.paging:paging-common:$version"
            val runtime = "androidx.paging:paging-runtime-ktx:$version"
            val rxjava2 = "androidx.paging:paging-rxjava2-ktx:$version"
        }

        object Lifecycle {
            private const val version = "2.4.0"
            val extensions = "androidx.lifecycle:lifecycle-extensions:2.2.0"
            val compiler = "androidx.lifecycle:lifecycle-compiler:$version"
            val lifeCyleViewModelKtx = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
            val lifeCyleLiveDataKtx = "androidx.lifecycle:lifecycle-livedata-ktx:$version"
            val lifeCycleKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
            val viewModelSavedState = "androidx.lifecycle:lifecycle-viewmodel-savedstate:2.3.0"
        }

        object Camera {
            private const val version = "1.1.0-alpha11"
            val cameraCore =  "androidx.camera:camera-core:$version"
            val camera =  "androidx.camera:camera-camera2:$version"
            val cameraLifecycle = "androidx.camera:camera-lifecycle:$version"
            val cameraView =  "androidx.camera:camera-view:1.0.0-alpha31"
        }

        object Room {
            private const val version = "2.2.5"
            val roomKtx = "androidx.room:room-ktx:$version"
            val common = "androidx.room:room-common:$version"
            val runtime = "androidx.room:room-runtime:$version"
            val rxjava2 = "androidx.room:room-rxjava2:$version"
            val compiler = "androidx.room:room-compiler:$version"
        }

        object Work {
            private const val version = "2.7.1"
            val workManager = "androidx.work:work-runtime-ktx:$version"
            val workManageRxJavaSupport = "androidx.work:work-rxjava2:$version"
        }

        object Security {
            val securityCrypto = "androidx.security:security-crypto:1.0.0-rc02"
        }
    }

    object AirBnb {
        val lottie = "com.airbnb.android:lottie:4.2.2"
    }


    object RxJava {
        val rxJava = "io.reactivex.rxjava2:rxjava:2.2.12"
        val rxKotlin = "io.reactivex.rxjava2:rxkotlin:2.3.0"
        val rxAndroid = "io.reactivex.rxjava2:rxandroid:2.1.1"
    }

    object Coroutines {
        private const val version = "1.6.0"
        val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        val rx2 = "org.jetbrains.kotlinx:kotlinx-coroutines-rx2:$version"
        val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
    }

    object Dagger {
        private const val version = "2.27"
        val dagger = "com.google.dagger:dagger:$version"
        val androidSupport = "com.google.dagger:dagger-android-support:$version"
        val compiler = "com.google.dagger:dagger-compiler:$version"
        val androidProcessor = "com.google.dagger:dagger-android-processor:$version"

        const val hiltVersion = "2.38.1"
        val hiltGradlePugin = "com.google.dagger:hilt-android-gradle-plugin:$hiltVersion"
        val hilt = "com.google.dagger:hilt-android:$hiltVersion"
        val hiltCompiler = "com.google.dagger:hilt-compiler:$hiltVersion"
    }

    object Dropbox {
        val store = "com.dropbox.mobile.store:store4:4.0.4-KT15"
    }

    object Glide {
        private const val version = "4.10.0"
        val glide = "com.github.bumptech.glide:glide:$version"
        val compiler = "com.github.bumptech.glide:compiler:$version"


    }


    object Square {

        object OkHttp {
            val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:3.11.0"
        }

        object Okio {
            val okIo = "com.squareup.okio:okio:2.2.2"
        }

        object Retrofit {
            private const val version = "2.9.0"
            val retrofit = "com.squareup.retrofit2:retrofit:$version"
            val retrofit_rxjava_adapter = "com.squareup.retrofit2:adapter-rxjava2:$version"
            val gsonConverter = "com.squareup.retrofit2:converter-gson:$version"
            val moshiConverter = "com.squareup.retrofit2:converter-moshi:$version"
        }
    }

    object Gson {
        val gson = "com.google.code.gson:gson:2.8.5"
    }

    object AssistedInject {
        private const val version = "0.4.0"
        val annotationDagger2 = "com.squareup.inject:assisted-inject-annotations-dagger2:$version"
        val processorDagger2 = "com.squareup.inject:assisted-inject-processor-dagger2:$version"
    }
}
