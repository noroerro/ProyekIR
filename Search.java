package ProyekIR;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Search {
    // Stop Word
    public static Set<String> stopwords = new HashSet<>(Arrays.asList(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
            "has", "he", "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "were", "will", "with"));

    public static void main(String[] args) {
        String path = "./Dokumen"; // Ganti dengan path folder dokumen yang sesuai
        // I : Data Indexing
        // 1. Mendapatkan semua file yang ada di folder dokumen
        File[] files = getAllFiles(path);
        // 2. Membuat inverted index dari semua file yang ada di folder dokumen
        HashMap<Integer, String> fileIndex = null;
        HashMap<String, LinkedList<Integer>> invertedIndex = null;
        try {
            // fileIndex u/ menyimpan nama file sebagai nomor
            fileIndex = new HashMap<>();
            // Inverted Index
            invertedIndex = createInvertedIndex(files, fileIndex);
            // file index tidak di return karena mengirim alamat file ke fungsi
            // createInvertedIndex, sehingga tidak perlu dikembalikan lagi

        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
        }
        System.out.println("File Index : " + fileIndex);
        System.out.println("Inverted Index : " + invertedIndex);

        // Scanner untuk query yang ingin dicari 
        Scanner sc = new Scanner(System.in);
        System.out.print("\nMasukkan kata yang ingin dicari: ");
        String query = sc.nextLine();
        sc.close();

        // Query dimasukkan ke dalam array (setiap kata yang dipisah oleh spasi akan dimasukkan ke array)
        String[] daftarKata = query.split("\\s+");
        String hasilPreProcessing = "";

        // Looping untuk setiap kata yang ada di query (di array daftarKata)
        for(String kata: daftarKata){
            // Setiap kata yang ada di query akan dilakukan preProcessing dahulu
            kata = preProcessing(kata);

            // Jika di inverted index terdapat kata pada query, maka hasil pre processing adalah kata tersebut
            if(invertedIndex.containsKey(kata)){
                hasilPreProcessing = kata;
            } else{ // Jika di inverted index tidak ada kata pada query, maka akan dihitung edit distance antara kata pada query dengan 
                    // setiap kata di inverted index, lalu hasil pre processing adalah kata yang memiliki edit distance paling kecil dengan kata pada query
                int minDistance = Integer.MAX_VALUE;

                // Looping ke semua kata (keys) di inverted index
                for (String kataDiIndex : invertedIndex.keySet()) {
                    
                    // Panggil fungsi edit distance untuk menghitung jarak edit distance antara kata pada query dengan kata di index
                    int jarak = hitungEditDistance(kata, kataDiIndex);
                    
                    // Update jika nemu jarak yang lebih kecil
                    if (jarak < minDistance) {
                        minDistance = jarak;
                        hasilPreProcessing = kataDiIndex;
                    }
                }
            }
            
            // Cek jika hasil preProcessing tidak kosong
            if (!hasilPreProcessing.equals("")) {
                // Jika hasil preProcessing sama dengan kata pada query, maka kata tersebut ditemukan pada indeks
                if(hasilPreProcessing.equals(kata)){
                    // Ini ntar bisa dihapus aja, ini cuma buat ngecek hasil preProcessing nya aja 
                    System.out.println("Query: '" + kata + "' ditemukan.");
                    System.out.println("Dokumen: " + invertedIndex.get(hasilPreProcessing));
                } else { //jika hasil preProcessing tidak sama dengan kata pada query, maka kata tersebut tidak ada pada indeks
                         // dan hasil preProcessing adalah perhitungan dan kata rekomendasi dari edit distance

                    // Ini ntar bisa dihapus aja, ini cuma buat ngecek hasil + biar gampang nanti cek boolean model nya
                    System.out.println("Query: '" + kata + "' tidak ditemukan.");
                    System.out.println("Did you mean '" + hasilPreProcessing + "'?");
                    System.out.println("Dokumen: " + invertedIndex.get(hasilPreProcessing));
                }
            } else { // Jika hasil preProcessing kosong, maka kata pada query tidak ditemukan dan tidak ada rekomendasi dari edit distance
                System.out.printf("Kata '%s' tidak ditemukan di indeks.\n", kata);
            }
        }
    }

    public static File[] getAllFiles(String path) {
        // Mendapatkan semua file yang ada di folder dokumen
        File folder = new File(path);
        // Mendapatkan semua file yang ada di folder dokumen
        File[] listFiles = folder.listFiles();
        return listFiles;
    }

    public static String preProcessing(String word) {
        // Pre processing sederhana
        // Mengubah semua menjadi lowercase
        word = word.toLowerCase();
        // Menghapus spasi tambahan
        word = word.trim();
        // Menghapus tanda baca
            //Step 1a
                if (word.endsWith("sses")) {
                    // sses -> ss
                    word = word.substring(0, word.length() - 2);
                }
                if (word.endsWith("ies")) {
                    //ies -> i
                    word = word.substring(0, word.length() - 2);
                }
                if (word.endsWith("s")){
                    // s -> (hapus s)
                    word = word.substring(0, word.length() - 1);
                }
            // Step 3
                if (word.endsWith("ing") && word.length() > 4) {
                    // ing -> (hapus ing)
                    word = word.substring(0, word.length() - 3);
                }
        
        // Stemming
        return word;
    }

    public static HashMap<String, LinkedList<Integer>> createInvertedIndex(File[] files,
            HashMap<Integer, String> fileIndex) throws FileNotFoundException {
        HashMap<String, LinkedList<Integer>> invertedIndex = new HashMap<>();
        Scanner sc;
        int counter = 0;
        // Looping semua file yang ada di folder dokumen
        for (File file : files) {
            // Bila merupakan sebuah file bertipe teks
            if (file.isFile() && file.getName().endsWith(".txt")) {
                // Buat scanner untuk membaca file
                sc = new Scanner(file);
                counter++; // Menambahkan nomor indeks untuk setiap file
                fileIndex.put(counter, file.getName()); // Menyimpan nama file dengan nomor indeks
                // Looping semua kata yang ada di file
                while (sc.hasNext()) {
                    String kata = sc.next(); // Memanggil fungsi preProcessing untuk memproses kata
                    if (stopwords.contains(kata)) { // Cek apakah kata termasuk stop word atau tidak
                        continue; // Jika termasuk stop word, lewati kata tersebut
                    }
                    // Pre processing 
                    kata = preProcessing(kata);

                    // Memakai LinkedList untuk menyimpan nama file yang mengandung kata tersebut
                    LinkedList<Integer> tempList;
                    // Bila belum terdapat di inverted index, maka perlu dimasukkan beserta nama
                    // filenya
                    if (!invertedIndex.containsKey(kata)) {
                        tempList = new LinkedList<>();
                        // Masukkan nama file ke dalam list
                        tempList.add(counter);
                        invertedIndex.put(kata, tempList);
                    } else {
                        if (!invertedIndex.get(kata).contains(counter)) {// Cek apakah nama file sudah ada di
                                                                                // list atau belum
                            // Masukkan nama file ke dalam list
                            invertedIndex.get(kata).add(counter);
                        }
                    }
                }
            }
        }

        return invertedIndex;
    }

    // Menghitung edit distance antara dua string (antara kata di query dan kata di index)
    public static int hitungEditDistance(String query, String cari){
        // Inisialisasi array berukuran 2, dimana array ke 0 menyimpan kata dari query dan array ke 1 menyimpan kata dari index
        String [] arr = new String[2];
        arr[0] = query;
        arr[1] = cari;

        // Memanggil fungsi potongAkhir untuk memotong huruf yang sama di akhir pada kedua string, 
        // lalu simpan hasilnya ke dalam variable hasilPotong_kata1 dan hasilPotong_kata2 agar mudah digunakan nantinya
        String[] hasilPotong = potongAkhir(arr);
        String hasilPotong_kata1 = hasilPotong[0];
        String hasilPotong_kata2 = hasilPotong[1];
        
        // Inisialisasi 2 variable string 
        String hasilAkhir_kata1, hasilAkhir_kata2;

        // Jika hasil potong kata ke 1 dan kata ke 2 lebih besar dari 0, maka panggil fungsi potongAwal untuk memotong huruf yang sama 
        // di awal pada kedua string (jika ada) dan disimpan pada variable yang sudah di inisialisasi sebelumnya
        if(hasilPotong_kata1.length() > 0 && hasilPotong_kata2.length() > 0){
            hasilPotong = potongAwal(hasilPotong);
            hasilAkhir_kata1 = hasilPotong[0];
            hasilAkhir_kata2 = hasilPotong[1];   
        } else{ //jika panjang hasil potong kata ke 1 atau kata ke 2 sama dengan 0, maka hasil akhir dari kedua kata tersebut adalah hasil potong sebelumnya
            hasilAkhir_kata1 = hasilPotong_kata1;
            hasilAkhir_kata2 = hasilPotong_kata2;
        }        
    
        int distance = 0;
        int [][] arrEditDistance; // Array untuk menyimpan serta menghitung edit distance dari setiap posisi huruf pada kedua string

        // Jika panjang hasil akhir dari kata ke 1 dan kata ke 2 lebih besar sama dengan 1, maka lakukan perhitungan edit distance menggunakan algoritma Levenshtein Distance
        if(hasilAkhir_kata1.length() >= 1 && hasilAkhir_kata2.length() >= 1){
            // Inisialisasi array edit distance dengan ukuran baris sebanyak panjang hasil akhir dari kata ke-1 + 1 dan 
            // ukuran kolom sebanyak panjang hasil akhir dari kata ke 2 + 1 (keduanya di +1 karena untuk menyimpan jarak dari posisi 0 
            // sampai posisi panjang kata)
            arrEditDistance  = new int [hasilAkhir_kata1.length() + 1][hasilAkhir_kata2.length() + 1];
            
            // Isi kolom pertama dari array dari 0 sampai panjang kata
            for(int i = 0; i <= hasilAkhir_kata1.length(); i++){
                arrEditDistance[i][0] = i;
            }
            // Isi baris pertama dari array dari 1 sampai panjang kata
            for(int j = 1; j <= hasilAkhir_kata2.length(); j++){
                arrEditDistance[0][j] = j;
            }
            // Hitung edit distance untuk setiap posisi huruf pada kedua string
            for(int i = 1; i <= hasilAkhir_kata1.length(); i++){
                for(int j = 1; j <= hasilAkhir_kata2.length(); j++){
                    // Jika kedua huruf sama, maka ambil nilai edit distance pada posisi diagonal kiri atas (do nothing)
                    if(hasilAkhir_kata1.charAt(i-1) == hasilAkhir_kata2.charAt(j-1)){
                        arrEditDistance[i][j] = arrEditDistance[i-1][j-1];
                    } else { // Jika kedua huruf berbeda, maka ambil nilai edit distance pada posisi diagonal kiri atas, posisi kiri, dan posisi atas,
                            //  kemudian ambil nilai edit distance paling minimum dari ketiga posisi tersebut, lalu tambahkan 1 
                        arrEditDistance[i][j] = 1 + Math.min(arrEditDistance[i-1][j-1], Math.min(arrEditDistance[i-1][j], arrEditDistance[i][j-1]));
                    }
                }
            }
            // Nilai edit distance dari kedua string adalah nilai yang berada pada posisi paling kanan bawah dari array edit distance
            distance = arrEditDistance[hasilAkhir_kata1.length()][hasilAkhir_kata2.length()];
        } else if(hasilAkhir_kata1.length() == 0 || hasilAkhir_kata2.length() == 0){ // Jika hasil akhir dari kata ke 1 atau kata ke 2 sama dengan 0,
                                                                                     // maka edit distance nya adalah maksimal panjang dari antara kedua kata tersebut
            distance = Math.max(hasilAkhir_kata1.length(), hasilAkhir_kata2.length());
        }

        //kembalikan angka distance yang sudah didapatkan dari perhitungan edit distance
        return distance;
    }

    //Memperpendek string dengan memotong huruf yang sama di akhir pada kedua string
    public static String [] potongAkhir(String [] arr){
        // Ambil array ke 0 sebagai kata dari query (kata ke-1) dan array ke 1 sebagai kata dari index (kata ke-2) 
        String query = arr[0];
        String cari = arr[1];

        // Iterator i dan j (posisi huruf yang di cek) di-inisialisasi dari akhir kedua string
        int i = query.length() - 1;
        int j = cari.length() - 1;

        // Loop selama nilai i dan j masih memiliki huruf (lebih besar sama dengan 0) 
        // dan selama kedua huruf dari kata ke-1 dan kata ke-2 sama, jika kedua huruf sama, maka posisi i dan j dikurang 1
        while (i >= 0 && j >= 0 && query.charAt(i) == cari.charAt(j)) {
            i--;
            j--;
        }
        // Jika loop berhenti (kondisi sudah tidak terpenuhi), maka potong kedua string dari posisi awal (0) hingga
        // posisi i+1 untuk kata ke-1 dan posisi j+1 untuk kata ke-2, lalu kembalikan dalam bentuk array
        return new String[]{query.substring(0, i+1), cari.substring(0, j+1)};
    }

    //Memperpendek string dengan memotong huruf yang sama di awal pada kedua string
    public static String [] potongAwal(String [] arr){
        // Ambil array ke 0 sebagai kata dari query (kata ke-1) dan array ke 1 sebagai kata dari index (kata ke-2) 
        String query = arr[0];
        String cari = arr[1];

        // Iterator i dan j (posisi huruf yang di cek) di-inisialisasi dari akhir kedua string
        int i = query.length() - 1;
        int j = cari.length() - 1;

        // Inisialisasi posisi awal dari huruf yaitu 0
        int posisiAwal = 0;

        // Loop selama posisi awal dari kedua huruf masih lebih kecil dari panjang kata ke-1 dan kata ke-2
        // dan selama kedua huruf dari kata ke-1 dan kata ke-2 sama, jika kedua huruf sama, maka posisi awal ditambah 1 
        while (posisiAwal <= i && posisiAwal <= j && query.charAt(posisiAwal) == cari.charAt(posisiAwal)) {
            posisiAwal++;
        }

        // Jika loop berhenti (kondisi sudah tidak terpenuhi), maka potong kedua string dari 
        // isi variable posisiAwal hingga akhir string, lalu kembalikan dalam bentuk array
        return new String[]{query.substring(posisiAwal), cari.substring(posisiAwal)};
    }
}
