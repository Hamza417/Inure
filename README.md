![Banner](assets/winter_banner.png)

# Inure App Manager

An elegant Android app manager, currently in development

## Stats

![](https://img.shields.io/tokei/lines/github/Hamza417/Inure?color=orange&label=Total%20Lines&logo=kotlin&logoColor=white)
![](https://img.shields.io/github/downloads/Hamza417/inure/total?color=orange&label=Total%20Downloads&logo=github&logoColor=white)
[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2FHamza417%2FInure&count_bg=%239A3DC8&title_bg=%23555555&icon=tencentweibo.svg&icon_color=%23E7E7E7&title=Total+Visits&edge_flat=false)](https://hits.seeyoufarm.com)
![](https://img.shields.io/github/languages/count/Hamza417/Inure?color=white&label=Languages)
![](https://img.shields.io/github/license/Hamza417/Inure?color=red&label=License)
![](https://img.shields.io/badge/Minimum%20SDK-23%20(Marshmallow)-839192?logo=android&logoColor=white)
![](https://img.shields.io/badge/Target%20SDK-33%20(Android%2013)-566573?logo=android&logoColor=white)
[![Crowdin](https://badges.crowdin.net/inure/localized.svg)](https://crowdin.com/project/inure)
[![Telegram Group](https://img.shields.io/badge/Telegram%20Group-blue?logo=telegram)](https://t.me/inure_app_manager)

## Download

[![](https://img.shields.io/badge/Play%20Store-ea4335?logo=googleplay)](https://play.google.com/store/apps/details?id=app.simple.inure)
[![](https://img.shields.io/badge/IzzyOnDroid-4AB495?logo=fdroid)](https://apt.izzysoft.de/fdroid/index/apk/app.simple.inure)
[![](https://img.shields.io/badge/GitHub%20Releases-181717?logo=github)](https://github.com/Hamza417/Inure/releases/latest)

## Buy

[![](https://img.shields.io/badge/Full%20Unlocker%20(GumRoad)-$5.99-23a094?logo=gumroad&logoColor=white)](https://hamza417.gumroad.com/l/inure_unlocker/)
[![](https://img.shields.io/badge/Full%20Unlocker%20(Payhip)-$5.99-23a094?logo=payhip&logoColor=white)](https://payhip.com/b/D9ikG)
[![](https://img.shields.io/badge/Full%20Unlocker%20(Play%20Store)-~$5.99-ea4335?logo=googleplay)](https://play.google.com/store/apps/details?id=app.simple.inureunlocker)

## Featured

- [Android Weekly Issue #465](https://androidweekly.net/issues/issue-465)
- ["Inure, a beautifully animated Android App Manager." - Android Dev Notes](https://twitter.com/androiddevnotes/status/1389111968670179340)
- [Top 5 Android Apps of the Week - NextPit](https://www.nextpit.com/apps-of-the-week-51-2021)
- [5 Super Useful OPENSOURCE Apps for Android - TechDoc](https://youtu.be/vlf0jEFHR74)

## About

Inure is an Android application package manager irrespective of whether they're installed or not, it
can scan through any app's internal components and modify them on the go. It also packs a nice
looking Terminal Emulator, Usage Stats, Split/APK Installer and various other tools and many of them
are waiting to be implemented as well.

Inure's development was started as an independent learning project which later has become one of the
most ambitious and intuitive Android apps developed by a single person and is based 100% on custom
UI APIs developed to be used only in this app with its own native theme engine, crash handler, image
renderer and a beautiful animation framework. There's also an implementation for PC like mouse hover
effects and click interactions for tablet and PC users.

Along with it own UI perks, Inure also supports dynamic Material You colors with split accent and
theme color modes.

Currently, the app is in stable beta testing stage. You can join app's early beta testing
from [Play Store](https://play.google.com/store/apps/details?id=app.simple.inure) and
its [Telegram Channel](https://t.me/inure_app_manager) for development related updates.

If you've tried the app, you can write your user/developer
feedbacks [here](https://github.com/Hamza417/Inure/discussions/48).

## Future

The new [Build53](https://github.com/Hamza417/Inure/releases/tag/Build53) is live for everyone to
update. I have finalized the Boot Manager and it should work as expected now. As of now it only
supports complete enable or disable mode for an app. In the future, I'm expected to add a component
management in Boot Manager so that you can allow some components to run while stop the others from
running.

And, I have revamped the Development Preferences panel, it should be more stable to use now. I'm
hoping to fix all the Preferences panel issue if time allows for that but I think it's stable as it
is. Also, due to changes in Development, you might have to re-enable some settings.

I added option to disable image caching. You can use that to prevent app from taking up a lot of
space in the storage. Disabling it shouldn't be affecting the app performance, in my tests the app
continued to perform same under both conditions.

This build also finalizes the app's development and I have added almost everything I had planned for
the app. Any new features might not be added anymore however I'll keep tweaking the app to make it
more usable and stable as the time progresses and keep on improving all the existing features and
everyone can expect one or two updates every month. You can however keep reporting the bugs and
issues and I'll keep fixing them too.

That's all.

## Development Status

### High priority features

- [x] Apps Backup feature.
- [x] Dedicated app installer.
- [x] External APK information.
- [x] Proper analytics panel.
- [x] App directories panel.
- [ ] APK browser for apps that are not yet installed.
- [ ] Modify Shared Prefs of other apps _(root)_.
- [ ] Modify SQLite databases of other apps _(root)_.
- [ ] Multiple user support _(suspended)_.
- [ ] Use app via ADB Shell instead of root? _(suspended)_.
- [x] Add battery optimisation manager for all apps.
- [x] Boot manager for all apps.
- [ ] Quick Apps Sync from different devices.
- [ ] Options to modify state of trackers of any app.

### Low priority features

- [ ] Code highlights for all languages an Android app possibly contains.
- [x] Dedicated TextEditor.
- [x] ImageViewer scalable zoom support.
- [ ] APK data extraction.

*This list will be updated as the development progresses and a concised to-do task list
is [here](https://github.com/Hamza417/Inure/issues/63).*

## Screenshots

### App's Interface

| ![01](fastlane/metadata/android/en-US/images/phoneScreenshots/01.png) | ![02](fastlane/metadata/android/en-US/images/phoneScreenshots/03.png) | ![03](fastlane/metadata/android/en-US/images/phoneScreenshots/04.png) | ![04](fastlane/metadata/android/en-US/images/phoneScreenshots/06.jpg) |
|:---------------------------------------------------------------------:|:---------------------------------------------------------------------:|:---------------------------------------------------------------------:|:---------------------------------------------------------------------:|
|                                 0x01                                  |                                 0x02                                  |                                 0x03                                  |                                 0x04                                  |

| ![15](fastlane/metadata/android/en-US/images/phoneScreenshots/15.png) | ![01](fastlane/metadata/android/en-US/images/phoneScreenshots/07.png) | ![01](fastlane/metadata/android/en-US/images/phoneScreenshots/05.jpg) | ![01](fastlane/metadata/android/en-US/images/phoneScreenshots/08.jpg) |
|:---------------------------------------------------------------------:|:---------------------------------------------------------------------:|:---------------------------------------------------------------------:|:---------------------------------------------------------------------:|
|                                 0x05                                  |                                 0x06                                  |                                 0x07                                  |                                 0x08                                  |

| ![01](fastlane/metadata/android/en-US/images/phoneScreenshots/02.jpg) | ![01](fastlane/metadata/android/en-US/images/phoneScreenshots/09.jpg) | ![01](fastlane/metadata/android/en-US/images/phoneScreenshots/10.jpg) | ![01](fastlane/metadata/android/en-US/images/phoneScreenshots/11.jpg) |
|:---------------------------------------------------------------------:|:---------------------------------------------------------------------:|:---------------------------------------------------------------------:|:---------------------------------------------------------------------:|
|                                 0x09                                  |                                 0x00                                  |                                 0x0A                                  |                                 0x0B                                  |

| ![01](fastlane/metadata/android/en-US/images/phoneScreenshots/12.jpg) | ![01](fastlane/metadata/android/en-US/images/phoneScreenshots/13.png) | ![01](fastlane/metadata/android/en-US/images/phoneScreenshots/00.png) | ![01](fastlane/metadata/android/en-US/images/phoneScreenshots/14.png) |
|:---------------------------------------------------------------------:|:---------------------------------------------------------------------:|:---------------------------------------------------------------------:|:---------------------------------------------------------------------:|
|                                 0x0C                                  |                                 0x0D                                  |                                 0x0E                                  |                                 0x0F                                  |

| ![01](fastlane/metadata/android/en-US/images/phoneScreenshots/16.png) | ![01](./assets/music.jpg) | ![01](fastlane/metadata/android/en-US/images/phoneScreenshots/17.png) | ![01](fastlane/metadata/android/en-US/images/phoneScreenshots/18.png) |
|:---------------------------------------------------------------------:|:-------------------------:|:---------------------------------------------------------------------:|:---------------------------------------------------------------------:|
|                                 0x11                                  |           0x12            |                                 0x13                                  |                                 0x14                                  |

## Behind The Scenes

|             ![01](./assets/01.jpg)              |
|:-----------------------------------------------:|
|   Designing of interface skeletons for Inure    |

|        ![01](./assets/inure_music.png)        |
|:---------------------------------------------:|
| Audio Player interface design (later removed) |

|     ![01](./assets/inure_batch.png)     |
|:---------------------------------------:|
| Presentation design for Batch interface |

## Contribute

### Developers

Inure's repo doesn't require any special setup, clone/fork the repo and run
in [Android Studio](https://developer.android.com/studio) and let the initial
setup finish automatically and you'll be good to go.

### Not a developer?

You can take participation in app's translation on [Crowdin](https://crowdin.com/project/inure)
and watch Inure work in your language and make the app more user friendly to
everyone speaking your tongue... 😎😎

### Want to show you were here?

You cannot do the above still wanna show your support? hit the star 🌟 and let me know you were here
and appreciate the project.. 😄

## License

**Inure App Manager** Copyright © 2022 - Hamza Rizwan

**Inure App Manager** is released as open source software under
the [GPL v3](https://opensource.org/licenses/gpl-3.0.html)
license, see the [LICENSE](./LICENSE) file in the project root for the full license text.
