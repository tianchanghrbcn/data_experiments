package util;

import java.util.*;
import java.util.regex.Pattern;

import info.debatty.java.stringsimilarity.*;

public class Similarity {
    private static final Pattern SPACE_REG = Pattern.compile("\\s+");
    private final int k = 3;

    public static int maxLength(String str1, String str2) {
        int maxLength = 0;
        int len_str1 = str1.length();
        int len_str2 = str2.length();
        if (len_str1 > len_str2) {
            maxLength = len_str1;
        } else {
            maxLength = len_str2;
        }
        return maxLength;
    }

    public float cosineSimilarity(String s1, String s2) {
        if(s1 == null) {
            throw new NullPointerException("s1 must not be null");
        } else if(s2 == null) {
            throw new NullPointerException("s2 must not be null");
        } else if(s1.equals(s2)) {
            return 1.0f;
        } else if(s1.length() >= 2 && s2.length() >= 2) {
            Map<String, Integer> profile1 = getProfile(s1);
            Map<String, Integer> profile2 = getProfile(s2);
            return (float)(dotProduct(profile1, profile2) / (norm(profile1) * norm(profile2)));
        } else {
            return 0.0f;
        }
    }

    public final Map<String, Integer> getProfile(String string) {
        HashMap<String, Integer> shingles = new HashMap();
        String string_no_space = SPACE_REG.matcher(string).replaceAll(" ");

        for(int i = 0; i < string_no_space.length() - this.k + 1; ++i) {
            String shingle = string_no_space.substring(i, i + this.k);
            Integer old = (Integer)shingles.get(shingle);
            if(old != null) {
                shingles.put(shingle, Integer.valueOf(old.intValue() + 1));
            } else {
                shingles.put(shingle, Integer.valueOf(1));
            }
        }

        return Collections.unmodifiableMap(shingles);
    }


    private static double norm(Map<String, Integer> profile) {
        double agg = 0.0D;

        Map.Entry entry;
        for(Iterator var3 = profile.entrySet().iterator(); var3.hasNext(); agg += 1.0D * (double)((Integer)entry.getValue()).intValue() * (double)((Integer)entry.getValue()).intValue()) {
            entry = (Map.Entry)var3.next();
        }

        return Math.sqrt(agg);
    }

    private static double dotProduct(Map<String, Integer> profile1, Map<String, Integer> profile2) {
        Map<String, Integer> small_profile = profile2;
        Map<String, Integer> large_profile = profile1;
        if(profile1.size() < profile2.size()) {
            small_profile = profile1;
            large_profile = profile2;
        }

        double agg = 0.0D;
        Iterator var6 = small_profile.entrySet().iterator();

        while(var6.hasNext()) {
            Map.Entry<String, Integer> entry = (Map.Entry)var6.next();
            Integer i = (Integer)large_profile.get(entry.getKey());
            if(i != null) {
                agg += 1.0D * (double)((Integer)entry.getValue()).intValue() * (double)i.intValue();
            }
        }

        return agg;
    }

    public static float jaccardSimilarity(String str1, String str2) {
        String[] s1 = str1.split(" ");
        String[] s2 = str2.split(" ");

        HashSet<String> hashSet = new HashSet<String>();
        ArrayList<String> inter = new ArrayList<String>();
        ArrayList<String> outside = new ArrayList<String>();
        for (String s : s1) {
            hashSet.add(s);
        }

        for (String s : s2) {
            if (hashSet.contains(s)) {
                inter.add(s);
            } else {
                outside.add(s);
            }
        }

        float dist = (float) inter.size() / (outside.size() + hashSet.size());

        return dist;
    }

    public static float editSimilarity(String str1, String str2) {
        Levenshtein l = new Levenshtein();
        float dist = (float) (1 - (l.distance(str1, str2) / maxLength(str1, str2)));
        return dist;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        System.out.println(new Cosine().similarity("clab", "cla"));
        System.out.println(new Similarity().cosineSimilarity("clab", "cla"));
    }

}
