import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
	alias(libs.plugins.spring.boot) apply false
}

subprojects {
	pluginManager.apply("java")

	group = "com.example"
	version = "0.0.1-SNAPSHOT"

	extensions.configure<JavaPluginExtension> {
		toolchain {
			languageVersion = JavaLanguageVersion.of(25)
		}
	}

	dependencies {
		"implementation"(platform(SpringBootPlugin.BOM_COORDINATES))
		"testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
	}

	tasks.withType<JavaCompile>().configureEach {
		options.compilerArgs.add("-parameters")
	}

	tasks.withType<Test>().configureEach {
		useJUnitPlatform()
	}
}
