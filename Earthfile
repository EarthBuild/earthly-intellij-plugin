VERSION --pass-args --global-cache --use-function-keyword 0.7
ARG --global gradle_version=8.7
ARG --global version=0.0.0
FROM gradle:${gradle_version}-jdk17

install:
  RUN apt-get update && apt-get install -y \
    zip \
    && rm -rf /var/lib/apt/lists/*
  COPY settings.gradle.kts build.gradle.kts ./
  # Set up Gradle caches directly
  ENV GRADLE_USER_HOME=/root/.gradle

src:
  FROM +install
  COPY src src
  ARG --required version
  RUN sed -i 's^0.0.0^'"$version"'^g' ./build.gradle.kts

test:
  FROM +src --version=$version
  RUN --mount=type=cache,target=/root/.gradle gradle --no-daemon test

# dist builds the plugin and saves the artifact locally
dist:
  FROM +src --version=$version
  RUN --mount=type=cache,target=/root/.gradle gradle --no-daemon buildPlugin
  SAVE ARTIFACT build/distributions/earthly-intellij-plugin-$version.zip AS LOCAL earthly-intellij-plugin-$version.zip

# sign signs the plugin and saves the artifact locally
sign:
  FROM --pass-args +src
  RUN --mount=type=cache,target=/root/.gradle \
    --secret CERTIFICATE_CHAIN \
    --secret PRIVATE_KEY \
    --secret PRIVATE_KEY_PASSWORD \
    gradle --no-daemon signPlugin
  SAVE ARTIFACT build/distributions/earthly-intellij-plugin-$version.zip AS LOCAL earthly-intellij-plugin-signed-$version.zip

# publish publishes the plugin to the IntelliJ marketplace
publish:
  FROM --pass-args +src
  RUN --push \
    --mount=type=cache,target=/root/.gradle \
    --secret CERTIFICATE_CHAIN \
    --secret PRIVATE_KEY \
    --secret PRIVATE_KEY_PASSWORD \
    --secret PUBLISH_TOKEN \
    gradle --no-daemon publishPlugin

# generate-gradle-wrapper generates ./gradlew and its dependencies in the local machine
generate-gradle-wrapper:
  WORKDIR /tmp/wrap
  RUN gradle --no-daemon init wrapper
  RUN ls -hal
  SAVE ARTIFACT gradle AS LOCAL gradle
  SAVE ARTIFACT gradlew AS LOCAL gradlew
  SAVE ARTIFACT gradlew.bat AS LOCAL gradlew.bat

# ide opens an IntelliJ IDE with the plugin installed. Requires ./gradlew (see +generate-gradle-wrapper)
ide:
  LOCALLY
  RUN ./gradlew runIde
