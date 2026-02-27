#!/usr/bin/env bash
set -euo pipefail

if [[ ! -f config/bot.toml ]]; then
  cp config/bot.toml.example config/bot.toml
  echo "Created config/bot.toml from example."
  echo "Set [runtime].discord_token before running."
  exit 1
fi

bash ./gradlew clean shadowJar
java -jar build/libs/ArcaneDemoBot-2.0.0.jar
