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
}
