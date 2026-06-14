package Source_Code;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Membaca file query evaluasi dan relevance judgments dari dataset Cranfield.
 *
 * Format Dokumen\TEST\query.txt:
 *   queryId \t queryText
 *   1        what similarity laws...
 *
 * Format Dokumen\TEST\RES\{queryId}.txt:
 *   queryId docId relevance
 *   1 184 2
 *   1 29  2
 */
public class FileReader {

    private static final String QUERY_PATH = "./Dokumen/TEST/query.txt";
    private static final String RES_PATH = "./Dokumen/TEST/RES";

    /**
     * Membaca semua query dari query.txt.
     *
     * @return Map dengan key = queryId, value = queryText (urutan sesuai file)
     * @throws FileNotFoundException jika file query tidak ditemukan
     */
    public static Map<Integer, String> bacaSemuaQuery() throws FileNotFoundException {
        return bacaSemuaQuery(QUERY_PATH);
    }

    private static Map<Integer, String> bacaSemuaQuery(String path) throws FileNotFoundException {
        Map<Integer, String> queries = new LinkedHashMap<>();
        Scanner sc = new Scanner(new File(path));
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            // Format: queryId (tab) queryText
            String[] parts = line.split("\t", 2);
            if (parts.length == 2) {
                int queryId = Integer.parseInt(parts[0].trim());
                String queryText = parts[1].trim();
                queries.put(queryId, queryText);
            }
        }
        sc.close();
        return queries;
    }

    /**
     * Membaca relevance judgments untuk satu query dari file RES/{queryId}.txt.
     *
     * @param queryId ID query
     * @return Map dengan key = docId, value = relevance score
     * @throws FileNotFoundException jika file relevance tidak ditemukan
     */
    public static Map<Integer, Integer> bacaRelevance(int queryId) throws FileNotFoundException {
        return bacaRelevance(queryId, RES_PATH);
    }

    private static Map<Integer, Integer> bacaRelevance(int queryId, String resPath) throws FileNotFoundException {
        Map<Integer, Integer> relevance = new HashMap<>();
        File file = new File(resPath + File.separator + queryId + ".txt");

        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            // Format: queryId docId relevance
            String[] parts = line.split("\\s+");
            if (parts.length >= 3) {
                int docId = Integer.parseInt(parts[1]);
                int rel = Integer.parseInt(parts[2]);
                relevance.put(docId, rel);
            }
        }
        sc.close();
        return relevance;
    }

    /**
     * Mendapatkan daftar docId yang relevan (relevance > 0) untuk suatu query.
     *
     * @param queryId ID query
     * @return List docId yang relevan
     * @throws FileNotFoundException jika file relevance tidak ditemukan
     */
    public static List<Integer> getDokumenRelevan(int queryId) throws FileNotFoundException {
        Map<Integer, Integer> relevance = bacaRelevance(queryId);
        List<Integer> docIds = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : relevance.entrySet()) {
            if (entry.getValue() > 0) {
                docIds.add(entry.getKey());
            }
        }
        return docIds;
    }
}
