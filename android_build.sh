#!/bin/bash

BINARY="gradlew tool"

function all() {
    clean
    build
    install
}

function dev() {
    ./gradlew assembleDebug
}

function build() {
    ./gradlew assembleRelease
}

function dependency() {
    ./gradlew dependencyUpdates
}

function install() {
    adb install app/build/outputs/apk/release/app-release.apk
}

function clean() {
    ./gradlew clean
}

function help() {
    echo "Usage: $0 {all|dev|build|dependency|install|clean|help}"
    echo "Commands:"
    echo "  all         Clean, build and install the release apk"
    echo "  dev         Compile debug apk"
    echo "  build       Compile release apk"
    echo "  dependency  Check for latest dependencies"
    echo "  install     Install release apk"
    echo "  clean       Clean build"
    echo "  help        Show this help message"
}

case "$1" in
    all) all ;;
    dev) dev ;;
    build) build ;;
    dependency) dependency ;;
    install) install ;;
    clean) clean ;;
    help|*) help ;;
esac