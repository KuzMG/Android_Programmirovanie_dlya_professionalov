pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MyEducation"
include(":app")
include(":CriminalIntent")
include(":beatbox")
include(":nerdlauncher")
include(":photogallery")
include(":draganddraw")
include(":sunset")
