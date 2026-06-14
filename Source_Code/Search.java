package Source_Code;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Search {
    // Rata-rata panjang dokumen (Average Document Length)
    public static double avgDocLength = 0.0;

    // Stop Word
    public static Set<String> stopwords = new HashSet<>(Arrays.asList(
            "a", "an", "as", "at", "be", "by", "for", "from",
            "has", "he", "in", "is", "it", "its", "of", "on",
            "that", "the", "to", "was", "were", "will", "with",
            "this", "which", "who", "whom", "there", "their", "and", "or", "not"));

    public static void main(String[] args) {
        String path = "./Dokumen"; // Ganti dengan path folder dokumen yang sesuai
        // I : Data Indexing
        // 1. Mendapatkan semua file yang ada di folder dokumen
        File[] files = getAllFiles(path);
        // 2. Membuat inverted index dari semua file yang ada di folder dokumen
        HashMap<Integer, String> fileIndex = null;
        HashMap<String, LinkedList<Posting>> invertedIndex = null;
        HashMap<Integer, Integer> docLength = null;
        try {
            // fileIndex u/ menyimpan nama file sebagai nomor
            fileIndex = new HashMap<>();
            docLength = new HashMap<>();
            // Inverted Index
            invertedIndex = createInvertedIndex(files, fileIndex, docLength);

            // Menghitung Rata-rata Panjang Dokumen (Average Document Length)
            double totalLength = 0;
            for (int len : docLength.values()) {
                totalLength += len;
            }
            avgDocLength = docLength.isEmpty() ? 0 : totalLength / docLength.size();
            System.out.println("Proses Indexing Selesai. Rata-rata panjang dokumen: " + avgDocLength);

            // file index tidak di return karena mengirim alamat file ke fungsi
            // createInvertedIndex, sehingga tidak perlu dikembalikan lagi

            // Test Print (Uncomment untuk melihat hasil)
            // System.out.println("=== File Index ===");
            // fileIndex.forEach((key, value) -> System.out.println(key + " : " + value));

            // System.out.println("\n=== Inverted Index ===");
            // invertedIndex.forEach((key, value) -> System.out.println(key + " : " +
            // value));
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
        }

        // Scanner untuk query yang ingin dicari
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("\nMasukkan kata yang ingin dicari: ");
            String query = sc.nextLine();

            if (query.equals("-1")) {
                System.out.println("Program berhasil diberhentikan");
                break;
            }

            BooleanQueryParser bqp = new BooleanQueryParser(invertedIndex);
            System.out.println("TOKENIZE:" + Arrays.toString(bqp.tokenize(query)));
            System.out.println("HASIL POSTFIX: " + Arrays.toString(bqp.infixToPostfix(bqp.tokenize(query))));
            System.out
                    .println("HASIL QUERY: " + bqp.evaluatePostfix(bqp.infixToPostfix(bqp.tokenize(query))).toString());
        
        System.out.println("\n==============================");
        System.out.println("HASIL RANKING BIM");
        System.out.println("==============================");
        
        List<Map.Entry<Integer, Double>> hasilBIM = hitungBIM(query, invertedIndex, fileIndex);
        
        // jika tidak ada dokumen yang relevan
        if (hasilBIM.isEmpty()) {
            System.out.println("Tidak ada dokumen yang relevan dengan query.");
        } else {
            // Menampilkan 5 dokumen teratas
            int peringkat = 1;
            for (Map.Entry<Integer, Double> entry : hasilBIM) {
                if (peringkat > 5) break; 
                
                int docId = entry.getKey();
                double skor = entry.getValue();
                String namaFile = fileIndex.get(docId);
                
                System.out.printf("%d. %s (Skor: %.4f)\n", peringkat, namaFile, skor);
                peringkat++;
            }
        }

        System.out.println("\n==============================");
        System.out.println("HASIL RANKING Two Poisson Model");
        System.out.println("==============================");
        
        List<Map.Entry<Integer, Double>> hasilTwoPoisson = hitungTwoPoisson(query, invertedIndex, fileIndex);
        
        // jika tidak ada dokumen yang relevan
        if (hasilTwoPoisson.isEmpty()) {
            System.out.println("Tidak ada dokumen yang relevan dengan query.");
        } else {
            // Menampilkan 5 dokumen teratas
            int peringkat = 1;
            for (Map.Entry<Integer, Double> entry : hasilTwoPoisson) {
                if (peringkat > 5) break; 
                
                int docId = entry.getKey();
                double skor = entry.getValue();
                String namaFile = fileIndex.get(docId);
                
                System.out.printf("%d. %s (Skor: %.4f)\n", peringkat, namaFile, skor);
                peringkat++;
            }
        }

        System.out.println("\n==============================");
        System.out.println("HASIL RANKING BM25");
        System.out.println("==============================");
        
        List<Map.Entry<Integer, Double>> hasilBM25 = hitungBM25(query, invertedIndex, fileIndex, docLength);
        
        // jika tidak ada dokumen yang relevan
        if (hasilBM25.isEmpty()) {
            System.out.println("Tidak ada dokumen yang relevan dengan query.");
        } else {
            // Menampilkan 5 dokumen teratas
            int peringkat = 1;
            for (Map.Entry<Integer, Double> entry : hasilBM25) {
                if (peringkat > 5) break; 
                
                int docId = entry.getKey();
                double skor = entry.getValue();
                String namaFile = fileIndex.get(docId);
                
                System.out.printf("%d. %s (Skor: %.4f)\n", peringkat, namaFile, skor);
                peringkat++;
            }
        }

            // Query dimasukkan ke dalam array (setiap kata yang dipisah oleh spasi akan
            // dimasukkan ke array)
            String[] daftarKata = query.split("\\s+");

            // Mendapatkan semua doc ID untuk keperluan not
            Set<Integer> semuaDocId = new HashSet<>(fileIndex.keySet());

            // Flag untuk menandai apakah kata tersebut diikuti negasi(not)
            boolean isNegated = false;

            // Looping untuk setiap kata yang ada di query (di array daftarKata)
            for (String kata : daftarKata) {
                String hasilPreProcessing = "";

                // Setiap kata yang ada di query akan dilakukan preProcessing dan porterStemmer
                // dahulu
                kata = preProcessing(kata);

                // Buang kata boolean (and dan or)
                if (kata.equals("and") || kata.equals("or")) {
                    isNegated = false; // reset negasi jika ketemu AND/OR
                    continue;
                }

                // Jika kata adalah not, maka set negasi jadi true
                if (kata.equals("not")) {
                    isNegated = true;
                    continue;
                }
                // Stemmer kata
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
                    System.out.println("==============================");

                    // Ambil posting list dari kata yang ditemukan
                    LinkedList<Posting> postingList = invertedIndex.get(hasilPreProcessing);

                    // jika kata mengandung negasi (not)
                    if (isNegated) {
                        // Jika di-negasi (not), ambil semua dokumen selain yang mengandung kata
                        // tersebut
                        Set<Integer> hasilNot = new HashSet<>(semuaDocId);
                        if (postingList != null) {
                            hasilNot.removeAll(postingList);
                        }
                        // Urutkan hasil dokumen not yg ditemukan
                        LinkedList<Integer> sortedResult = new LinkedList<>(new java.util.TreeSet<>(hasilNot));

                        // jika hasil preProcessing ada di inverted index (kondisi not)
                        if (hasilPreProcessing.equals(kata)) {
                            System.out.println(
                                    "Kata NOT '" + kata + "' ditemukan.");
                            System.out.println("Dokumen: " + sortedResult);
                        } else {// jika hasil preProcessing tidak ada di inverted index (kondisi not)
                            System.out.println("Kata: '" + kata + "' tidak ditemukan.");
                            System.out.println("Did you mean '" + hasilPreProcessing + "'?");
                            System.out.println("Dokumen yang mengandung NOT '" + hasilPreProcessing
                                    + "'" + sortedResult);
                        }
                    } else {
                        // jika hasil preprocessing ditemukan
                        if (hasilPreProcessing.equals(kata)) {
                            // Ini ntar bisa dihapus aja, ini cuma buat ngecek hasil preProcessing nya aja
                            System.out.println("Kata: '" + kata + "' ditemukan.");
                            System.out.println("Dokumen: " + postingList);
                        } else { // jika hasil preProcessing tidak sama dengan kata pada query, maka kata
                                 // tersebut tidak ada pada indeks
                                 // dan hasil preProcessing adalah perhitungan dan kata rekomendasi dari edit
                                 // distance

                            // Ini ntar bisa dihapus aja, ini cuma buat ngecek hasil + biar gampang nanti
                            // cek boolean model nya
                            System.out.println("Kata: '" + kata + "' tidak ditemukan.");
                            System.out.println("Did you mean '" + hasilPreProcessing + "'?");
                            System.out.println("Dokumen: " + postingList);
                        }
                    }
                } else { // Jika hasil preProcessing kosong, maka kata pada query tidak ditemukan dan
                         // tidak ada rekomendasi dari edit distance
                    System.out.println("==============================");
                    System.out.printf("Kata '%s' tidak ditemukan di indeks.\n", kata);
                }
                // Reset negasi
                isNegated = false;
            }
        }
        sc.close();
    }

    /**
     * Mendapatkan daftar file di dalam direktori dokumen.
     */
    public static File[] getAllFiles(String path) {
        File folder = new File(path);
        return folder.listFiles();
    }

    /**
     * Melakukan preprocessing pada kata (lowercase, trim, dan hapus tanda baca).
     *
     * @param word kata yang akan diproses
     * @return kata hasil preprocessing yang hanya berisi huruf
     */
    public static String preProcessing(String word) {
        word = word.toLowerCase().trim();
        return word.replaceAll("[^a-zA-Z]", "");
    }

    /**
     * Membuat Inverted Index beserta perhitungan Term Frequency (TF) dan Document
     * Length.
     *
     * @param files     array dari file-file dokumen yang akan diindeks
     * @param fileIndex peta (map) untuk menyimpan pemetaan ID dokumen ke nama file
     * @param docLength peta (map) untuk menyimpan panjang setiap dokumen
     * @return inverted index yang memetakan setiap term ke daftar posting-nya
     * @throws FileNotFoundException jika file dokumen tidak ditemukan
     */
    public static HashMap<String, LinkedList<Posting>> createInvertedIndex(File[] files,
            HashMap<Integer, String> fileIndex, HashMap<Integer, Integer> docLength) throws FileNotFoundException {
        HashMap<String, LinkedList<Posting>> invertedIndex = new HashMap<>();
        Scanner sc;
        int counter = 0;

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                sc = new Scanner(file);
                counter++;
                fileIndex.put(counter, file.getName());
                docLength.put(counter, 0);

                while (sc.hasNext()) {
                    String kata = preProcessing(sc.next());

                    if (stopwords.contains(kata)) {
                        continue;
                    }

                    kata = Stemmer.doPorterStemmer(kata);
                    if (kata.isEmpty()) {
                        continue;
                    }

                    // Increment panjang dokumen (hanya kata valid setelah stopword & stemming)
                    docLength.put(counter, docLength.get(counter) + 1);

                    if (!invertedIndex.containsKey(kata)) {
                        LinkedList<Posting> posting = new LinkedList<>();
                        posting.add(new Posting(counter, 1));
                        invertedIndex.put(kata, posting);
                    } else {
                        LinkedList<Posting> posting = invertedIndex.get(kata);
                        Posting lastPo = posting.getLast();

                        if (lastPo.getDocId() == counter) {
                            lastPo.incrementTermFrequency(); // Increment TF jika kata muncul lagi di dokumen yang sama
                        } else {
                            posting.add(new Posting(counter, 1)); // Buat posting baru jika di dokumen baru
                        }
                    }
                }
                sc.close();
            }
        }

        return invertedIndex;
    }

    /**
     * Membersihkan query dari stopword dan melakukan stemming pada setiap kata di
     * dalamnya.
     *
     * @param query string query asli yang dimasukkan oleh pengguna
     * @return daftar kata (terms) yang sudah bersih dari stopword dan telah di-stem
     */
    public static List<String> getQueryClean(String query) {
        String[] daftarKata = query.split(" ");
        List<String> kataBersih = new ArrayList<>();

        for (String kata : daftarKata) {
            kata = preProcessing(kata);
            if (stopwords.contains(kata) || kata.isEmpty()) {
                continue;
            }
            kata = Stemmer.doPorterStemmer(kata);
            if (!kata.isEmpty()) {
                kataBersih.add(kata);
            }
        }
        return kataBersih;
    }

    /**
     * Menghitung skor kemiripan dokumen terhadap query menggunakan Binary
     * Independence Model (BIM).
     *
     * @param query         string query yang dicari
     * @param invertedIndex inverted index dari koleksi dokumen
     * @param fileIndex     pemetaan antara ID dokumen dengan nama filenya
     */
    public static List<Map.Entry<Integer, Double>> hitungBIM(String query, HashMap<String, LinkedList<Posting>> invertedIndex,
            HashMap<Integer, String> fileIndex) {
        List<String> queryTerms = getQueryClean(query);

        if (queryTerms.isEmpty()) {
            System.out.println("Query tidak valid atau hanya berisi stopword.");
            return new ArrayList<>();
        }

        int N = fileIndex.size();
        HashMap<Integer, Double> docScores = new HashMap<>();

        for (String term : queryTerms) {

            if (!invertedIndex.containsKey(term)) {
                continue;
            }

            LinkedList<Posting> postings = invertedIndex.get(term);
            int df = postings.size();

            double weight = Math.log((N - df + 0.5) / (df + 0.5));
            // double weight = Math.log((N - df)/df);

            for (Posting posting : postings) {
                int docId = posting.getDocId();
                docScores.put(docId, docScores.getOrDefault(docId, 0.0) + weight);
            }
        }
        return urutkanDokumen(docScores);
    }

    /**
     * Menghitung skor kemiripan dokumen terhadap query menggunakan Two Poisson Model.
     *
     * @param query         string query yang dicari
     * @param invertedIndex inverted index dari koleksi dokumen
     * @param fileIndex     pemetaan antara ID dokumen dengan nama filenya
     */
    public static List<Map.Entry<Integer, Double>> hitungTwoPoisson(String query, HashMap<String, LinkedList<Posting>> invertedIndex,
            HashMap<Integer, String> fileIndex) {
        List<String> queryTerms = getQueryClean(query);

        if (queryTerms.isEmpty()) {
            System.out.println("Query tidak valid atau hanya berisi stopword.");
            return new ArrayList<>();
        }

        int N = fileIndex.size();
        HashMap<Integer, Double> docScores = new HashMap<>();

        for (String term : queryTerms) {

            if (!invertedIndex.containsKey(term)) {
                continue;
            }

            LinkedList<Posting> postings = invertedIndex.get(term);
            int df = postings.size();
            
            double k = 1.5; //ini parameter k nya, bisa di tuning

            double weight = Math.log((N - df + 0.5) / (df + 0.5));

            for (Posting posting : postings) {
                int docId = posting.getDocId();
                int tf = posting.getTermFrequency();
                double weightTwoPoisson = (tf * (k + 1) * weight) / (tf + k);
                docScores.put(docId, docScores.getOrDefault(docId, 0.0) + weightTwoPoisson);
            }
        }
        return urutkanDokumen(docScores);
    }

        /**
     * Menghitung skor kemiripan dokumen terhadap query menggunakan BM25.
     *
     * @param query         string query yang dicari
     * @param invertedIndex inverted index dari koleksi dokumen
     * @param fileIndex     pemetaan antara ID dokumen dengan nama filenya
     * @param doclength     menyimpan panjang setiap dokumen untuk perhitungan BM25
     */
    public static List<Map.Entry<Integer, Double>> hitungBM25(String query, HashMap<String, LinkedList<Posting>> invertedIndex,
            HashMap<Integer, String> fileIndex, HashMap<Integer, Integer> docLength) {
        List<String> queryTerms = getQueryClean(query);

        if (queryTerms.isEmpty()) {
            System.out.println("Query tidak valid atau hanya berisi stopword.");
            return new ArrayList<>();
        }

        int N = fileIndex.size();
        double lavg = avgDocLength; // Rata-rata panjang dokumen (Average Document Length)
        double k1 = 1.5; //ini parameter k nya, bisa di tuning
        double b = 0.75; //ini parameter b nya, bisa di tuning


        HashMap<Integer, Double> docScores = new HashMap<>();

        for (String term : queryTerms) {

            if (!invertedIndex.containsKey(term)) {
                continue;
            }

            LinkedList<Posting> postings = invertedIndex.get(term);
            int df = postings.size();
            

            double weight = Math.log((N - df + 0.5) / (df + 0.5));

            for (Posting posting : postings) {
                int docId = posting.getDocId();
                int tf = posting.getTermFrequency();
                int ld = docLength.get(docId);
                double weightBM25 = (tf * (k1 + 1) * weight) / (tf + (k1 * ld / lavg) * b + k1 * (1-b));
                docScores.put(docId, docScores.getOrDefault(docId, 0.0) + weightBM25);
            }
        }
        return urutkanDokumen(docScores);
    }

    public static List<Map.Entry<Integer, Double>> urutkanDokumen(HashMap<Integer, Double> scores) {
        List<Map.Entry<Integer, Double>> listDokumen = new ArrayList<>(scores.entrySet());
        //ngurutin score dokumen dari yang terbesar ke yang terkecil
        listDokumen.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        return listDokumen;
    }
}