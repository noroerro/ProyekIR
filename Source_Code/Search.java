package Source_Code;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;

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

        // Scanner untuk query yang ingin dicari
        Scanner sc = new Scanner(System.in);
        System.out.print("\nMasukkan kata yang ingin dicari: ");
        String query = sc.nextLine();

        BooleanQueryParser bqp = new BooleanQueryParser(invertedIndex);
        System.out.println("TOKENIZE:" + Arrays.toString(bqp.tokenize(query)));
        System.out.println("HASIL POSTFIX: " + Arrays.toString(bqp.infixToPostfix(bqp.tokenize(query))));
        System.out.println("HASIL QUERY: " + bqp.evaluatePostfix(bqp.infixToPostfix(bqp.tokenize(query))).toString());
        sc.close();

        // Query dimasukkan ke dalam array (setiap kata yang dipisah oleh spasi akan
        // dimasukkan ke array)
        String[] daftarKata = query.split("\\s+");

        // Looping untuk setiap kata yang ada di query (di array daftarKata)
        for (String kata : daftarKata) {
            String hasilPreProcessing = "";

            // Setiap kata yang ada di query akan dilakukan preProcessing dan porterStemmer
            // dahulu
            kata = preProcessing(kata);
            kata = Stemmer.doPorterStemmer(kata);

            // Jika di inverted index terdapat kata pada query, maka hasil pre processing
            // adalah kata tersebut
            if (invertedIndex.containsKey(kata)) {
                hasilPreProcessing = kata;
            } else { // Jika di inverted index tidak ada kata pada query, maka akan dihitung edit
                     // distance antara kata pada query dengan
                     // setiap kata di inverted index, lalu hasil pre processing adalah kata yang
                     // memiliki edit distance paling kecil dengan kata pada query
                int minDistance = Integer.MAX_VALUE;

                // Looping ke semua kata (keys) di inverted index
                for (String kataDiIndex : invertedIndex.keySet()) {

                    // Panggil fungsi edit distance untuk menghitung jarak edit distance antara kata
                    // pada query dengan kata di index
                    int jarak = LevenshteinDistance.hitungEditDistance(kata, kataDiIndex);
                    // Update jika nemu jarak yang lebih kecil
                    if (jarak < minDistance) {
                        minDistance = jarak;
                        hasilPreProcessing = kataDiIndex;
                    }
                }
            }

            // Cek jika hasil preProcessing tidak kosong
            if (!hasilPreProcessing.equals("")) {
                // Jika hasil preProcessing sama dengan kata pada query, maka kata tersebut
                // ditemukan pada indeks
                if (hasilPreProcessing.equals(kata)) {
                    // Ini ntar bisa dihapus aja, ini cuma buat ngecek hasil preProcessing nya aja
                    System.out.println("Query: '" + kata + "' ditemukan.");
                    System.out.println("Dokumen: " + invertedIndex.get(hasilPreProcessing));
                } else { // jika hasil preProcessing tidak sama dengan kata pada query, maka kata
                         // tersebut tidak ada pada indeks
                         // dan hasil preProcessing adalah perhitungan dan kata rekomendasi dari edit
                         // distance

                    // Ini ntar bisa dihapus aja, ini cuma buat ngecek hasil + biar gampang nanti
                    // cek boolean model nya
                    System.out.println("Query: '" + kata + "' tidak ditemukan.");
                    System.out.println("Did you mean '" + hasilPreProcessing + "'?");
                    System.out.println("Dokumen: " + invertedIndex.get(hasilPreProcessing));
                }
            } else { // Jika hasil preProcessing kosong, maka kata pada query tidak ditemukan dan
                     // tidak ada rekomendasi dari edit distance
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
        word = word.replaceAll("[^a-zA-Z]", "");
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

                    // Pre processing sederhana
                    kata = preProcessing(kata);

                    // Cek apakah kata termasuk stop word atau tidak
                    if (stopwords.contains(kata)) {
                        continue; // Jika termasuk stop word, lewati kata tersebut
                    }

                    // Masukkan kata ke porter stemmer untuk mendapatkan bentuk dasar kata
                    kata = Stemmer.doPorterStemmer(kata);
                    if (kata.equals("")) {
                        continue; // Jika kata setelah pre processing dan porter stemmer menjadi kosong, lewati
                                  // kata tersebut
                    }

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

}