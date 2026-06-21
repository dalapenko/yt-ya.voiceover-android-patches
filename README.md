# Yandex Voice Over Translation Patches for YouTube

This repository contains a standalone set of ReVanced patches that add Yandex Voice Over Translation to YouTube.

## Overview

This project is an extension built on top of the official [ReVanced Patches](https://gitlab.com/ReVanced/revanced-patches). It provides the Yandex Voice Over Translation feature, which was extracted from [anddea/revanced-patches](https://github.com/anddea/revanced-patches) and packaged as a separate patches bundle.

## Features

- **Voice Over Translation**: Translates foreign video audio using Yandex translation services.
- **Natural Voices**: Support for high-quality, natural-sounding Yandex voices.
- **Audio Dimming**: Automatically dims the original video audio track when the translation is playing.
- **Integrated Controls**: Player overlay buttons integrated directly into YouTube's player controls.

## How to Use

You can import these patches into the ReVanced Manager using one of the following methods:

### Option 1: Remote Patches Source (Recommended)
1. Open **ReVanced Manager**.
2. Go to **Settings** > **Sources**.
3. Under **Patches source**, add the following URL:
   ```text
   https://raw.githubusercontent.com/dalapenko/yt-ya.voiceover-android-patches/main/patches.json
   ```

### Option 2: Local Patches Bundle
1. Go to the [Releases](https://github.com/dalapenko/yt-ya.voiceover-android-patches/releases) page and download the latest `patches.rvp` file.
2. In **ReVanced Manager**, go to the patcher screen and import the downloaded `.rvp` file locally.

## Building from Source

This project compiles against a pinned version of the official patches repository, which is included as a Git submodule.

### Cloning

To clone the repository along with the required submodules:
```bash
git clone --recursive <repository-url>
```
If already cloned, fetch submodules manually:
```bash
git submodule update --init --recursive
```

### Compiling

To compile the patches bundle:
```bash
./gradlew buildAndroid
```
The output patches bundle (`.rvp`) will be generated at `patches/build/libs/`.
