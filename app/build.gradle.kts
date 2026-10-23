plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.dsdkwe.panvk.g925"
    compileSdk = 34

    signingConfigs {
        create("releaseBuild") {
            storeFile = file("common_plugin.jks")
            storePassword = "common_plugin"
            keyAlias = "common_plugin"
            keyPassword = "common_plugin"
        }
    }

    defaultConfig {
        applicationId = "com.dsdkwe.panvk.g925"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("releaseBuild")
        }
        configureEach {
            // 应用名
            resValue("string", "app_name", "PanVK G925")
            // 包名后缀
            applicationIdSuffix = ".panvk_g925"
            // 插件描述
            manifestPlaceholders["des"] = "Mesa PanVK Driver for Mali G925 (Kbase CSF)"
            // JVM 环境参数配置
            manifestPlaceholders["environment"] = mutableMapOf<String, String>().run {
                // 这里可以配置需要的环境变量，留空即可
                buildList {
                    this@run.forEach { (key, value) -> add("-D$key=$value") }
                }.joinToString(" ")
            }
            // 支持版本
            manifestPlaceholders["minMCVer"] = ""
            manifestPlaceholders["maxMCVer"] = ""
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

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}
