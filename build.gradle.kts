plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.6.0"
}

group = "dev.earthly"
version = "0.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("org.apache.commons:commons-collections4:4.5.0")
    testImplementation("junit:junit:4.13.2")
    
    intellijPlatform {
        intellijIdeaUltimate("2025.1.3")
        bundledPlugin("org.jetbrains.plugins.textmate")
        
        pluginVerifier()
        zipSigner()
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

intellijPlatform {
    buildSearchableOptions = false
    
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "232"
            untilBuild = "252.*"
        }
    }
    
    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }
    
    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
    
    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    
    test {
        useJUnit()
    }
}
