# pbo2
Tugas5
1. Deskripsi Program
Aplikasi Penghitung Kata adalah aplikasi berbasis Java Swing yang berfungsi untuk menghitung jumlah:
kata,
karakter termasuk spasi,
karakter tanpa spasi,
kalimat,
paragraf,
dari teks yang dimasukkan oleh pengguna di dalam JTextArea.
Program juga memiliki fitur pencarian kata, sorot hasil pencarian, serta menyimpan hasil analisis.
2. Komponen GUI yang Digunakan
Komponen utama:
JFrame → jendela utama aplikasi.
JPanel → pembungkus layout.
JTextArea → tempat input teks oleh pengguna.
JScrollPane → membuat JTextArea bisa di-scroll.
JLabel → menampilkan hasil perhitungan seperti jumlah kata, karakter, dll.
JButton → tombol "Hitung", "Cari", "Bersihkan Sorot".
JTextField → input kata yang ingin dicari.
JMenuBar → menu atur file (simpan, bersihkan sorot).
3. Logika Program
Logika utama menggunakan:
BreakIterator untuk menghitung kata dan kalimat secara akurat menurut bahasa Indonesia.
String manipulation untuk menghitung karakter, menyingkirkan spasi, dan memisahkan paragraf.
Regex untuk pencarian kata/frasa.
Highlighter untuk menyorot kata yang dicari.
4. Events yang Digunakan
ActionListener:
tombol Hitung → memproses teks dan update statistik.
tombol Cari → mencari dan menyorot teks.
tombol Bersihkan Sorot → menghapus highlight.
DocumentListener:
mendeteksi perubahan realtime dalam JTextArea → hitungan langsung berubah tanpa menekan tombol.
5. Variasi yang Ditambahkan
Hitungan paragraf berdasarkan baris kosong.
Pencarian kata beserta highlight dalam teks.
Simpan data ke file .txt berisi teks asli dan statistik hasil analisis.

Tugas6
1. Deskripsi Program
Aplikasi Cek Cuaca menggunakan API OpenWeatherMap untuk mengambil data cuaca secara realtime berdasarkan kota yang dipilih/diinput oleh pengguna.
Data yang ditampilkan:
kota dan negara,
temperatur,
kondisi cuaca,
kelembapan,
kecepatan angin,
waktu update,
ikon cuaca sesuai kondisi (cerah, hujan, mendung, dll).
2. Komponen GUI yang Digunakan
JFrame → jendela utama aplikasi.
JPanel → menyusun layout panel atas, tengah, dan tabel.
JTextField → input nama kota.
JComboBox → daftar kota favorit.
JButton:
Cek cuaca,
Tambah kota favorit,
Simpan ke CSV,
Muat CSV.
JLabel → menampilkan detail cuaca.
JLabel + ImageIcon → menampilkan ikon cuaca.
JTable → menampilkan riwayat cuaca yang pernah dicek.
JMenuBar → opsi file dan pengaturan API Key.
3. Logika Program
Menghubungkan API OpenWeatherMap dengan HttpURLConnection.
Mengirim request HTTP GET untuk mengambil data JSON.
Parsing JSON manual menggunakan regex sederhana.
Menampilkan ikon cuaca berdasarkan kode ikon dari API.
Data cuaca dipetakan ke objek WeatherData.
Menyimpan data riwayat ke CSV.
Memuat data CSV ke JTable.
4. Events yang Digunakan
ActionListener:
Tombol Cek Cuaca → memanggil API cuaca.
Tombol Tambah Favorit → menambahkan kota ke daftar favorit.
Tombol Simpan CSV → simpan tabel ke file.
Tombol Muat CSV → load file CSV.
Menu Ubah API Key → memasukkan API key baru.
ItemListener:
JComboBox kota favorit → ketika dipilih, otomatis memasukkan nama kota ke input.
KeyListener:
Menekan ENTER pada input kota → memanggil check cuaca.
5. Variasi yang Ditambahkan
Menyimpan kota favorit ke file eksternal lalu memuat ulang saat aplikasi dibuka.
Menyimpan riwayat cek cuaca ke CSV.
Memuat CSV untuk ditampilkan kembali pada JTable.
Menampilkan ikon cuaca sesuai API.
