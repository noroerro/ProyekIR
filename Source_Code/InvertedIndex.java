package Source_Code;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Kelas untuk membangun struktur data Inverted Index dari koleksi dokumen.
 * Menyimpan term frequency, document frequency, dan panjang dokumen.
 */
public class InvertedIndex {

    /** Path folder dokumen Cranfield */
    public static final String DOC_PATH = "./Dokumen/Cranfield";

    /**
     * Membuat Inverted Index beserta perhitungan Term Frequency (TF) dan Document
     * Length.
     *
     * @param path      path direktori dokumen
     * @param fileIndex peta (map) untuk menyimpan pemetaan ID dokumen ke nama file
     * @param docLength peta (map) untuk menyimpan panjang setiap dokumen
     * @return inverted index yang memetakan setiap term ke daftar posting-nya
     * @throws FileNotFoundException jika file dokumen tidak ditemukan
     */
    public static HashMap<String, LinkedList<Posting>> createInvertedIndex(HashMap<Integer, String> fileIndex, HashMap<Integer, Integer> docLength) throws FileNotFoundException {
        File folder = new File(DOC_PATH);
        HashMap<String, LinkedList<Posting>> invertedIndex = new HashMap<>();
        Scanner sc;

        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
                sc = new Scanner(file);
                int docId = Integer.parseInt(file.getName().replace(".txt", ""));
                fileIndex.put(docId, file.getName());
                docLength.put(docId, 0);

                while (sc.hasNext()) {
                    String kata = TextPreprocessor.preProcessing(sc.next());

                    if (TextPreprocessor.stopwords.contains(kata)) {
                        continue;
                    }

                    kata = Stemmer.doPorterStemmer(kata);
                    if (kata.isEmpty()) {
                        continue;
                    }

                    // Increment panjang dokumen (hanya kata valid setelah stopword & stemming)
                    docLength.put(docId, docLength.get(docId) + 1);

                    if (!invertedIndex.containsKey(kata)) {
                        LinkedList<Posting> posting = new LinkedList<>();
                        posting.add(new Posting(docId, 1));
                        invertedIndex.put(kata, posting);
                    } else {
                        LinkedList<Posting> posting = invertedIndex.get(kata);
                        Posting lastPo = posting.getLast();

                        if (lastPo.getDocId() == docId) {
                            lastPo.incrementTermFrequency(); // Increment TF jika kata muncul lagi di dokumen yang sama
                        } else {
                            posting.add(new Posting(docId, 1)); // Buat posting baru jika di dokumen baru
                        }
                    }
                }
                sc.close();
            }
        }

        return invertedIndex;
    }
}
