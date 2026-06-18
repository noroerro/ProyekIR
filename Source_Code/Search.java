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
    public static HashMap<Integer, Integer> docLength = null;

    public static void main(String[] args) throws FileNotFoundException {
        String path = "./Dokumen/cranfield"; // Path folder dokumen cranfield
        // I : Data Indexing
        // 1. Mendapatkan semua file yang ada di folder dokumen
        File[] files = InvertedIndex.getAllFiles(path);
        // 2. Membuat inverted index dari semua file yang ada di folder dokumen
        HashMap<Integer, String> fileIndex = null;
        HashMap<String, LinkedList<Posting>> invertedIndex = null;
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
            Map<Integer, Integer> queryRelevance = null;
            if (pilihan.equals("1")) {
                System.out.print("Pilih (1 - 225) untuk query evaluasi: ");
                int queryId = Integer.parseInt(sc.nextLine().trim());
                if (queryId > 225 || queryId < 1) {
                    System.out.println("Masukkan query yg valid!");
                    continue;
                }
                query = queries.get(queryId);
                queryRelevance = FileReader.bacaRelevance(queryId);
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

            int K = 10;
            if (queryRelevance != null) {
                System.out.print("Masukkan nilai K untuk Precision@K: ");
                try {
                    K = Integer.parseInt(sc.nextLine().trim());
                    if (K <= 0) {
                        System.out.println("Nilai K harus lebih besar dari 0. Menggunakan default K = 10.");
                        K = 10;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Input tidak valid. Menggunakan default K = 10.");
                    K = 10;
                }
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
                    if (peringkat > 5)
                        break;

                    int docId = entry.getKey();
                    double skor = entry.getValue();
                    String namaFile = fileIndex.get(docId);

                    System.out.printf("%d. %s (Skor: %.4f)\n", peringkat, namaFile, skor);
                    peringkat++;
                }
                System.out.printf("Total dokumen yang di retrieve: %d\n", hasilBIM.size());
                if (queryRelevance != null) {
                    System.out.printf("Dokumen relevan yang didapat: %d\n", hitungHasilRelevan(queryRelevance, hasilBIM));
                    System.out.printf("Total dokumen relevan: %d\n", hitungDokumenRelevan(queryRelevance));
                    System.out.printf("Precision: %f\n", hitungPrecision(queryRelevance, hasilBIM));
                    System.out.printf("Recall: %f\n", hitungRecall(queryRelevance, hasilBIM));
                    System.out.printf("Precision@%d: %f\n", K, hitungPrecisionAtK(queryRelevance, hasilBIM, K));
                    System.out.printf("11-Point AP: %f\n", hitung11PointAP(queryRelevance, hasilBIM));
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
                    if (peringkat > 5)
                        break;

                    int docId = entry.getKey();
                    double skor = entry.getValue();
                    String namaFile = fileIndex.get(docId);

                    System.out.printf("%d. %s (Skor: %.4f)\n", peringkat, namaFile, skor);
                    peringkat++;
                }
                System.out.printf("Total dokumen yang di retrieve: %d\n", hasilTwoPoisson.size());
                if (queryRelevance != null) {
                    System.out.printf("Dokumen relevan yang didapat: %d\n",
                            hitungHasilRelevan(queryRelevance, hasilTwoPoisson));
                    System.out.printf("Total dokumen relevan: %d\n", hitungDokumenRelevan(queryRelevance));
                    System.out.printf("Precision: %f\n", hitungPrecision(queryRelevance, hasilTwoPoisson));
                    System.out.printf("Recall: %f\n", hitungRecall(queryRelevance, hasilTwoPoisson));
                    System.out.printf("Precision@%d: %f\n", K, hitungPrecisionAtK(queryRelevance, hasilTwoPoisson, K));
                    System.out.printf("11-Point AP: %f\n", hitung11PointAP(queryRelevance, hasilTwoPoisson));
                }
            }

            System.out.println("\n==============================");
            System.out.println("HASIL RANKING BM25");
            System.out.println("==============================");

            List<Map.Entry<Integer, Double>> hasilBM25 = BM25Model.hitungBM25(query, invertedIndex, fileIndex);

            if (hasilBM25.isEmpty()) {
                System.out.println("Tidak ada dokumen yang relevan dengan query.");
            } else {
                int peringkat = 1;
                for (Map.Entry<Integer, Double> entry : hasilBM25) {
                    if (peringkat > 5)
                        break;

                    int docId = entry.getKey();
                    double skor = entry.getValue();
                    String namaFile = fileIndex.get(docId);

                    System.out.printf("%d. %s (Skor: %.4f)\n", peringkat, namaFile, skor);
                    peringkat++;
                }
                System.out.printf("Total dokumen yang di retrieve: %d\n", hasilBM25.size());
                if (queryRelevance != null) {
                    System.out.printf("Dokumen relevan yang didapat: %d\n", hitungHasilRelevan(queryRelevance, hasilBM25));
                    System.out.printf("Total dokumen relevan: %d\n", hitungDokumenRelevan(queryRelevance));
                    System.out.printf("Precision: %f\n", hitungPrecision(queryRelevance, hasilBM25));
                    System.out.printf("Recall: %f\n", hitungRecall(queryRelevance, hasilBM25));
                    System.out.printf("Precision@%d: %f\n", K, hitungPrecisionAtK(queryRelevance, hasilBM25, K));
                    System.out.printf("11-Point AP: %f\n", hitung11PointAP(queryRelevance, hasilBM25));
                }
            }

            System.out.println("\n==============================");
            System.out.println("HASIL RANKING BM11");
            System.out.println("==============================");

            List<Map.Entry<Integer, Double>> hasilBM11 = BM11Model.hitungBM11(query, invertedIndex, fileIndex);

            if (hasilBM11.isEmpty()) {
                System.out.println("Tidak ada dokumen yang relevan dengan query.");
            } else {
                int peringkat = 1;
                for (Map.Entry<Integer, Double> entry : hasilBM11) {
                    if (peringkat > 5)
                        break;

                    int docId = entry.getKey();
                    double skor = entry.getValue();
                    String namaFile = fileIndex.get(docId);

                    System.out.printf("%d. %s (Skor: %.4f)\n", peringkat, namaFile, skor);
                    peringkat++;
                }
                System.out.printf("Total dokumen yang di retrieve: %d\n", hasilBM11.size());
                if (queryRelevance != null) {
                    System.out.printf("Dokumen relevan yang didapat: %d\n", hitungHasilRelevan(queryRelevance, hasilBM11));
                    System.out.printf("Total dokumen relevan: %d\n", hitungDokumenRelevan(queryRelevance));
                    System.out.printf("Precision: %f\n", hitungPrecision(queryRelevance, hasilBM11));
                    System.out.printf("Recall: %f\n", hitungRecall(queryRelevance, hasilBM11));
                    System.out.printf("Precision@%d: %f\n", K, hitungPrecisionAtK(queryRelevance, hasilBM11, K));
                    System.out.printf("11-Point AP: %f\n", hitung11PointAP(queryRelevance, hasilBM11));
                }
            }

        }
        sc.close();
    }

    public static List<Map.Entry<Integer, Double>> urutkanDokumen(HashMap<Integer, Double> scores) {
        List<Map.Entry<Integer, Double>> listDokumen = new ArrayList<>(scores.entrySet());
        // ngurutin score dokumen dari yang terbesar ke yang terkecil
        listDokumen.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        return listDokumen;
    }

    private static int hitungHasilRelevan(Map<Integer, Integer> queryRelevance,
            List<Map.Entry<Integer, Double>> hasil) {

        int hasilRelevan = 0;
        for (Map.Entry<Integer, Double> docScore : hasil) {
            int docId = docScore.getKey();

            if (queryRelevance.getOrDefault(docId, 0) > 0) {
                hasilRelevan++;
            }
        }
        return hasilRelevan;
    }

    private static double hitungPrecision(Map<Integer, Integer> queryRelevance,
            List<Map.Entry<Integer, Double>> hasil) {
        int hasilRelevan = hitungHasilRelevan(queryRelevance, hasil);

        return (double) hasilRelevan / hasil.size();
    }

    private static double hitungRecall(Map<Integer, Integer> queryRelevance, List<Map.Entry<Integer, Double>> hasil) {
        int hasilRelevan = hitungHasilRelevan(queryRelevance, hasil);
        int dokumenRelevan = hitungDokumenRelevan(queryRelevance);

        return (double) hasilRelevan / (double) dokumenRelevan;
    }

    private static int hitungDokumenRelevan(Map<Integer, Integer> queryRelevance) {
        int dokumenRelevan = 0;
        for (int rel : queryRelevance.values()) {
            if (rel > 0) {
                dokumenRelevan++;
            }
        }
        return dokumenRelevan;
    }

    private static double hitungPrecisionAtK(Map<Integer, Integer> queryRelevance,
            List<Map.Entry<Integer, Double>> hasil, int k) {
        if (queryRelevance == null || hasil == null || hasil.isEmpty() || k <= 0) {
            return 0.0;
        }
        int limit = Math.min(k, hasil.size());
        int hasilRelevan = 0;
        for (int i = 0; i < limit; i++) {
            int docId = hasil.get(i).getKey();
            if (queryRelevance.getOrDefault(docId, 0) > 0) {
                hasilRelevan++;
            }
        }
        return (double) hasilRelevan / limit;
    }

    private static double hitung11PointAP(Map<Integer, Integer> queryRelevance,
            List<Map.Entry<Integer, Double>> hasil) {
        if (queryRelevance == null || hasil == null || hasil.isEmpty()) {
            return 0.0;
        }

        int totalRelevan = 0;
        for (int rel : queryRelevance.values()) {
            if (rel > 0) {
                totalRelevan++;
            }
        }
        if (totalRelevan == 0) {
            return 0.0;
        }

        List<Double> recalls = new ArrayList<>();
        List<Double> precisions = new ArrayList<>();

        int foundRelevan = 0;
        for (int i = 0; i < hasil.size(); i++) {
            int docId = hasil.get(i).getKey();
            if (queryRelevance.getOrDefault(docId, 0) > 0) {
                foundRelevan++;
                double recall = (double) foundRelevan / totalRelevan;
                double precision = (double) foundRelevan / (i + 1);
                recalls.add(recall);
                precisions.add(precision);
            }
        }

        double sumInterpolated = 0.0;
        for (int l = 0; l <= 10; l++) {
            double level = l / 10.0;
            double maxPrecision = 0.0;
            for (int i = 0; i < recalls.size(); i++) {
                if (recalls.get(i) >= level) {
                    if (precisions.get(i) > maxPrecision) {
                        maxPrecision = precisions.get(i);
                    }
                }
            }
            sumInterpolated += maxPrecision;
        }

        return sumInterpolated / 11.0;
    }
}