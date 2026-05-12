package Source_Code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import java.util.TreeSet;

public class BooleanQueryParser {
    HashMap<String, LinkedList<Integer>> invertedIndex = null;
    LinkedList<Integer> docAll;

    public BooleanQueryParser(HashMap<String, LinkedList<Integer>> invertedIndex) {
        this.invertedIndex = invertedIndex;
        docAll = getAllDocIds();
        // System.out.println("ALL DOCS: " + docAll.toString());
    }

    public String[] tokenize(String query) {
        ArrayList<String> token = new ArrayList<>();
        String current = ""; // helper untuk mengambil term kata
        // memproses query yang didapatkan menjadi token token, dipisahkan berdasarkan
        // '(', ')', 'AND', 'not', 'OR', "<term pada query>"
        for (int i = 0; i < query.length(); i++) {
            char curr = query.charAt(i);
            // cek curr apakah whitespace atau simbol atau huruf
            switch (curr) {
                case ' ':
                    // kalo misal ada whitespace, cek apakah current mengandung kata, kalo ya, maka
                    // masukkan dulu kata itu menjadi sebuah token
                    if (!current.equals("")) {
                        token.add(current);
                        current = "";
                    }
                    break;
                case '(':
                case ')':
                    if (!current.equals("")) {
                        token.add(current);
                        current = "";
                    }
                    token.add(String.valueOf(curr));
                    break;
                default:
                    current += curr;
                    break;
            }
        }
        // tokenize kata terakhir dalam query
        if (!current.equals("")) {
            token.add(current);
        }

        return token.toArray(new String[0]);
    }

    // method untuk melakukan shunting yard algorithm, dimana mengubah query infix
    // menjadi postfix agar lebih mudah untuk di proses
    public String[] infixToPostfix(String[] tokens) {
        Stack<String> temp = new Stack<>();
        ArrayList<String> output = new ArrayList<>();
        HashMap<String, Integer> precedence = new HashMap<>();

        precedence.put("not", 3);
        precedence.put("NOT", 3);
        precedence.put("and", 2);
        precedence.put("AND", 2);
        precedence.put("or", 1);
        precedence.put("OR", 1);

        for (String token : tokens) {
            // jika token yang sedang diproses merupakan operator, cek presecedence lalu
            // push ke stack sesuai precedence
            token = token.toLowerCase();
            if (precedence.containsKey(token)) {
                // selama precedence operator yang di stack lebih besar/sama dengan token, maka
                // masukkan operand ke output
                while (!temp.isEmpty() && !temp.peek().equals("(")
                        && precedence.get(token) <= precedence.get(temp.peek())) {
                    output.add(temp.pop());
                }

                temp.push(token);
            }
            // jika token merupakan "(", maka push ke stack
            else if (token.equals("(")) {
                temp.push(token);
            }
            // jika token merupakan ")", maka pop stack dan masukkan ke output hingga ketemu
            // "("
            else if (token.equals(")")) {
                while (!temp.peek().equals("(")) {
                    output.add(temp.pop());
                }
                temp.pop();
            }
            // jika token merupakan term, maka masukkan ke output
            else {
                output.add(token);
            }
        }
        while (!temp.isEmpty()) {
            output.add(temp.pop());
        }

        return output.toArray(new String[0]);
    }

    // method untuk mengoperasikan query berdasarkan postfix
    public LinkedList<Integer> evaluatePostfix(String[] postfix) {
        Stack<LinkedList<Integer>> temp = new Stack<>();

        // ambil token dari postfix, lalu proses sesuai operator
        for (String token : postfix) {
            // jika ditemukan operator AND, ambil 2 kata dari stack dan lakukan intersection
            token = token.toLowerCase();
            if (token.equals("and")) {
                // intersection
                LinkedList<Integer> right = temp.pop();
                LinkedList<Integer> left = temp.pop();
                temp.push(intersect(left, right));
            }
            // jika ditemukan operator OR, maka lakukan union
            else if (token.equals("or")) {
                // union
                LinkedList<Integer> right = temp.pop();
                LinkedList<Integer> left = temp.pop();
                temp.push(union(left, right));
            }
            // jika ditemukan operator NOT, maka lakukan complement
            else if (token.equals("not")) {
                LinkedList<Integer> operand = temp.pop();
                temp.push(complement(operand, docAll));
            }
            // jika token merupakan term, maka push POSTING LIST dari token ke stack
            else {
                // jika term tidak ditemukan di posting list, maka masukkan posting list kosong
                token = token.toLowerCase().replaceAll("[^a-zA-Z]", "");
                token = Stemmer.doPorterStemmer(token);
                LinkedList<Integer> pl = invertedIndex.get(token);
                if (pl == null) {
                    temp.push(new LinkedList<>());
                } else {
                    temp.push(pl);
                }
            }
        }
        return temp.pop();
    }

    private LinkedList<Integer> intersect(LinkedList<Integer> pL1, LinkedList<Integer> pL2) {
        LinkedList<Integer> result = new LinkedList<>();
        Iterator<Integer> it1 = pL1.iterator();
        Iterator<Integer> it2 = pL2.iterator();

        if (!it1.hasNext() || !it2.hasNext())
            return result;

        Integer val1 = it1.next();
        Integer val2 = it2.next();

        while (val1 != null && val2 != null) {
            if (val1.equals(val2)) {
                result.add(val1);
                val1 = it1.hasNext() ? it1.next() : null;
                val2 = it2.hasNext() ? it2.next() : null;
            } else if (val1 < val2) {
                val1 = it1.hasNext() ? it1.next() : null;
            } else {
                val2 = it2.hasNext() ? it2.next() : null;
            }
        }

        return result;
    }

    private LinkedList<Integer> union(LinkedList<Integer> pL1, LinkedList<Integer> pL2) {
        LinkedList<Integer> result = new LinkedList<>();

        Iterator<Integer> it1 = pL1.iterator();
        Iterator<Integer> it2 = pL2.iterator();

        Integer val1 = it1.hasNext() ? it1.next() : null;
        Integer val2 = it2.hasNext() ? it2.next() : null;

        while (val1 != null || val2 != null) {
            // jika posting list 1 sudah habis, masukkan sisa posting list 2
            if (val1 == null) {
                result.add(val2);
                val2 = it2.hasNext() ? it2.next() : null;
            }
            // jika posting list 2 sudah habis, masukkan sisa posting list 1
            else if (val2 == null) {
                result.add(val1);
                val1 = it1.hasNext() ? it1.next() : null;
            }
            // jika beririsan, masukkan salah satu
            else if (val1.equals(val2)) {
                result.add(val1);
                val1 = it1.hasNext() ? it1.next() : null;
                val2 = it2.hasNext() ? it2.next() : null;
            }
            // masukkan semua secara berurutan, mulai dari id dokumen yang lebih kecil
            // hingga terbesar
            else if (val1 < val2) {
                result.add(val1);
                val1 = it1.hasNext() ? it1.next() : null;
            } else {
                result.add(val2);
                val2 = it2.hasNext() ? it2.next() : null;
            }
        }

        return result;
    }

    private LinkedList<Integer> complement(LinkedList<Integer> operandPL, LinkedList<Integer> docAll) {
        // ide nya, harus tau semua docIDAll yang ada, terus dibandingin sama dokumen
        // yang di not, yang beririsan gausah dimasukkin
        LinkedList<Integer> result = new LinkedList<>();
        Iterator<Integer> itUniv = docAll.iterator();
        Iterator<Integer> itL1 = operandPL.iterator();

        Integer valUniv = itUniv.hasNext() ? itUniv.next() : null;
        Integer valL1 = itL1.hasNext() ? itL1.next() : null;

        while (valUniv != null) {
            // jika operandPL sudah habis, maka semua sisa di docAll adalah hasil
            if (valL1 == null) {
                result.add(valUniv);
                valUniv = itUniv.hasNext() ? itUniv.next() : null;
            }
            // jika beririsan, tidak perlu dimasukkan ke result
            else if (valUniv.equals(valL1)) {
                valUniv = itUniv.hasNext() ? itUniv.next() : null;
                valL1 = itL1.hasNext() ? itL1.next() : null;
            }
            // jika nilai di docAll lebih kecil, berarti tidak ada di operandPL, masukkan ke
            // hasil
            else if (valUniv < valL1) {
                result.add(valUniv);
                valUniv = itUniv.hasNext() ? itUniv.next() : null;
            }
            // jika nilai di operandPL lebih kecil, skip
            else {
                valL1 = itL1.hasNext() ? itL1.next() : null;
            }
        }
        return result;
    }

    // method helper untuk mendapatkan docAll yang nantinya akan digunakan di
    // complement
    public LinkedList<Integer> getAllDocIds() {
        TreeSet<Integer> allIds = new TreeSet<>();
        for (LinkedList<Integer> list : invertedIndex.values()) {
            allIds.addAll(list);
        }
        return new LinkedList<>(allIds);
    }
}