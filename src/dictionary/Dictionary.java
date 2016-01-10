package dictionary;

import com.sun.deploy.util.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by Sebastian on 25.12.2015.
 */
public class Dictionary {
    private static Logger L = LogManager.getLogger(Dictionary.class.getName());
    static {
        L = Main.setupLogger(L);
    }

    private File f;

    private HashMap<Character, HashMap<Character, LinkedList<String>>> dictionaryMap;
    private HashMap<Character, LinkedList<String>> dictionaryMap2;

    public Dictionary(File f, HashMap<Character, HashMap<Character, LinkedList<String>>> map1, HashMap<Character, LinkedList<String>> map2) {
        this.f = f;
        this.dictionaryMap = map1;
        this.dictionaryMap2 = map2;
    }

    public String[] update(String word) {
        L.info("search for '" + word + "'");
        LinkedList<String> list = new LinkedList<>();

        if(word.length() == 0)
            return new String[]{"..."};
        char c1 = word.charAt(0);
        if(!dictionaryMap.containsKey(c1))
            return new String[]{"Es wurden kein Wort gefunden.", "?+"};

        if(word.length() == 1) {
            LinkedList<String> secondtList = (LinkedList<String>) dictionaryMap2.get(c1).clone();
            return sort(word, secondtList);
        }
        char c2 = word.charAt(1);
        if(!dictionaryMap.get(c1).containsKey(c2)) {
            return new String[]{"Es wurden kein Wort gefunden.", "?+"};
        }
        LinkedList<String> firstList = (LinkedList<String>) dictionaryMap.get(c1).get(c2).clone();

        return sort(word, firstList);
    }

    private String[] sort(String word, LinkedList<String> list) {
        HashMap<Integer, VoteEntry> votes = new HashMap<>();
        for(int i = 0; i < list.size(); i++) {
            String word2 = list.get(i);
            //int vote = doLevenshteinDistance(word, word2);
            int vote = computeLevenshteinDistance(word, word2);
            votes.put(i, new VoteEntry(i, vote));
        }

        boolean equalsWord = false;

        int maxVote = 20;
        VoteEntry e;
        while(votes.size() > 100 && maxVote > 0) {
            maxVote--;
            List<VoteEntry> voteList = new ArrayList<>(votes.values());
            //System.out.println("minvote=" + maxVote + " size=" + votes.size());
            for(int i = 0; i < votes.size(); i++) {
                e = voteList.get(i);
                if(e.getVote() == 0) {
                    equalsWord = true;
                }
                if(e.getVote() > maxVote) {
                    votes.remove(e.getIndex());
                }
            }
        }

        LinkedList<VoteEntry> entries = new LinkedList<>(votes.values());
        Collections.sort(entries, new Comparator<VoteEntry>() {
            @Override
            public int compare(VoteEntry o1, VoteEntry o2) {
                int v1 = o1.getVote();
                int v2 = o2.getVote();
                if(v1 > v2)
                    return 1;
                else if(v1 == v2)
                    return 0;
                else
                    return -1;
            }
        });

        LinkedList strings = new LinkedList();
        for(int i = 0; i < votes.size(); i++) {
            e = entries.get(i);
            //strings.add(e.getVote() + " - " + list.get(e.getIndex()));
            strings.add(list.get(e.getIndex()));
        }
        if(!equalsWord)
            strings.add("?+");

        return (String[]) strings.toArray(new String[strings.size()]);
    }

    private static int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    public static int computeLevenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];

        for (int i = 0; i <= lhs.length(); i++)
            distance[i][0] = i;
        for (int j = 1; j <= rhs.length(); j++)
            distance[0][j] = j;

        for (int i = 1; i <= lhs.length(); i++)
            for (int j = 1; j <= rhs.length(); j++)
                distance[i][j] = minimum(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));

        return distance[lhs.length()][rhs.length()];
    }

    private int doLevenshteinDistance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int [] costs = new int [b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    public void add(String s) throws IllegalArgumentException {
        if(s.length() <= 2) {
            throw new IllegalArgumentException("Das Wort muss mindestens 2 Buchstaben lang sein.");
        }
        char c1 = s.charAt(0);
        char c2 = s.charAt(1);

        if(!dictionaryMap.containsKey(c1))
            dictionaryMap.put(c1, new HashMap<>());

        if(!dictionaryMap.get(c1).containsKey(c2)) {
            dictionaryMap.get(c1).put(c2, new LinkedList<>());
        }

        if(!dictionaryMap.get(c1).get(c2).contains(s))
            dictionaryMap.get(c1).get(c2).add(s);

        if(!dictionaryMap2.containsKey(c1)) {
            dictionaryMap2.put(c1, new LinkedList<>());
        }

        if(!dictionaryMap2.get(c1).contains(s))
            dictionaryMap2.get(c1).add(s);

        L.info("Add '" + s + "' to dictionary");
    }

    public void save() throws IOException {
        save(this, f);
    }

    public HashMap<Character, HashMap<Character, LinkedList<String>>> getDictionaryMap1() {
        return dictionaryMap;
    }

    public HashMap<Character, LinkedList<String>> getDictionaryMap2() {
        return dictionaryMap2;
    }


    public static Dictionary getDictionary(String pathTo) {
        try {
            L.info("load dictionary in '" + pathTo + "'.");
            File f = new File(pathTo);
            Dictionary d = new Dictionary(f, parsFileToFirstMap(f), parsFileToSecondMap(f));
            L.info("dictionary is loaded");
            return d;
        } catch (IOException e) {

            e.printStackTrace();
            System.exit(3);
        }
        return null;
    }

    private static HashMap<Character, HashMap<Character, LinkedList<String>>> parsFileToFirstMap(File f) throws IOException {
        LinkedList<String> list = new LinkedList<>();
        BufferedReader in = new BufferedReader(new FileReader(f));
        String input;
        while((input = in.readLine()) != null) {
            list.add(input);
        }


        HashMap<Character, HashMap<Character, LinkedList<String>>> dictionaryMap = new HashMap<>();

        int counter = 0;
        for(String s : list) {
            char c1 = s.charAt(0);
            if(!dictionaryMap.containsKey(c1)) {
                dictionaryMap.put(c1, new HashMap<>());
            }

            char c2 = s.charAt(1);
            if(!dictionaryMap.get(c1).containsKey(c2)) {
                dictionaryMap.get(c1).put(c2, new LinkedList<>());
            }

            dictionaryMap.get(c1).get(c2).add(s);
        }

        return dictionaryMap;
    }


    private static HashMap<Character, LinkedList<String>> parsFileToSecondMap(File f) throws IOException {
        LinkedList<String> list = new LinkedList<>();
        BufferedReader in = new BufferedReader(new FileReader(f));
        String input;
        while((input = in.readLine()) != null) {
            list.add(input);
        }


        HashMap<Character, LinkedList<String>> dictionaryMap = new HashMap<>();

        int counter = 0;
        for(String s : list) {
            char c1 = s.charAt(0);
            if(!dictionaryMap.containsKey(c1)) {
                dictionaryMap.put(c1, new LinkedList<>());
            }

            dictionaryMap.get(c1).add(s);
        }

        return dictionaryMap;
    }

    public static void save(Dictionary d, File f) throws IOException {
        L.info("save dictionary to'" + f.getPath() + "'.");
        HashMap<Character, LinkedList<String>> secondMap = d.getDictionaryMap2();
        PrintWriter out = new PrintWriter(new FileWriter(f));

        for(char c : secondMap.keySet()) {
            LinkedList<String> list = secondMap.get(c);
            for(String s : list) {
                out.println(s);
            }
        }

    }


    private static class VoteEntry {
        private int index;
        private int vote;

        public VoteEntry(int index, int vote) {
            this.index = index;
            this.vote = vote;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getVote() {
            return vote;
        }

        public void setVote(int vote) {
            this.vote = vote;
        }
    }

    private static class LetterEntriy {
        private char letter;
        private HashMap<Character, LetterEntriy> map = new HashMap<>();

        public LetterEntriy(char letter) {
            this.letter = letter;
        }

        public char getLetter() {
            return letter;
        }

        public void setLetter(char letter) {
            this.letter = letter;
        }

        public HashMap<Character, LetterEntriy> getMap() {
            return map;
        }

        public void setMap(HashMap<Character, LetterEntriy> map) {
            this.map = map;
        }
    }
}
