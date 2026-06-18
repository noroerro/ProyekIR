package Source_Code;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
    public static HashMap<Integer, Integer> docLength = null;

    // Parameter eksperimen
    private static int kPrecision = 10; // Nilai K untuk evaluasi Precision@K

    public static void main(String[] args) throws FileNotFoundException {
        String path = "./Dokumen/Cranfield"; // Path folder dokumen cranfield
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
            System.out.println("3. Eksperimen kPrecision (Perbandingan Model pada Precision@K)");
            System.out.println("4. Perbandingan Keseluruhan Model (All Metrics)");
            System.out.println("-1. Keluar");
            System.out.print("\nPilih opsi: ");
            String pilihan = sc.nextLine();

            if (pilihan.equals("-1")) {
                System.out.println("Program berhasil diberhentikan");
                break;
            }

            // Eksperimen kPrecision: bandingkan Precision@K semua model
            if (pilihan.equals("3")) {
                int[] kValues = {5, 10, 15, 20,25};
                System.out.println("\n==============================");
                System.out.println("EKSPERIMEN kPrecision - Perbandingan Model");
                System.out.println("Rata-rata dari " + queries.size() + " query");
                System.out.println("==============================\n");

                // Header tabel
                System.out.printf("%-6s | %-10s | %-12s | %-12s | %-12s%n", "K", "BIM", "Two-Poisson", "BM25", "BM11");
                System.out.println("-------|------------|--------------|--------------|-------------");

                for (int kVal : kValues) {
                    double totalBIM = 0, totalTP = 0, totalBM25 = 0, totalBM11 = 0;
                    int queryCount = 0;

                    for (Map.Entry<Integer, String> qEntry : queries.entrySet()) {
                        int queryId = qEntry.getKey();
                        String queryText = qEntry.getValue();
                        Map<Integer, Integer> relevance;
                        try {
                            relevance = FileReader.bacaRelevance(queryId);
                        } catch (FileNotFoundException e) {
                            continue;
                        }

                        // Convert relevance map to Set<Integer> for model training
                        Set<Integer> relevantSet = getRelevantSet(relevance);

                        // Run all 4 models with ground truth relevant set
                        List<Map.Entry<Integer, Double>> hasilBIM = BIMModel.hitungBIM(queryText, invertedIndex, fileIndex, relevantSet);
                        List<Map.Entry<Integer, Double>> hasilTP = TwoPoissonModel.hitungTwoPoisson(queryText, invertedIndex, fileIndex, relevantSet);
                        List<Map.Entry<Integer, Double>> hasilBM25 = BM25Model.hitungBM25(queryText, invertedIndex, fileIndex, relevantSet);
                        List<Map.Entry<Integer, Double>> hasilBM11 = BM11Model.hitungBM11(queryText, invertedIndex, fileIndex, relevantSet);

                        if (!hasilBIM.isEmpty() && !hasilTP.isEmpty() && !hasilBM25.isEmpty() && !hasilBM11.isEmpty()) {
                            totalBIM += hitungPrecisionAtK(relevance, hasilBIM, kVal);
                            totalTP += hitungPrecisionAtK(relevance, hasilTP, kVal);
                            totalBM25 += hitungPrecisionAtK(relevance, hasilBM25, kVal);
                            totalBM11 += hitungPrecisionAtK(relevance, hasilBM11, kVal);
                            queryCount++;
                        }
                    }

                    double avgBIM = queryCount > 0 ? totalBIM / queryCount : 0;
                    double avgTP = queryCount > 0 ? totalTP / queryCount : 0;
                    double avgBM25 = queryCount > 0 ? totalBM25 / queryCount : 0;
                    double avgBM11 = queryCount > 0 ? totalBM11 / queryCount : 0;
                    System.out.printf("%-6d | %-10.4f | %-12.4f | %-12.4f | %-12.4f%n",
                            kVal, avgBIM, avgTP, avgBM25, avgBM11);
                }
                System.out.println("\nCatatan: Rata-rata dihitung dari query yang memiliki hasil di semua model.");
                continue;
            }

            // Eksperimen 4: Perbandingan keseluruhan model
            if (pilihan.equals("4")) {
                System.out.println("\n==============================");
                System.out.println("PERBANDINGAN KESELURUHAN MODEL");
                System.out.println("Rata-rata dari " + queries.size() + " query");
                System.out.println("==============================\n");

                double totalPrecBIM = 0, totalRecallBIM = 0, totalAPBIM = 0, totalP10BIM = 0;
                double totalPrecTP = 0, totalRecallTP = 0, totalAPTP = 0, totalP10TP = 0;
                double totalPrecBM25 = 0, totalRecallBM25 = 0, totalAPBM25 = 0, totalP10BM25 = 0;
                double totalPrecBM11 = 0, totalRecallBM11 = 0, totalAPBM11 = 0, totalP10BM11 = 0;
                int count = 0;

                for (Map.Entry<Integer, String> qEntry : queries.entrySet()) {
                    int queryId = qEntry.getKey();
                    String queryText = qEntry.getValue();
                    Map<Integer, Integer> relevance;
                    try {
                        relevance = FileReader.bacaRelevance(queryId);
                    } catch (FileNotFoundException e) {
                        continue;
                    }

                    Set<Integer> relevantSet = getRelevantSet(relevance);

                    List<Map.Entry<Integer, Double>> hasilBIM = BIMModel.hitungBIM(queryText, invertedIndex, fileIndex, relevantSet);
                    List<Map.Entry<Integer, Double>> hasilTP = TwoPoissonModel.hitungTwoPoisson(queryText, invertedIndex, fileIndex, relevantSet);
                    List<Map.Entry<Integer, Double>> hasilBM25 = BM25Model.hitungBM25(queryText, invertedIndex, fileIndex, relevantSet);
                    List<Map.Entry<Integer, Double>> hasilBM11 = BM11Model.hitungBM11(queryText, invertedIndex, fileIndex, relevantSet);

                    if (!hasilBIM.isEmpty() && !hasilTP.isEmpty() && !hasilBM25.isEmpty() && !hasilBM11.isEmpty()) {
                        totalPrecBIM += hitungPrecision(relevance, hasilBIM);
                        totalRecallBIM += hitungRecall(relevance, hasilBIM);
                        totalAPBIM += hitung11PointAP(relevance, hasilBIM);
                        totalP10BIM += hitungPrecisionAtK(relevance, hasilBIM, kPrecision);

                        totalPrecTP += hitungPrecision(relevance, hasilTP);
                        totalRecallTP += hitungRecall(relevance, hasilTP);
                        totalAPTP += hitung11PointAP(relevance, hasilTP);
                        totalP10TP += hitungPrecisionAtK(relevance, hasilTP, kPrecision);

                        totalPrecBM25 += hitungPrecision(relevance, hasilBM25);
                        totalRecallBM25 += hitungRecall(relevance, hasilBM25);
                        totalAPBM25 += hitung11PointAP(relevance, hasilBM25);
                        totalP10BM25 += hitungPrecisionAtK(relevance, hasilBM25, kPrecision);

                        totalPrecBM11 += hitungPrecision(relevance, hasilBM11);
                        totalRecallBM11 += hitungRecall(relevance, hasilBM11);
                        totalAPBM11 += hitung11PointAP(relevance, hasilBM11);
                        totalP10BM11 += hitungPrecisionAtK(relevance, hasilBM11, kPrecision);

                        count++;
                    }
                }

                // Header tabel
                System.out.printf("%-12s | %-13s | %-12s | %-12s | %-8s%n",
                        "Model", "Avg Precision", "Avg Recall", "Avg 11-Pt AP", "P@" + kPrecision);
                System.out.println("-------------|---------------|--------------|--------------|---------");

                if (count > 0) {
                    System.out.printf("%-12s | %-13.4f | %-12.4f | %-12.4f | %-8.4f%n",
                            "BIM", totalPrecBIM/count, totalRecallBIM/count, totalAPBIM/count, totalP10BIM/count);
                    System.out.printf("%-12s | %-13.4f | %-12.4f | %-12.4f | %-8.4f%n",
                            "Two-Poisson", totalPrecTP/count, totalRecallTP/count, totalAPTP/count, totalP10TP/count);
                    System.out.printf("%-12s | %-13.4f | %-12.4f | %-12.4f | %-8.4f%n",
                            "BM25", totalPrecBM25/count, totalRecallBM25/count, totalAPBM25/count, totalP10BM25/count);
                    System.out.printf("%-12s | %-13.4f | %-12.4f | %-12.4f | %-8.4f%n",
                            "BM11", totalPrecBM11/count, totalRecallBM11/count, totalAPBM11/count, totalP10BM11/count);
                }
                System.out.println("\nJumlah query yang dievaluasi: " + count);
                System.out.println("Parameter: kPrecision = " + kPrecision);
                continue;
            }

            String query;
            Map<Integer, Integer> queryRelevance = null;
            Set<Integer> relevantSet = new HashSet<>();
            if (pilihan.equals("1")) {
                System.out.print("Pilih (1 - 226) untuk query evaluasi: ");
                int queryId = Integer.parseInt(sc.nextLine().trim());
                if (queryId > 226 || queryId < 1) {
                    System.out.println("Masukkan query yg valid!");
                    continue;
                }
                query = queries.get(queryId);
                queryRelevance = FileReader.bacaRelevance(queryId);
                relevantSet = getRelevantSet(queryRelevance);
                System.out.println("Query yang dipilih: " + query);
            } else if (pilihan.equals("2")) {
                System.out.print("Masukkan query: ");
                query = sc.nextLine();
                if (query.trim().isEmpty()) {
                    System.out.println("Query tidak boleh kosong.");
                    continue;
                }
                System.out.println("Catatan: Query manual menggunakan empty relevant set (tanpa training).");
            } else {
                System.out.println("Opsi tidak valid.");
                continue;
            }

            System.out.println("\n==============================");
            System.out.println("HASIL RANKING BIM Model");
            System.out.println("==============================");
            // === BIM Ranking ===
            List<Map.Entry<Integer, Double>> hasilBIM = BIMModel.hitungBIM(query, invertedIndex, fileIndex, relevantSet);

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
                    System.out.printf("Precision@%d: %f\n", kPrecision, hitungPrecisionAtK(queryRelevance, hasilBIM, kPrecision));
                    System.out.printf("11-Point AP: %f\n", hitung11PointAP(queryRelevance, hasilBIM));
                }
            }

            System.out.println("\n==============================");
            System.out.println("HASIL RANKING Two Poisson Model");
            System.out.println("==============================");

            List<Map.Entry<Integer, Double>> hasilTwoPoisson = TwoPoissonModel.hitungTwoPoisson(query, invertedIndex, fileIndex, relevantSet);

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
                    System.out.printf("Precision@%d: %f\n", kPrecision, hitungPrecisionAtK(queryRelevance, hasilTwoPoisson, kPrecision));
                    System.out.printf("11-Point AP: %f\n", hitung11PointAP(queryRelevance, hasilTwoPoisson));
                }
            }

            System.out.println("\n==============================");
            System.out.println("HASIL RANKING BM25");
            System.out.println("==============================");

            List<Map.Entry<Integer, Double>> hasilBM25 = BM25Model.hitungBM25(query, invertedIndex, fileIndex, relevantSet);

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
                    System.out.printf("Precision@%d: %f\n", kPrecision, hitungPrecisionAtK(queryRelevance, hasilBM25, kPrecision));
                    System.out.printf("11-Point AP: %f\n", hitung11PointAP(queryRelevance, hasilBM25));
                }
            }

            System.out.println("\n==============================");
            System.out.println("HASIL RANKING BM11");
            System.out.println("==============================");

            List<Map.Entry<Integer, Double>> hasilBM11 = BM11Model.hitungBM11(query, invertedIndex, fileIndex, relevantSet);

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
                    System.out.printf("Precision@%d: %f\n", kPrecision, hitungPrecisionAtK(queryRelevance, hasilBM11, kPrecision));
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

    /**
     * Mengkonversi Map relevance judgments menjadi Set docId yang relevan.
     * Dokumen dianggap relevan jika relevance score > 0.
     */
    private static Set<Integer> getRelevantSet(Map<Integer, Integer> relevance) {
        Set<Integer> relevantSet = new HashSet<>();
        for (Map.Entry<Integer, Integer> entry : relevance.entrySet()) {
            if (entry.getValue() > 0) {
                relevantSet.add(entry.getKey());
            }
        }
        return relevantSet;
    }
}