# Tugas Praktikum 2
Nama: Nafisya Ghalia

NIM: 124140100
## News Feed Simulator

Aplikasi News Feed Simulator menggunakan Kotlin.

### Fitur
1. Flow untuk mensimulasikan berita baru setiap 2 detik.
2. Filter berita berdasarkan kategori.
3. Transform data berita menjadi format tampilan.
4. StateFlow untuk menyimpan jumlah berita yang sudah dibaca.
5. Coroutine untuk mengambil detail berita secara asynchronous.

## Struktur Proyek

'''text
├── shared/            # Module shared (Logic, ViewModel, Repository, Flows)


│   └── src/commonMain # Kode
'''

## Screenshoot Aplikasi
tampilan utama apk

<img width="490" height="691" alt="Screenshot 2026-09-25 225020" src="https://github.com/user-attachments/assets/a63c6e84-77b5-4ff9-9a06-817be0c99181" />

----
filter berita

<img width="237" height="527" alt="Screenshot 2026-09-25 232959" src="https://github.com/user-attachments/assets/9d4e5e29-9369-41ae-a77a-c3299fae5f99" />

----
update data terbaca

<img width="241" height="532" alt="Screenshot 2026-09-25 233022" src="https://github.com/user-attachments/assets/ce5b994a-6f4b-4202-9dff-bf9301f070e8" />

----
