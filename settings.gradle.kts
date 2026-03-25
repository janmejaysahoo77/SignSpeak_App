pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://storage.zego.im/maven") }

        // Agora plugin repository
        maven {
            url = uri("https://repo.agora.io/repository/maven/")
            content {
                includeGroup("io.agora.rtc")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://storage.zego.im/maven") }

        // REQUIRED for Agora SDK 4.x
        maven {
            url = uri("https://repo.agora.io/repository/maven/")
            content {
                includeGroup("io.agora.rtc")
            }
        }
    }
}

rootProject.name = "SignSpeak"
include(":app")
