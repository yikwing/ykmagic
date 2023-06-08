.PHONY: Android Gradlew MakeFile

BINARY="gradlew tool"

all: clean buildR install

dev:
	./gradlew assembleDebug

buildR:
	./gradlew assembleRelease

version:
	./gradlew dependencyUpdates

install:
	adb install app/build/outputs/apk/release/app-release.apk

clean:
	./gradlew clean

help:
	@echo "make - build android apk"
	@echo "make dev - 编译debug apk"
	@echo "make build - 编译Release apk"
	@echo "make version - 查看最新依赖"
	@echo "make install - 安装release apk"
	@echo "make clean - clean build"
