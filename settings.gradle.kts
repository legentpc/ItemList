pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		mavenCentral()
		gradlePluginPortal()
	}
}

// Should match your modid
rootProject.name = "skyblock-item-list"

val versions = listOf("26.1")
