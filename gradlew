#!/usr/bin/env sh
##############################################################################
# Gradle start up script for UN*X
# (gerado manualmente pelo assistente - pode ser substituído pelo real via 'gradle wrapper')
##############################################################################

DIRNAME="`dirname "$0"`"
APP_HOME="`cd "$DIRNAME"; pwd`"

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
