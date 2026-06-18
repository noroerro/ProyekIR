# Proyek Information Retrieval: Implementasi Mesin Pencari Probabilistik

**Dikerjakan oleh:**
* Kenneth Nathanael - 6182301018
* Gregorius Jason Maresi - 6182301055
* Andrew Kevin Alexander - 6182301046

## Cara Menjalankan Program

Terdapat 2 cara untuk menjalankan program:

### 1. Menggunakan Tombol Play
1. Buka folder `Source_Code`.
2. Klik file `Search.java`.
3. Setelah file `Search.java` terbuka, klik tombol play (Run Java di bagian kanan atas).
4. Pilih opsi menu yang tersedia di terminal.
5. Ketik -1 untuk memberhentikan program.

### 2. Menggunakan Terminal
1. Buka terminal.
2. Pastikan terminal berada di path yang benar sesuai direktori proyek.
3. Di path `...\ProyekIR>`, kompilasi program dengan mengetik:
   ```bash
   javac Source_Code/*.java
   ```
4. Setelah itu, jalankan program dengan mengetik:
   ```bash
   java Source_Code.Search
   ```
5. Pilih opsi menu yang tersedia di terminal.
6. Ketik -1 untuk memberhentikan program.

## Panduan Query

Pada sistem input query sendiri, gunakan query berbahasa inggris formal atau memilih menggunakan pilihan query yang sudah ada (input angka 1 - 225)

## Model Retrieval

Sistem ini mengimplementasikan 4 model probabilistik untuk melakukan ranking dokumen terhadap query.

### Estimasi Probabilitas (Semua Model)

Semua model menggunakan estimasi probabilitas berikut dari *relevant set*:

- **pt** = (rt + 0.5) / (R + 1.0) — probabilitas term muncul di dokumen relevan
- **ut** = (df − rt + 0.5) / (N − R + 1.0) — probabilitas term muncul di dokumen non-relevan

Dimana: 
- R = jumlah dokumen relevan
- rt = dokumen relevan yang mengandung term
- N = total dokumen
- df = dokumen yang mengandung term.

### 1. Binary Independence Model (BIM)

BIM menghitung **Retrieval Status Value (RSV)** berdasarkan probabilitas kemunculan term tanpa memperhitungkan frekuensi term (TF) maupun panjang dokumen:

```
RSV(d) = Σ log₁₀(pt / ut)
```

### 2. Two-Poisson Model

Two-Poisson Model mengasumsikan bahwa setiap dokumen di dalam koleksi memiliki panjang atau total jumlah term yang sama. Parameter k (default: 1.5) mengontrol bobot frekuensi kata, sehingga kenaikan term frequency (tf) pada suatu dokumen tidak akan membuat skornya naik secara drastis.


```
wt = log₁₀(pt / ut)

Skor(d) = Σ [ tf × (k + 1) × wt / (tf + k) ] 
```

Dimana: 
- Nt = jumlah dokumen yang mengandung term.
- rt = jumlah dokumen relevan yang mengandung term.
- pt = probabilitas term muncul pada dokumen relevan.
- ut = probabilitas term muncul pada dokumen non-relevan.
- k = nilai konstan dengan rentang 1 ≤  k ≤ 2
- wt = bobot yang dimiliki oleh suatu term 


### 3. BM25

BM25 memperluas Two-Poisson dengan menambahkan **normalisasi panjang dokumen**. Parameter b (default: 0.75) mengontrol pengaruh panjang dokumen, dan k1 (default: 1.5) mengontrol saturasi TF:

```
wt = log₁₀(pt / ut)

Skor(d) = Σ [ tf × (k1 + 1) x wt / (tf + (k1 x ld / lavg) * b + k x (1 − b)) ]

```
Dimana : 
- Nt = jumlah dokumen yang mengandung term.
- rt = jumlah dokumen relevan yang mengandung term.
- pt = probabilitas term muncul pada dokumen relevan.
- ut = probabilitas term muncul pada dokumen non-relevan.
- k = nilai konstan dengan rentang 1 ≤  k ≤ 2
- wt = bobot yang dimiliki oleh suatu term 
- ld = panjang dokumen D
- lavg = rata-rata panjang dokumen dalam koleksi dokumen
- b = nilai konstan

### 4. BM11

BM11 adalah varian BM25 **tanpa komponen (1 − b)**, sehingga normalisasi panjang dokumen sepenuhnya bergantung pada rasio panjang dokumen terhadap rata-rata:

```
wt = log₁₀(pt / ut)

Skor(d) = Σ [ tf × (k + 1)  x wt / (tf + (k × ld / lavg)) ]
```
Dimana : 
- Nt = jumlah dokumen yang mengandung term.
- rt = jumlah dokumen relevan yang mengandung term.
- pt = probabilitas term muncul pada dokumen relevan.
- ut = probabilitas term muncul pada dokumen non-relevan.
- k = nilai konstan dengan rentang 1 ≤  k ≤ 2
- wt = bobot yang dimiliki oleh suatu term 
- ld = panjang dokumen D
- lavg = rata-rata panjang dokumen dalam koleksi dokumen