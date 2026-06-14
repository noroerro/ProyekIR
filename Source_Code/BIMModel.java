package Source_Code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BIMModel {

    /** Top-10 dokumen pseudo-relevant, bisa diakses model lain setelah hitungBIM() */
    public static Set<Integer> relevantSet = new HashSet<>();

    public static List<Map.Entry<Integer, Double>> hitungBIM(String query, HashMap<String, LinkedList<Posting>> invertedIndex, HashMap<Integer, String> fileIndex) {
        List<String> queryTerms = TextPreprocessor.getQueryClean(query);
        int N = fileIndex.size();

        // Filter hanya term yang ada di index
        List<String> validTerms = new ArrayList<>();
        for (String term : queryTerms) {
            if (invertedIndex.containsKey(term)) {
                validTerms.add(term);
            }
        }

        if (validTerms.isEmpty()) {
            return new ArrayList<>();
        }

        // Hitung skor BIM TANPA relevansi (initial ranking)
        HashMap<Integer, Double> docScores = hitungTanpaRelevansi(validTerms, invertedIndex, N);

        // Hitung skor BIM DENGAN relevansi (ambil top-10 sebagai pseudo-relevant)
        docScores = hitungDenganRelevansi(docScores, validTerms, invertedIndex, N, 10);

        // Urutkan dan return
        return Search.urutkanDokumen(docScores);
    }

    public static HashMap<Integer, Double> hitungTanpaRelevansi(List<String> queryTerms,
            HashMap<String, LinkedList<Posting>> invertedIndex, int N) {

        HashMap<Integer, Double> docScores = new HashMap<>();

        for (String term : queryTerms) {
            int nt = invertedIndex.get(term).size();
            double weight = skorTanpaRelevansi(N, nt);

            for (Posting posting : invertedIndex.get(term)) {
                int docId = posting.getDocId();
                docScores.put(docId, docScores.getOrDefault(docId, 0.0) + weight);
            }
        }

        return docScores;
    }

    public static HashMap<Integer, Double> hitungDenganRelevansi(HashMap<Integer, Double> initialScores,
            List<String> validTerms,
            HashMap<String, LinkedList<Posting>> invertedIndex, int N,
            int topK) {

        // Urutkan skor awal dan ambil top-K sebagai pseudo-relevant
        List<Map.Entry<Integer, Double>> sorted = Search.urutkanDokumen(initialScores);
        List<Integer> topKDocs = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, sorted.size()); i++) {
            topKDocs.add(sorted.get(i).getKey());
        }
        int R = topKDocs.size();
        if (R == 0) return initialScores;

        BIMModel.relevantSet = new HashSet<>(topKDocs);
        HashMap<Integer, Double> docScores = new HashMap<>();

        for (String term : validTerms) {
            int df = invertedIndex.get(term).size();
            int rt = countDocsInSet(term, invertedIndex, relevantSet);

            double pt = (rt + 0.5) / (R + 1.0);
            double ut = (df - rt + 0.5) / (N - R + 1.0);

            // Hindari log(0) atau pembagian nol
            if (pt <= 0 || pt >= 1 || ut <= 0 || ut >= 1) continue;

            double weight = Math.log10((pt / ut));

            for (Posting posting : invertedIndex.get(term)) {
                int docId = posting.getDocId();
                docScores.put(docId, docScores.getOrDefault(docId, 0.0) + weight);
            }
        }

        return docScores;
    }


    private static double skorTanpaRelevansi(int N, int nt) {
        if (nt == 0) return 0;
        return Math.log10((double) N / nt);
    }
    // Hitung jumlah dokumen yang memuat term dan juga dalam relevant set
    private static int countDocsInSet(String term,
            HashMap<String, LinkedList<Posting>> invertedIndex,
            Set<Integer> relevantSet) {
        int count = 0;
        for (Posting posting : invertedIndex.get(term)) {
            if (relevantSet.contains(posting.getDocId())) {
                count++;
            }
        }
        return count;
    }

}
