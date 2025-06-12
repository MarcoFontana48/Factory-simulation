plugins {
    id("com.gradle.develocity") version "3.19.2"
}

dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

rootProject.name = "factory-simulation"

include("factory")

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
        uploadInBackground = !System.getenv("CI").toBoolean()
        publishing {
            onlyIf {
                it.buildResult.failures.isNotEmpty()
            }
        }
    }
}
