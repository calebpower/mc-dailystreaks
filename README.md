# Daily Streaks

A Spigot plugin to encourage player engagement.

## Building

You can build this software with Gradle, provided you have the proper build
environment installed. We recommend OpenJDK 17. The Gradle Wrapper has been
included in this project. Although there is discourse on the subject, it has
been included to ensure the proper Gradle version is adhered to and to make it
easier to build the software.

On *NIX-like systems, you can use the following to build the project.

```bash
./gradlew build publishToMavenLocal
```

Likewise, on *doze systems, you can use the following.

```bash
gradlew.bat build publishToMavenLocal
```

The above should yield the plugin JAR at `build/libs/DailyStreaks.jar`.

## Installation

This plugin was designed for Spigot v1.20.1. You'll want to ensure that you're
using Java 17 LTS. Like normal, just put `DailyStreaks.jar` in your `plugins`
folder and start the server. Please note that this plugin requires `McDb` to be
properly configured for the `game` database handler prior to installation.

# License

Copyright 2023 Caleb L. Power. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This repository is not affiliated or endorsed with either Microsoft or Mojang.
