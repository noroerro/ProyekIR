package ProyekIR;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

public class Search {

    public static void main(String[] args) {
        String path = "SearchEngine\\Dokumen";
        // I : Data Indexing 
            //1. Mendapatkan semua file yang ada di folder dokumen
            File[] files = getAllFiles(path);
            //2. Membuat inverted index dari semua file yang ada di folder dokumen
            TreeMap<String, TreeSet<String>> invertedIndex = null;
            try {
                invertedIndex = createInvertedIndex(files);
            } catch (Exception e) {
                System.out.println("Error : " + e.getMessage());
            }
            System.out.println("Inverted Index : " + invertedIndex);
    }

    public static File[] getAllFiles(String path){
        // Mendapatkan semua file yang ada di folder dokumen
        File folder = new File(path);
        // Mendapatkan semua file yang ada di folder dokumen
        File[] listFiles = folder.listFiles();
        return listFiles;
    }

    public static TreeMap<String, TreeSet<String>> createInvertedIndex(File[] files) throws FileNotFoundException{
        TreeMap<String, TreeSet<String>> invertedIndex = new TreeMap<>();
        Scanner sc;
        // Looping semua  file yang ada di folder dokumen
        for (File file : files) {
            //Bila merupakan sebuah file bertipe teks
            if (file.isFile() && file.getName().endsWith(".txt")) {
                // Buat scanner untuk membaca file
                sc = new Scanner(file);
                // Looping semua kata yang ada di file
                while(sc.hasNext()){
                    String kata = sc.next().toLowerCase();//lowercase untuk menyamarakatan kata yang sama dengan huruf kapital atau kapitil
                    // Memakai treeSet untuk mencegah duplikasi nama file
                    TreeSet<String> tempList;
                    // Bila belum terdapat di inverted index, maka perlu dimasukkan beserta nama filenya
                    if(!invertedIndex.containsKey(kata)){
                        tempList = new TreeSet<>();
                        // Masukkan nama file ke dalam list
                        tempList.add(file.getName());
                        invertedIndex.put(kata, tempList);
                    }else {
                        // Masukkan nama file ke dalam list
                        invertedIndex.get(kata).add(file.getName());
                    }
                }
            }
        }

        return invertedIndex;
    }
}
