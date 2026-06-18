package Source_Code;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Kelas untuk melakukan text preprocessing pada dokumen dan query.
 * Bertanggung jawab atas tokenization, stopword removal, dan pemanggilan stemming.
 */
public class TextPreprocessor {

    /**
     * Kumpulan kata-kata stopword yang akan dihapus selama preprocessing.
     */
    public static Set<String> stopwords = new HashSet<>(Arrays.asList(
            "a", "an", "as", "at", "be", "by", "for", "from",
            "has", "he", "in", "is", "it", "its", "of", "on",
            "that", "the", "to", "was", "were", "will", "with",
            "this", "which", "who", "whom", "there", "their"));

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
}
