package Source_Code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BIMModel {

    public static List<Map.Entry<Integer, Double>> hitungBIM(String query,
            HashMap<String, LinkedList<Posting>> invertedIndex,
            HashMap<Integer, String> fileIndex,
            Set<Integer> relevantSet) {
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

        // Hitung skor BIM menggunakan relevant set (ground truth atau pseudo-relevant)
        HashMap<Integer, Double> docScores = hitungRSV(validTerms, invertedIndex, N, relevantSet);

        return Search.urutkanDokumen(docScores);
    }

    /**
     * Hitung skor BIM Skenario 1 untuk pseudo-relevance set.
     * Weight = log10(N / df)
     */
    public static HashMap<Integer, Double> hitungSkorTanpaRelevansi(String query,
            HashMap<String, LinkedList<Posting>> invertedIndex, int N) {

        List<String> queryTerms = TextPreprocessor.getQueryClean(query);
        HashMap<Integer, Double> docScores = new HashMap<>();

        // Filter hanya term yang ada di index
        List<String> validTerms = new ArrayList<>();
        for (String term : queryTerms) {
            if (invertedIndex.containsKey(term)) {
                validTerms.add(term);
            }
        }

        for (String term : validTerms) {
            int df = invertedIndex.get(term).size();
            if (df == 0) continue;
            double weight = Math.log10((double) N / df);

            for (Posting posting : invertedIndex.get(term)) {
                int docId = posting.getDocId();
                docScores.put(docId, docScores.getOrDefault(docId, 0.0) + weight);
            }
        }

        return docScores;
    }

    /**
     * Ambil top-K dokumen dari sorted list sebagai pseudo-relevant set.
     */
    public static Set<Integer> getTopKDocs(List<Map.Entry<Integer, Double>> sortedDocs, int k) {
        Set<Integer> topK = new HashSet<>();
        for (int i = 0; i < Math.min(k, sortedDocs.size()); i++) {
            topK.add(sortedDocs.get(i).getKey());
        }
        return topK;
    }

    /**
     * Hitung RSV (Retrieval Status Value) menggunakan pt dan ut dari ground truth.
     */
    public static HashMap<Integer, Double> hitungRSV(List<String> validTerms,
            HashMap<String, LinkedList<Posting>> invertedIndex, int N,
            Set<Integer> relevantSet) {

        HashMap<Integer, Double> docScores = new HashMap<>();
        int R = relevantSet.size();

        // Jika tidak ada dokumen relevan, return empty
        if (R == 0) return docScores;

        for (String term : validTerms) {
            int df = invertedIndex.get(term).size();
            int rt = countDocsInSet(term, invertedIndex, relevantSet);

            double pt = (rt + 0.5) / (R + 1.0);
            double ut = (df - rt + 0.5) / (N - R + 1.0);

            // Hindari log(0) atau pembagian nol
            if (pt <= 0 || pt >= 1 || ut <= 0 || ut >= 1) continue;

            double weight = Math.log10(pt / ut);

            for (Posting posting : invertedIndex.get(term)) {
                int docId = posting.getDocId();
                docScores.put(docId, docScores.getOrDefault(docId, 0.0) + weight);
            }
        }

        return docScores;
    }

    /** Hitung jumlah dokumen yang memuat term dan juga dalam relevant set */
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
