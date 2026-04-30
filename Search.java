package ProyekIR;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.LinkedList;

public class Search {

    public static void main(String[] args) {
        String path = "./Dokumen"; // Ganti dengan path folder dokumen yang sesuai
        // I : Data Indexing
            // 1. Mendapatkan semua file yang ada di folder dokumen
            File[] files = getAllFiles(path);
            // 2. Membuat inverted index dari semua file yang ada di folder dokumen
            TreeMap<String, LinkedList<String>> invertedIndex = null;
            try {
                invertedIndex = createInvertedIndex(files);
            } catch (Exception e) {
                System.out.println("Error : " + e.getMessage());
            }
            System.out.println("Inverted Index : " + invertedIndex);
    }

    public static File[] getAllFiles(String path) {
        // Mendapatkan semua file yang ada di folder dokumen
        File folder = new File(path);
        // Mendapatkan semua file yang ada di folder dokumen
        File[] listFiles = folder.listFiles();
        return listFiles;
    }

    public static TreeMap<String, LinkedList<String>> createInvertedIndex(File[] files) throws FileNotFoundException {
        TreeMap<String, LinkedList<String>> invertedIndex = new TreeMap<>();
        Scanner sc;
        // Looping semua file yang ada di folder dokumen
        for (File file : files) {
            // Bila merupakan sebuah file bertipe teks
            if (file.isFile() && file.getName().endsWith(".txt")) {
                // Buat scanner untuk membaca file
                sc = new Scanner(file);
                // Looping semua kata yang ada di file
                while (sc.hasNext()) {
                    String kata = sc.next().toLowerCase();// lowercase untuk menyamarakatan kata yang sama dengan huruf
                                                          // kapital atau kapitil
                    // Memakai LinkedList untuk menyimpan nama file yang mengandung kata tersebut
                    LinkedList<String> tempList;
                    // Bila belum terdapat di inverted index, maka perlu dimasukkan beserta nama
                    // filenya
                    if (!invertedIndex.containsKey(kata)) {
                        tempList = new LinkedList<>();
                        // Masukkan nama file ke dalam list
                        tempList.add(file.getName());
                        invertedIndex.put(kata, tempList);
                    } else {
                        if (!invertedIndex.get(kata).contains(file.getName())) {// Cek apakah nama file sudah ada di
                                                                                // list atau belum
                            // Masukkan nama file ke dalam list
                            invertedIndex.get(kata).add(file.getName());
                        }
                    }
                }
            }
        }

        return invertedIndex;
    }
}
