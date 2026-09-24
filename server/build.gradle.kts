plugins {
	alias(libs.plugins.spring.boot)
}

dependencies {
	implementation(project(":api"))
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
}
