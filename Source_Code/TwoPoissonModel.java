package Source_Code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TwoPoissonModel {

    public static List<Map.Entry<Integer, Double>> hitungTwoPoisson(String query,
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

        double k = 1.5; // ini parameter k nya, bisa di tuning dengan rentang 1 <= k <= 2

        // Hitung skor menggunakan two poisson model
        HashMap<Integer, Double> docScores = hitungSkor(validTerms, invertedIndex, N, k, relevantSet);

        // Urutkan dan return
        return Search.urutkanDokumen(docScores);
    }

    public static HashMap<Integer, Double> hitungSkor(List<String> validTerms,
            HashMap<String, LinkedList<Posting>> invertedIndex, int N, double k,
            Set<Integer> relevantSet) {

        int R = relevantSet.size();

        HashMap<Integer, Double> docScores = new HashMap<>();

        for (String term : validTerms) {
            int Nt = invertedIndex.get(term).size(); //banyak dokumen yg mengandung term
            int rt = countDocsInSet(term, invertedIndex, relevantSet); //banyak dokumen relevan yang mengandung term

            double pt = (rt + 0.5) / (R + 1.0);
            double ut = (Nt - rt + 0.5) / (N - R + 1.0);

            // Hindari log(0) atau pembagian nol
            if (pt <= 0 || pt >= 1 || ut <= 0 || ut >= 1) continue;

            double wt = Math.log10((pt / ut));

            for (Posting posting : invertedIndex.get(term)) {
                int docId = posting.getDocId();
                int tf = posting.getTermFrequency();

                double weightTwoPoisson = (tf * (k + 1) * wt) / (tf + k);
                docScores.put(docId, docScores.getOrDefault(docId, 0.0) + weightTwoPoisson);
            }
        }
        return docScores;
    }

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
