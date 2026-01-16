pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()

        // Agora plugin repository
        maven(url = "https://repo.agora.io/repository/maven/")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // REQUIRED for Agora SDK 4.x
        maven(url = "https://repo.agora.io/repository/maven/")
    }
}

rootProject.name = "SignSpeak"
include(":app")
