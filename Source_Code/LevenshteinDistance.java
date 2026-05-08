package Source_Code;
public class LevenshteinDistance {
    
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
