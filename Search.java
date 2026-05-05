package ProyekIR;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class Search {
    // Stop Word
    public static Set<String> stopwords = new HashSet<>(Arrays.asList(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
            "has", "he", "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "were", "will", "with"));

    public static Set<Character> vowels = new HashSet<>(Arrays.asList('a', 'e', 'i', 'o', 'u'));

    public static void main(String[] args) {
        String path = "./Dokumen"; // Ganti dengan path folder dokumen yang sesuai
        // I : Data Indexing
        // 1. Mendapatkan semua file yang ada di folder dokumen
        File[] files = getAllFiles(path);
        // 2. Membuat inverted index dari semua file yang ada di folder dokumen
        HashMap<Integer, String> fileIndex = null;
        HashMap<String, LinkedList<Integer>> invertedIndex = null;
        try {
            // fileIndex u/ menyimpan nama file sebagai nomor
            fileIndex = new HashMap<>();
            // Inverted Index
            invertedIndex = createInvertedIndex(files, fileIndex);
            // file index tidak di return karena mengirim alamat file ke fungsi
            // createInvertedIndex, sehingga tidak perlu dikembalikan lagi

        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
        }
        System.out.println("File Index : " + fileIndex);
        System.out.println("Inverted Index : " + invertedIndex);

        // Scanner untuk query yang ingin dicari 
        Scanner sc = new Scanner(System.in);
        System.out.print("\nMasukkan kata yang ingin dicari: ");
        String query = sc.nextLine();
        sc.close();

        // Query dimasukkan ke dalam array (setiap kata yang dipisah oleh spasi akan dimasukkan ke array)
        String[] daftarKata = query.split("\\s+");
        String hasilPreProcessing = "";

        // Looping untuk setiap kata yang ada di query (di array daftarKata)
        for(String kata: daftarKata){
            // Setiap kata yang ada di query akan dilakukan preProcessing dan porterStemmer dahulu
            kata = preProcessing(kata);
            kata = porterStemmer(kata);

            // Jika di inverted index terdapat kata pada query, maka hasil pre processing adalah kata tersebut
            if(invertedIndex.containsKey(kata)){
                hasilPreProcessing = kata;
            } else{ // Jika di inverted index tidak ada kata pada query, maka akan dihitung edit distance antara kata pada query dengan 
                    // setiap kata di inverted index, lalu hasil pre processing adalah kata yang memiliki edit distance paling kecil dengan kata pada query
                int minDistance = Integer.MAX_VALUE;

                // Looping ke semua kata (keys) di inverted index
                for (String kataDiIndex : invertedIndex.keySet()) {
                    
                    // Panggil fungsi edit distance untuk menghitung jarak edit distance antara kata pada query dengan kata di index
                    int jarak = hitungEditDistance(kata, kataDiIndex);
                    
                    // Update jika nemu jarak yang lebih kecil
                    if (jarak < minDistance) {
                        minDistance = jarak;
                        hasilPreProcessing = kataDiIndex;
                    }
                }
            }
            
            // Cek jika hasil preProcessing tidak kosong
            if (!hasilPreProcessing.equals("")) {
                // Jika hasil preProcessing sama dengan kata pada query, maka kata tersebut ditemukan pada indeks
                if(hasilPreProcessing.equals(kata)){
                    // Ini ntar bisa dihapus aja, ini cuma buat ngecek hasil preProcessing nya aja 
                    System.out.println("Query: '" + kata + "' ditemukan.");
                    System.out.println("Dokumen: " + invertedIndex.get(hasilPreProcessing));
                } else { //jika hasil preProcessing tidak sama dengan kata pada query, maka kata tersebut tidak ada pada indeks
                         // dan hasil preProcessing adalah perhitungan dan kata rekomendasi dari edit distance

                    // Ini ntar bisa dihapus aja, ini cuma buat ngecek hasil + biar gampang nanti cek boolean model nya
                    System.out.println("Query: '" + kata + "' tidak ditemukan.");
                    System.out.println("Did you mean '" + hasilPreProcessing + "'?");
                    System.out.println("Dokumen: " + invertedIndex.get(hasilPreProcessing));
                }
            } else { // Jika hasil preProcessing kosong, maka kata pada query tidak ditemukan dan tidak ada rekomendasi dari edit distance
                System.out.printf("Kata '%s' tidak ditemukan di indeks.\n", kata);
            }
        }
    }

    public static File[] getAllFiles(String path) {
        // Mendapatkan semua file yang ada di folder dokumen
        File folder = new File(path);
        // Mendapatkan semua file yang ada di folder dokumen
        File[] listFiles = folder.listFiles();
        return listFiles;
    }

    public static int hitungM(String word) {
        if (word == null || word.length() < 2) {
            return 0;
        }

        int count = 0;

        for (int i = 0; i < word.length() - 1; i++) {
            char current = word.charAt(i);
            char next = word.charAt(i + 1);

            // Cek apakah karakter saat ini vokal
            boolean isVowel = vowels.contains(current);
            // Cek apakah karakter berikutnya adalah huruf dan merupakan konsonan
            boolean isNextConsonant = Character.isLetter(next) && !vowels.contains(next);

            if (isVowel && isNextConsonant) {
                count++;
                // i++;
                // (misal: "aba" = 1 VC)
            }
        }
        return count;
    }

    public static boolean isCVC(String word) {
        if (word == null || word.length() < 3) {
            return false;
        }

        char last = word.charAt(word.length() - 1);
        char secondLast = word.charAt(word.length() - 2);
        char thirdLast = word.charAt(word.length() - 3);

        // Cek apakah pola CVC terpenuhi
        return !vowels.contains(thirdLast) && vowels.contains(secondLast) && !vowels.contains(last)
                && last != 'w' && last != 'x' && last != 'y';
    }

    public static boolean conditionV(String word) {
        // Cek apakah stem memiliki setidaknya satu vokal
        if (word == null || word.isEmpty())
            return false;

        for (int i = 0; i < word.length(); i++) {
            if (vowels.contains(word.charAt(i))) {
                return true; // Menemukan setidaknya satu vokal
            }
        }
        return false;
    }

    public static boolean conditionDoubleKonsonan(String word) {
        // Cek apakah stem berakhir dengan huruf konsonan ganda
        if (word == null || word.length() < 2)
            return false;

        char last = word.charAt(word.length() - 1);
        char secondLast = word.charAt(word.length() - 2);

        return last == secondLast && !vowels.contains(last);
    }

    public static String preProcessing(String word) {
        // Pre processing sederhana
        // Mengubah semua menjadi lowercase
        word = word.toLowerCase();
        // Menghapus spasi tambahan
        word = word.trim();
        // Menghapus tanda baca
        word = word.replaceAll("[^a-zA-Z]", "");
        return word;
    }

    public static String porterStemmer(String word) {
        // Step 1a
        if (word.endsWith("sses")) {
            // sses -> ss
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("ies")) {
            // ies -> i
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("ss")) {
            // ss -> ss
            word = word.substring(0, word.length() - 0);
        } else if (word.endsWith("s")) {
            // s -> (hapus s)
            word = word.substring(0, word.length() - 1);
        }
        // Step 1b
        // Part 1
        if (word.endsWith("eed") && hitungM(word.substring(0, word.length() - 3)) > 0) {
            // eed -> ee
            word = word.substring(0, word.length() - 1);
        } else if (word.endsWith("ed") && conditionV(word.substring(0, word.length() - 2))
                || word.endsWith("ing") && conditionV(word.substring(0, word.length() - 3))) {
            // (*v*) ed -> (hapus ed) atau (*v*) ing -> (hapus ing)
            if (word.endsWith("ed")) {
                word = word.substring(0, word.length() - 2);
            } else {
                word = word.substring(0, word.length() - 3);
            }

        }
        // Part 2
        if (word.endsWith("at") || word.endsWith("bl") || word.endsWith("iz")) {
            word = word + "e";
        } else if (conditionDoubleKonsonan(word)
                && (word.endsWith("l") || word.endsWith("s") || word.endsWith("z"))) {
            // Jika kata adalah berakhir ganda kecuali l s z -> (hapus huruf terakhir)
            word = word.substring(0, word.length() - 1);
        } else if (hitungM(word) == 1 && isCVC(word)) {
            // Jika kata memiliki satu VC dan berakhir dengan pola CVC -> tambahkan "e"
            word = word + "e";
        }
        // Step 1c
        if (conditionV(word) && word.endsWith("y")) {
            // (*v*) y -> (hapus y) + i
            word = word.substring(0, word.length() - 1) + "i";
        }

        // Step 2
        if (word.endsWith("ational") && hitungM(word.substring(0, word.length() - 7)) > 0) {
            // ational -> ate
            word = word.substring(0, word.length() - 5) + "e";
        } else if (word.endsWith("tional") && hitungM(word.substring(0, word.length() - 6)) > 0) {
            // tional -> tion
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("enci") && hitungM(word.substring(0, word.length() - 4)) > 0) {
            // enci -> ence
            word = word.substring(0, word.length() - 1) + "e";
        } else if (word.endsWith("anci") && hitungM(word.substring(0, word.length() - 4)) > 0) {
            // anci -> ance
            word = word.substring(0, word.length() - 1) + "e";
        } else if (word.endsWith("izer") && hitungM(word.substring(0, word.length() - 4)) > 0) {
            // izer -> ize
            word = word.substring(0, word.length() - 1);
        } else if (word.endsWith("abli") && hitungM(word.substring(0, word.length() - 4)) > 0) {
            // abli -> able
            word = word.substring(0, word.length() - 1) + "e";
        } else if (word.endsWith("alli") && hitungM(word.substring(0, word.length() - 4)) > 0) {
            // alli -> al
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("entli") && hitungM(word.substring(0, word.length() - 5)) > 0) {
            // entli -> ent
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("eli") && hitungM(word.substring(0, word.length() - 3)) > 0) {
            // eli -> e
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("ousli") && hitungM(word.substring(0, word.length() - 5)) > 0) {
            // ousli -> ous
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("ization") && hitungM(word.substring(0, word.length() - 7)) > 0) {
            // ization -> ize
            word = word.substring(0, word.length() - 5) + "e";
        } else if (word.endsWith("ation") && hitungM(word.substring(0, word.length() - 5)) > 0) {
            // ation -> ate
            word = word.substring(0, word.length() - 3) + "e";
        } else if (word.endsWith("ator") && hitungM(word.substring(0, word.length() - 4)) > 0) {
            // ator -> ate
            word = word.substring(0, word.length() - 2) + "e";
        } else if (word.endsWith("alism") && hitungM(word.substring(0, word.length() - 5)) > 0) {
            // alism -> al
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("iveness") && hitungM(word.substring(0, word.length() - 7)) > 0) {
            // iveness -> ive
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("fulness") && hitungM(word.substring(0, word.length() - 7)) > 0) {
            // fulness -> ful
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("ousness") && hitungM(word.substring(0, word.length() - 7)) > 0) {
            // ousness -> ous
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("aliti") && hitungM(word.substring(0, word.length() - 5)) > 0) {
            // aliti -> al
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("iviti") && hitungM(word.substring(0, word.length() - 5)) > 0) {
            // iviti -> ive
            word = word.substring(0, word.length() - 3) + "e";
        } else if (word.endsWith("biliti") && hitungM(word.substring(0, word.length() - 6)) > 0) {
            // biliti -> ble
            word = word.substring(0, word.length() - 5) + "le";
        }
        // Step 3
        if (word.endsWith("icate") && hitungM(word.substring(0, word.length() - 5)) > 0) {
            // icate -> ic
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ative") && hitungM(word.substring(0, word.length() - 5)) > 0) {
            // ative -> (hapus total)
            word = word.substring(0, word.length() - 5);
        } else if (word.endsWith("alize") && hitungM(word.substring(0, word.length() - 5)) > 0) {
            // alize -> al
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("iciti") && hitungM(word.substring(0, word.length() - 5)) > 0) {
            // iciti -> ic
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ical") && hitungM(word.substring(0, word.length() - 4)) > 0) {
            // ical -> ic
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("ousness") && hitungM(word.substring(0, word.length() - 7)) > 0) {
            // ousness -> ous
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("ful") && hitungM(word.substring(0, word.length() - 3)) > 0) {
            // ful -> (hapus total)
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ness") && hitungM(word.substring(0, word.length() - 4)) > 0) {
            // ness -> (hapus total)
            word = word.substring(0, word.length() - 4);
        }

        // Step 4 (m > 1)
        if (word.endsWith("al") && hitungM(word.substring(0, word.length() - 2)) > 1) {
            // al -> (hapus total)
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("ance") && hitungM(word.substring(0, word.length() - 4)) > 1) {
            // ance -> (hapus total)
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("ence") && hitungM(word.substring(0, word.length() - 4)) > 1) {
            // ence -> (hapus total)
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("er") && hitungM(word.substring(0, word.length() - 2)) > 1) {
            // er -> (hapus total)
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("ic") && hitungM(word.substring(0, word.length() - 2)) > 1) {
            // ic -> (hapus total)
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("able") && hitungM(word.substring(0, word.length() - 4)) > 1) {
            // able -> (hapus total)
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("ible") && hitungM(word.substring(0, word.length() - 4)) > 1) {
            // ible -> (hapus total)
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("ant") && hitungM(word.substring(0, word.length() - 3)) > 1) {
            // ant -> (hapus total)
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ement") && hitungM(word.substring(0, word.length() - 5)) > 1) {
            // ement -> (hapus total)
            word = word.substring(0, word.length() - 5);
        } else if (word.endsWith("ment") && hitungM(word.substring(0, word.length() - 4)) > 1) {
            // ment -> (hapus total)
            word = word.substring(0, word.length() - 4);
        } else if (word.endsWith("ent") && hitungM(word.substring(0, word.length() - 3)) > 1) {
            // ent -> (hapus total)
            word = word.substring(0, word.length() - 3);
        } else if ((word.endsWith("s") || word.endsWith("t")) && word.endsWith("ion")
                && hitungM(word.substring(0, word.length() - 3)) > 1) {
            // ion && (*S || * T) -> (hapus total)
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ou") && hitungM(word.substring(0, word.length() - 2)) > 1) {
            // ou -> (hapus total)
            word = word.substring(0, word.length() - 2);
        } else if (word.endsWith("ism") && hitungM(word.substring(0, word.length() - 3)) > 1) {
            // ism -> (hapus total)
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ate") && hitungM(word.substring(0, word.length() - 3)) > 1) {
            // ate -> (hapus total)
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("iti") && hitungM(word.substring(0, word.length() - 3)) > 1) {
            // iti -> (hapus total)
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ous") && hitungM(word.substring(0, word.length() - 3)) > 1) {
            // ous -> (hapus total)
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ive") && hitungM(word.substring(0, word.length() - 3)) > 1) {
            // ive -> (hapus total)
            word = word.substring(0, word.length() - 3);
        } else if (word.endsWith("ize") && hitungM(word.substring(0, word.length() - 3)) > 1) {
            // ize -> (hapus total)
            word = word.substring(0, word.length() - 3);
        }

        // Step 5a
        if (word.endsWith("e")) {
            int m = hitungM(word.substring(0, word.length() - 1));
            if (m > 1) {
                // e -> (hapus total)
                word = word.substring(0, word.length() - 1);
            } else if (m == 1 && !isCVC(word.substring(0, word.length() - 1))) {
                // e -> (hapus total) jika m=1 dan bukan CVC
                word = word.substring(0, word.length() - 1);
            }
        }

        // Step 5b
        if (hitungM(word) > 1 && conditionDoubleKonsonan(word) && word.endsWith("l")) {
            // (*d and *L) -> (hapus satu l)
            word = word.substring(0, word.length() - 1);
        }
        return word;
    }

    public static HashMap<String, LinkedList<Integer>> createInvertedIndex(File[] files,
            HashMap<Integer, String> fileIndex) throws FileNotFoundException {
        HashMap<String, LinkedList<Integer>> invertedIndex = new HashMap<>();
        Scanner sc;
        int counter = 0;
        // Looping semua file yang ada di folder dokumen
        for (File file : files) {
            // Bila merupakan sebuah file bertipe teks
            if (file.isFile() && file.getName().endsWith(".txt")) {
                // Buat scanner untuk membaca file
                sc = new Scanner(file);
                counter++; // Menambahkan nomor indeks untuk setiap file
                fileIndex.put(counter, file.getName()); // Menyimpan nama file dengan nomor indeks
                // Looping semua kata yang ada di file
                while (sc.hasNext()) {
                    String kata = sc.next(); // Memanggil fungsi preProcessing untuk memproses kata
                    if (stopwords.contains(kata)) { // Cek apakah kata termasuk stop word atau tidak
                        continue; // Jika termasuk stop word, lewati kata tersebut
                    }
                    // Pre processing
                    kata = preProcessing(kata);
                    // Masukkan kata ke porter stemmer untuk mendapatkan bentuk dasar kata
                    kata = porterStemmer(kata);

                    // Memakai LinkedList untuk menyimpan nama file yang mengandung kata tersebut
                    LinkedList<Integer> tempList;
                    // Bila belum terdapat di inverted index, maka perlu dimasukkan beserta nama
                    // filenya
                    if (!invertedIndex.containsKey(kata)) {
                        tempList = new LinkedList<>();
                        // Masukkan nama file ke dalam list
                        tempList.add(counter);
                        invertedIndex.put(kata, tempList);
                    } else {
                        if (!invertedIndex.get(kata).contains(counter)) {// Cek apakah nama file sudah ada di
                                                                         // list atau belum
                            // Masukkan nama file ke dalam list
                            invertedIndex.get(kata).add(counter);
                        }
                    }
                }
            }
        }

        return invertedIndex;
    }

    // Menghitung edit distance antara dua string (antara kata di query dan kata di index)
    public static int hitungEditDistance(String query, String cari){
        // Inisialisasi array berukuran 2, dimana array ke 0 menyimpan kata dari query dan array ke 1 menyimpan kata dari index
        String [] arr = new String[2];
        arr[0] = query;
        arr[1] = cari;

        // Memanggil fungsi potongAkhir untuk memotong huruf yang sama di akhir pada kedua string, 
        // lalu simpan hasilnya ke dalam variable hasilPotong_kata1 dan hasilPotong_kata2 agar mudah digunakan nantinya
        String[] hasilPotong = potongAkhir(arr);
        String hasilPotong_kata1 = hasilPotong[0];
        String hasilPotong_kata2 = hasilPotong[1];
        
        // Inisialisasi 2 variable string 
        String hasilAkhir_kata1, hasilAkhir_kata2;

        // Jika hasil potong kata ke 1 dan kata ke 2 lebih besar dari 0, maka panggil fungsi potongAwal untuk memotong huruf yang sama 
        // di awal pada kedua string (jika ada) dan disimpan pada variable yang sudah di inisialisasi sebelumnya
        if(hasilPotong_kata1.length() > 0 && hasilPotong_kata2.length() > 0){
            hasilPotong = potongAwal(hasilPotong);
            hasilAkhir_kata1 = hasilPotong[0];
            hasilAkhir_kata2 = hasilPotong[1];   
        } else{ //jika panjang hasil potong kata ke 1 atau kata ke 2 sama dengan 0, maka hasil akhir dari kedua kata tersebut adalah hasil potong sebelumnya
            hasilAkhir_kata1 = hasilPotong_kata1;
            hasilAkhir_kata2 = hasilPotong_kata2;
        }        
    
        int distance = 0;
        int [][] arrEditDistance; // Array untuk menyimpan serta menghitung edit distance dari setiap posisi huruf pada kedua string

        // Jika panjang hasil akhir dari kata ke 1 dan kata ke 2 lebih besar sama dengan 1, maka lakukan perhitungan edit distance menggunakan algoritma Levenshtein Distance
        if(hasilAkhir_kata1.length() >= 1 && hasilAkhir_kata2.length() >= 1){
            // Inisialisasi array edit distance dengan ukuran baris sebanyak panjang hasil akhir dari kata ke-1 + 1 dan 
            // ukuran kolom sebanyak panjang hasil akhir dari kata ke 2 + 1 (keduanya di +1 karena untuk menyimpan jarak dari posisi 0 
            // sampai posisi panjang kata)
            arrEditDistance  = new int [hasilAkhir_kata1.length() + 1][hasilAkhir_kata2.length() + 1];
            
            // Isi kolom pertama dari array dari 0 sampai panjang kata
            for(int i = 0; i <= hasilAkhir_kata1.length(); i++){
                arrEditDistance[i][0] = i;
            }
            // Isi baris pertama dari array dari 1 sampai panjang kata
            for(int j = 1; j <= hasilAkhir_kata2.length(); j++){
                arrEditDistance[0][j] = j;
            }
            // Hitung edit distance untuk setiap posisi huruf pada kedua string
            for(int i = 1; i <= hasilAkhir_kata1.length(); i++){
                for(int j = 1; j <= hasilAkhir_kata2.length(); j++){
                    // Jika kedua huruf sama, maka ambil nilai edit distance pada posisi diagonal kiri atas (do nothing)
                    if(hasilAkhir_kata1.charAt(i-1) == hasilAkhir_kata2.charAt(j-1)){
                        arrEditDistance[i][j] = arrEditDistance[i-1][j-1];
                    } else { // Jika kedua huruf berbeda, maka ambil nilai edit distance pada posisi diagonal kiri atas, posisi kiri, dan posisi atas,
                            //  kemudian ambil nilai edit distance paling minimum dari ketiga posisi tersebut, lalu tambahkan 1 
                        arrEditDistance[i][j] = 1 + Math.min(arrEditDistance[i-1][j-1], Math.min(arrEditDistance[i-1][j], arrEditDistance[i][j-1]));
                    }
                }
            }
            // Nilai edit distance dari kedua string adalah nilai yang berada pada posisi paling kanan bawah dari array edit distance
            distance = arrEditDistance[hasilAkhir_kata1.length()][hasilAkhir_kata2.length()];
        } else if(hasilAkhir_kata1.length() == 0 || hasilAkhir_kata2.length() == 0){ // Jika hasil akhir dari kata ke 1 atau kata ke 2 sama dengan 0,
                                                                                     // maka edit distance nya adalah maksimal panjang dari antara kedua kata tersebut
            distance = Math.max(hasilAkhir_kata1.length(), hasilAkhir_kata2.length());
        }

        //kembalikan angka distance yang sudah didapatkan dari perhitungan edit distance
        return distance;
    }

    //Memperpendek string dengan memotong huruf yang sama di akhir pada kedua string
    public static String [] potongAkhir(String [] arr){
        // Ambil array ke 0 sebagai kata dari query (kata ke-1) dan array ke 1 sebagai kata dari index (kata ke-2) 
        String query = arr[0];
        String cari = arr[1];

        // Iterator i dan j (posisi huruf yang di cek) di-inisialisasi dari akhir kedua string
        int i = query.length() - 1;
        int j = cari.length() - 1;

        // Loop selama nilai i dan j masih memiliki huruf (lebih besar sama dengan 0) 
        // dan selama kedua huruf dari kata ke-1 dan kata ke-2 sama, jika kedua huruf sama, maka posisi i dan j dikurang 1
        while (i >= 0 && j >= 0 && query.charAt(i) == cari.charAt(j)) {
            i--;
            j--;
        }
        // Jika loop berhenti (kondisi sudah tidak terpenuhi), maka potong kedua string dari posisi awal (0) hingga
        // posisi i+1 untuk kata ke-1 dan posisi j+1 untuk kata ke-2, lalu kembalikan dalam bentuk array
        return new String[]{query.substring(0, i+1), cari.substring(0, j+1)};
    }

    //Memperpendek string dengan memotong huruf yang sama di awal pada kedua string
    public static String [] potongAwal(String [] arr){
        // Ambil array ke 0 sebagai kata dari query (kata ke-1) dan array ke 1 sebagai kata dari index (kata ke-2) 
        String query = arr[0];
        String cari = arr[1];

        // Iterator i dan j (posisi huruf yang di cek) di-inisialisasi dari akhir kedua string
        int i = query.length() - 1;
        int j = cari.length() - 1;

        // Inisialisasi posisi awal dari huruf yaitu 0
        int posisiAwal = 0;

        // Loop selama posisi awal dari kedua huruf masih lebih kecil dari panjang kata ke-1 dan kata ke-2
        // dan selama kedua huruf dari kata ke-1 dan kata ke-2 sama, jika kedua huruf sama, maka posisi awal ditambah 1 
        while (posisiAwal <= i && posisiAwal <= j && query.charAt(posisiAwal) == cari.charAt(posisiAwal)) {
            posisiAwal++;
        }

        // Jika loop berhenti (kondisi sudah tidak terpenuhi), maka potong kedua string dari 
        // isi variable posisiAwal hingga akhir string, lalu kembalikan dalam bentuk array
        return new String[]{query.substring(posisiAwal), cari.substring(posisiAwal)};
    }
}