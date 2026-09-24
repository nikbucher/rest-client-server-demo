rootProject.name = "rest-client-server-demo"

dependencyResolutionManagement {
	repositories {
		mavenCentral()
	}
}

include("api", "client", "server")
