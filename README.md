# CANLI TV PRO — Android APK Projesi

Bu proje, **CANLI TV PRO** HTML5 uygulamasını tam özellikli bir Android WebView APK'sına dönüştürmek için oluşturulmuştur.

## Özellikler
- **Paket Adı:** `com.halilkilic.canlitvpro`
- **Sürüm:** `2026.11.18.02` (`versionCode: 2026111802`)
- **HTTP Yayın Desteği (Cleartext):** Açık (IPTV ve FM HTTP akışları çalışır)
- **Direksiyon / Medya Tuşları:** WebView klavye köprüsü aktif
- **Ekranı Açık Tut:** Aktif
- **Tam Ekran Video & Dosya Seçici:** Aktif

## 1. Yöntem: Bilgisayar Kurulumu Olmadan GitHub Actions ile APK Almak (Önerilen)
1. GitHub'da yeni bir depo oluşturun (ör. `canli-tv-pro-apk`).
2. Bu ZIP dosyasının içindeki tüm dosya ve klasörleri (`.github` klasörü dahil) depoya yükleyin.
3. GitHub deponuzda **Actions** sekmesine girin; **"Android APK Derle"** iş akışı otomatik çalışacaktır (~90 saniye).
4. İşlem tamamlandığında sayfanın altındaki **Artifacts** bölümünden `app-debug.apk` dosyasını indirip Android cihazınıza veya araç multimedya ekranınıza kurun.

## 2. Yöntem: Android Studio ile Yerel Derleme
1. Bu klasörü **Android Studio** ile açın (`File > Open`).
2. Gradle senkronizasyonunun bitmesini bekleyin.
3. Menüden **Build > Build Bundle(s) / APK(s) > Build APK(s)** yolunu izleyin.
