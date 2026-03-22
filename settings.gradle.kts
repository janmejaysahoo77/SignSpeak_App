pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()

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
