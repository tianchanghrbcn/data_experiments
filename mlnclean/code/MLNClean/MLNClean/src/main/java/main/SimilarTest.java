package main;

import info.debatty.java.stringsimilarity.*;
import spellchecker.SpellChecker;

/**
 * Created by gcc on 17-12-23.
 */
public class SimilarTest {
    public static void main(String[] args) {
        //String s1 = "tsx,amgeneral,convertible,2010,new,fwd,4,2.4li4";
        String s1 = "DOTH,AL";
        //cl,acura,coupe,1999,used,fwd,2,3.0lv6
        // tl,acura,sedan,1999,used,fwd,4,3.2lv6
        // a4,audi,sedan,2005,used,fwd,2,3.0lv6

        String s2 = "DOTHAN,AL";
        String[] out_content = s1.split(",");
        String[] curr_content = s2.split(",");

        Damerau damerau = new Damerau();
        Cosine cosine = new Cosine();
        QGram qGram = new QGram();
        double qgR = qGram.distance(s1, s2);
        double lcsR = new MetricLCS().distance(s1, s2);
        double lsR = new NormalizedLevenshtein().distance(s1, s2);
        double cosR = cosine.distance(s1, s2);
        double damerauR = damerau.distance(s1, s2);
        double jaccardR = new Jaccard().distance(s1, s2);
        double jaroWinklerR = new JaroWinkler().distance(s1, s2);
        double LevenshteinR = new Levenshtein().distance(s1, s2);
        double NGramR = new NGram().distance(s1, s2);
        double optimalStringAlignment = new OptimalStringAlignment().distance(s1, s2);
        double sorensenDice = new SorensenDice().distance(s1, s2);
        double ed = SpellChecker.distance(s1, s2);
        double LD = 0;
        double CD = 0;
        for (int j = 0; j < out_content.length; j++) {
            if (!out_content[j].equals(curr_content[j])) {
                CD += cosine.distance(out_content[j], curr_content[j]);
                LD += SpellChecker.distance(out_content[j], curr_content[j]);
            }
        }
        /*System.out.println("LD = " + LD);
        System.out.println("CD = " + CD);*/

        System.out.println("LD = " + SpellChecker.distance(s1,s2));
        System.out.println("CD = " + cosine.distance(s1,s2));

        /*System.out.println("lcs = " + lcsR);
        System.out.println("NormalizedLevenshtein = " + lsR);

        System.out.println("QGram = " + qgR);
        System.out.println("Damerau = " + damerauR);
        System.out.println("Jaccard = " + jaccardR);
        System.out.println("JaroWinkler = " + jaroWinklerR);
        System.out.println("Levenshtein = " + LevenshteinR);
        System.out.println("NGram = " + NGramR);
        System.out.println("OptimalStringAlignment = " + optimalStringAlignment);
        System.out.println("sorensenDice = " + sorensenDice);*/
    }
}
