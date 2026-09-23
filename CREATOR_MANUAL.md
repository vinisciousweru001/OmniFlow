# OmniFlow creator manual

The in-app creator manual is disabled by default and is only exposed in a creator build.

## Enable creator mode

From Android Studio, add this Gradle project property to the creator build:

```bash
-PcreatorMode=true
```

If you are using a Gradle wrapper or another Gradle runner, the equivalent command is:

```bash
./gradlew assembleDebug -PcreatorMode=true
```

The same flag can be provided through the environment:

```bash
OMNIFLOW_CREATOR_MODE=true ./gradlew assembleDebug
```

With creator mode enabled, open **Sync & AI → Creator Manual**. The manual covers the
Today hub, tasks, calendar, notes, AI, voice, sync/export, and the release checklist.

## Release rule

Do not pass `creatorMode=true` and do not set `OMNIFLOW_CREATOR_MODE` for normal user
builds. The default is `false`, so the manual card is not rendered and the manual screen
cannot be opened through the app UI.