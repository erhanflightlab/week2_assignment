# DevCluster

Donanım envanterinden kaynak tahsis etmek, kalan stoku izlemek ve toplam maliyeti hesaplamak için geliştirilmiş bir Android uygulaması.

**Geliştirici:** Erhan Bülbül  
**Proje:** week2_assignment

## Kullanım

Donanımlar ad, kategori veya kimlik ile aranabilir. Kategori seçimi arama ile birlikte çalışır. “Tahsis et” bir adet kaynak ayırır; “Geri al” tahsis edilen bir kaynağı envantere döndürür. Kartın gövdesine tıklamak da bir adet tahsis eder.

Stok tükendiğinde yeni tahsis engellenir. Tahsis bulunmayan donanımda geri alma düğmesi pasiftir. Üstteki adet ve maliyet bilgileri, seçili filtreden bağımsız olarak bütün tahsisleri kapsar.

Arama, kategori seçimi ve tahsis miktarları ekran dönüşünde korunur. Ekran yüksekliği azaldığında başlık daha az yer kaplayan bir düzene geçer. Uygulama, ödevde verilen sekiz kayıtla çalışır; sunucu bağlantısı veya veritabanı kullanmaz.

## Teknik yapı

- Kotlin, Jetpack Compose ve Material 3.
- `HardwareItem.kt`: Tüm alanları `val` olan, `@Immutable` ile işaretlenmiş veri modeli.
- `LabHardwareInventory.kt`: Ödevde verilen başlangıç envanteri.
- `MainActivity.kt`: Merkezi durum yönetimi ve ekran bileşenleri.

Durum `AllocationDeskScreen` içinde tutulur. Bileşenler değerleri parametre olarak alır, kullanıcı olaylarını callback ile üst bileşene iletir. Kalan stok, toplam maliyet ve filtre sonuçları ilgili girdilerle anahtarlanmış `remember` bloklarında hesaplanır.

`AllocationSummaryPanel` tahsis özetini, `InventoryCategoryStrip` kategori seçimlerini gösterir. Tahsis ve geri alma işlemlerinin stok sınırları `changeReservedQuantity` içinde kontrol edilir. `AllocationDeskPreview`, ekranın tasarım önizlemesini sağlar.

Donanım listesi `LazyColumn` kullanır. Her kaydın anahtarı `id` alanıdır. `HardwareCard` ve `HardwareSearchBar` içinde yerel durum bulunmaz.

## Çalıştırma

1. Proje kök klasörünü Android Studio ile açın.
2. Gradle eşitlemesinin tamamlanmasını bekleyin.
3. API 24 veya üzeri bir Android cihaz ya da emülatör seçin.
4. `app` yapılandırmasını çalıştırın.

## Kontrol senaryoları

- “pro” aramasında MacBook, Pixel ve HHKB olmak üzere üç kayıt listelenir.
- “Yapay Zeka / GPU” kategorisinde iki kayıt bulunur; aynı kategoride “amd” araması sonucu bire indirir.
- Başka tahsis yapılmadan dört monitör ayrıldığında kalan stok sıfır, toplam maliyet 4.796,00 olur. Bir monitör geri alındığında maliyet 3.597,00 olur.
- Başlangıç stoku sıfır olan HHKB için tahsis yapılamaz.
- Ekran döndürüldüğünde arama, kategori, tahsis miktarları ve toplam maliyet korunur.

