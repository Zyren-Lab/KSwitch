📂 KSwitch (Beta)
The Native "Smart Switch" Alternative for Linux
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-purple) ![Platform](https://img.shields.io/badge/Platform-Linux-black) ![License](https://img.shields.io/badge/License-GPLv3-blue) ![Status](https://img.shields.io/badge/Status-Beta-orange)

KSwitch is an open-source, native desktop application designed to bridge the gap between Android devices and Linux desktops. It functions as a lightweight, privacy-focused alternative to proprietary backup tools, allowing you to backup media, documents, and apps without installing any agent APKs on your phone.

Note: This project is unofficial and not affiliated with Samsung Electronics Co., Ltd.

✨ Features
🐧 Linux Native: Built with Kotlin Multiplatform (Compose for Desktop). No Wine, no VMs, no heavy dependencies.
🚀 Agentless Architecture: Works purely via ADB. No need to install a helper app on your Android device.
📂 Smart Mirroring: Preserves the exact directory structure of your phone.
Phone: /sdcard/DCIM/Camera/2024.jpg
PC: ./backup/sdcard/DCIM/Camera/2024.jpg
⚡ High-Speed Scanning: Uses direct Android ContentProvider queries instead of slow recursive scanning.
📦 Comprehensive Backup:
Images & Videos (Camera, WhatsApp, Telegram, etc.)
Documents (PDF, DOCX, TXT)
Archives (ZIP, RAR, 7Z)
Installed Apps (Extracts .apk files automatically!)
🌑 Dark Mode: Professional UI designed for modern Linux environments.
📝 Manifest System: Generates a backup_manifest.json for detailed tracking of every backup session.
📸 Screenshots
(Please upload a screenshot of your app here. Example: ![Dashboard](screenshots/dashboard.png))

📥 Installation
Prerequisites
You need adb installed on your system.

# Debian / Ubuntu / Mint
sudo apt install adb

# Fedora
sudo dnf install android-tools

# Arch Linux
sudo pacman -S android-tools
Install KSwitch
Download the latest .deb or .rpm file from the Releases Page.

Debian/Ubuntu:

sudo dpkg -i kswitch_1.0.0_amd64.deb
Fedora/RedHat:

sudo rpm -i kswitch-1.0.0.x86_64.rpm
🚀 How to Use
Enable USB Debugging on your Android phone (Settings > Developer Options).
Connect your phone to your PC via USB.
Open KSwitch.
Click "Scan Device". Accept the RSA prompt on your phone screen if asked.
Select the categories you want to backup (Images, Apps, etc.).
Click "Start Backup".
Enjoy your coffee ☕ while KSwitch does the work.
⚠️ Limitations (v1.0)
Contacts & Call Logs: Currently supported for Backup Only (Saved as VCF/XML). Direct restore to phone is restricted by Android security policies without a helper app.
App Data: Backs up the Application Installer (.apk) only. Does not backup internal app data (login sessions, game saves) as this requires Root access.
☕ Support the Development
I built this tool because I was tired of struggling with Android backups on Linux. If KSwitch saved you time or kept your data safe, consider supporting ZyrenLab.

<a href="https://www.buymeacoffee.com/ZyrenLab" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" ></a>

⚖️ License
This project is licensed under the GNU General Public License v3.0.
You are free to use, study, modify, and distribute this software, provided that any derivative works are also open-source.

Copyright (C) 2024 ZyrenLab