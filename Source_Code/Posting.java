package Source_Code;

public class Posting {
    /**
     * ID dokumen di mana term ditemukan.
     */
    private int docId;

    /**
     * Frekuensi kemunculan term (Term Frequency) dalam dokumen terkait.
     */
    private int termFrequency;

    /**
     * Membuat objek Posting baru dengan ID dokumen dan frekuensi term tertentu.
     *
     * @param docId         ID dokumen
     * @param termFrequency frekuensi kemunculan term pada dokumen tersebut
     */
    public Posting(int docId, int termFrequency) {
        this.docId = docId;
        this.termFrequency = termFrequency;
    }

    /**
     * Mendapatkan ID dokumen.
     * 
     * @return nilai ID dokumen
     */
    public int getDocId() {
        return docId;
    }

    /**
     * Mendapatkan frekuensi kemunculan term (TF).
     *
     * @return nilai frekuensi term
     */
    public int getTermFrequency() {
        return termFrequency;
    }

    /**
     * Menaikkan nilai frekuensi term sebesar satu.
     */
    public void incrementTermFrequency() {
        this.termFrequency++;
    }

    /**
     * Mengembalikan string dari objek Posting.
     * Format: (docId, tf:termFrequency)
     *
     * @return representasi string dari Posting
     */
    @Override
    public String toString() {
        return "(" + docId + ", tf:" + termFrequency + ")";
    }
}
