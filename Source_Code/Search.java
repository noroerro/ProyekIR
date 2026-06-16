package Source_Code;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Search {
    // Rata-rata panjang dokumen (Average Document Length)
    public static double avgDocLength = 0.0;

    public static void main(String[] args) throws FileNotFoundException {
        String path = "./Dokumen/cranfield"; // Path folder dokumen cranfield
        // I : Data Indexing
        // 1. Mendapatkan semua file yang ada di folder dokumen
        File[] files = InvertedIndex.getAllFiles(path);
        // 2. Membuat inverted index dari semua file yang ada di folder dokumen
        HashMap<Integer, String> fileIndex = null;
        HashMap<String, LinkedList<Posting>> invertedIndex = null;
        HashMap<Integer, Integer> docLength = null;
        try {
            // fileIndex u/ menyimpan nama file sebagai nomor
            fileIndex = new HashMap<>();
            docLength = new HashMap<>();
            // Inverted Index
            invertedIndex = InvertedIndex.createInvertedIndex(files, fileIndex, docLength);

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
        // Membaca semua query test dari file query.txt
        Map<Integer, String> queries = FileReader.bacaSemuaQuery();

        // Scanner untuk query yang ingin dicari
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n==============================");
            System.out.println("MENU QUERY");
            System.out.println("==============================");
            System.out.println("1. Gunakan Query Evaluasi (Cranfield)");
            System.out.println("2. Masukkan Query Sendiri");
            System.out.println("-1. Keluar");
            System.out.print("\nPilih opsi: ");
            String pilihan = sc.nextLine();

            if (pilihan.equals("-1")) {
                System.out.println("Program berhasil diberhentikan");
                break;
            }

            String query;
            if (pilihan.equals("1")) {
                System.out.print("Pilih (1 - 225) untuk query evaluasi: ");
                int queryId = Integer.parseInt(sc.nextLine().trim());
                if (queryId>255 || queryId<1) {
                    System.out.println("Masukkan query yg valid!");
                    continue;
                }
                query = queries.get(queryId);
                System.out.println("Query yang dipilih: " + query);
            } else if (pilihan.equals("2")) {
                System.out.print("Masukkan query: ");
                query = sc.nextLine();
                if (query.trim().isEmpty()) {
                    System.out.println("Query tidak boleh kosong.");
                    continue;
                }
            } else {
                System.out.println("Opsi tidak valid.");
                continue;
            }


            System.out.println("\n==============================");
            System.out.println("HASIL RANKING BIM Model");
            System.out.println("==============================");
            // === BIM Ranking ===
            List<Map.Entry<Integer, Double>> hasilBIM = BIMModel.hitungBIM(query, invertedIndex, fileIndex);

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

            List<Map.Entry<Integer, Double>> hasilTwoPoisson = TwoPoissonModel.hitungTwoPoisson(query, invertedIndex, fileIndex);

            if (hasilTwoPoisson.isEmpty()) {
                System.out.println("Tidak ada dokumen yang relevan dengan query.");
            } else {
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

            if (hasilBM25.isEmpty()) {
                System.out.println("Tidak ada dokumen yang relevan dengan query.");
            } else {
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
        }
        sc.close();
    }

    /**
     * Menghitung skor kemiripan dokumen terhadap query menggunakan BM25.
     *
     * @param query         string query yang dicari
     * @param invertedIndex inverted index dari koleksi dokumen
     * @param fileIndex     pemetaan antara ID dokumen dengan nama filenya
     * @param doclength     menyimpan panjang setiap dokumen untuk perhitungan BM25
     */
    public static List<Map.Entry<Integer, Double>> hitungBM25(String query,
            HashMap<String, LinkedList<Posting>> invertedIndex,
            HashMap<Integer, String> fileIndex, HashMap<Integer, Integer> docLength) {
        List<String> queryTerms = TextPreprocessor.getQueryClean(query);

        if (queryTerms.isEmpty()) {
            System.out.println("Query tidak valid atau hanya berisi stopword.");
            return new ArrayList<>();
        }

        int N = fileIndex.size();
        double lavg = avgDocLength; // Rata-rata panjang dokumen (Average Document Length)
        double k1 = 1.5; // ini parameter k nya, bisa di tuning
        double b = 0.75; // ini parameter b nya, bisa di tuning

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
                double weightBM25 = (tf * (k1 + 1) * weight) / (tf + (k1 * ld / lavg) * b + k1 * (1 - b));
                docScores.put(docId, docScores.getOrDefault(docId, 0.0) + weightBM25);
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
     * @param doclength     menyimpan panjang setiap dokumen untuk perhitungan BM11
     */
    public static List<Map.Entry<Integer, Double>> hitungBM11(String query,
            HashMap<String, LinkedList<Posting>> invertedIndex,
            HashMap<Integer, String> fileIndex, HashMap<Integer, Integer> docLength) {
        List<String> queryTerms = TextPreprocessor.getQueryClean(query);

        if (queryTerms.isEmpty()) {
            System.out.println("Query tidak valid atau hanya berisi stopword.");
            return new ArrayList<>();
        }

        int N = fileIndex.size();
        double lavg = avgDocLength; // Rata-rata panjang dokumen (Average Document Length)
        double k1 = 1.5; // ini parameter k nya, bisa di tuning

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
                double weightBM25 = (tf * (k1 + 1) * weight) / (tf + (k1 * ld / lavg));
                docScores.put(docId, docScores.getOrDefault(docId, 0.0) + weightBM25);
            }
        }
        return urutkanDokumen(docScores);
    }

    public static List<Map.Entry<Integer, Double>> urutkanDokumen(HashMap<Integer, Double> scores) {
        List<Map.Entry<Integer, Double>> listDokumen = new ArrayList<>(scores.entrySet());
        // ngurutin score dokumen dari yang terbesar ke yang terkecil
        listDokumen.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        return listDokumen;
    }
}