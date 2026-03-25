# So'z Boyligi — Android Loyihasi

## Fayl strukturasi
```
app/src/main/
├── kotlin/uz/sozboyligi/app/
│   └── MainActivity.kt          ← Asosiy kod
├── assets/
│   └── logo.png                 ← Splash screen rasmi
├── res/xml/
│   └── network_security_config.xml
└── AndroidManifest.xml
```

## Android Studio da ochish
1. Android Studio → Open → bu papkani tanlang
2. Sync Gradle
3. Build → Generate Signed APK

## O'zgartirishlar
- Splash screen: `assets/logo.png` rasmi ko'rsatiladi
- Internet yo'q bo'lsa: "Internetga ulaning" dialog chiqadi
- OK bosganda internet bor bo'lsa sayt ochiladi, yo'q bo'lsa dialog yana chiqadi
- Splash matn ("Made with 81-school 8A-class") olib tashlangan
