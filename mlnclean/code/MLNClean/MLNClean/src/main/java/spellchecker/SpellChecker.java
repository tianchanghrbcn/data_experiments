package spellchecker;
import java.util.ArrayList;
import java.util.Set;


/**
 * 拼写纠错
 */
public class SpellChecker {

    public static void checkSpell(ArrayList<String> attributes , String term, double radius){
        //double radius = 10; // 编辑距离阈值
        //String term = "heli"; // 待纠错的词

        // 创建BK树
        MetricSpace<String> ms = new LevensteinDistance();
        BKTree<String> bk = new BKTree<String>(ms);

        for(int i=0;i<attributes.size();i++){
            bk.put(attributes.get(i));
        }
        ArrayList<String[]> list = bk.query(term, radius);
        for(int i=0;i<list.size();i++){
            System.out.println("D = " + list.get(i)[0]+"; " + list.get(i)[1]);

        }
    }

    public static int distance(String A, String B) {
        if(A.equals(B)) {
            //System.out.println(0);
            return 0;
        }
        //dp[i][j]表示源串A位置i到目标串B位置j处最低需要操作的次数
        int[][] dp = new int[A.length() + 1][B.length() + 1];
        for(int i = 1;i <= A.length();i++)
            dp[i][0] = i;
        for(int j = 1;j <= B.length();j++)
            dp[0][j] = j;
        for(int i = 1;i <= A.length();i++) {
            for(int j = 1;j <= B.length();j++) {
                if(A.charAt(i - 1) == B.charAt(j - 1))
                    dp[i][j] = dp[i - 1][j - 1];
                else {
                    dp[i][j] = Math.min(dp[i - 1][j] + 1,
                            Math.min(dp[i][j - 1] + 1, dp[i - 1][j - 1] + 1));
                }
            }
        }
        //System.out.println(dp[A.length()][B.length()]);
        return dp[A.length()][B.length()];
    }

    public static void main(String args[]) {
        String A = "10001abc";
        String B = "10011ac";
        SpellChecker.distance(A, B);
//        ArrayList<String> list = new ArrayList<String>();
//        list.add("10001abc");
//        list.add("10011ac");
//        list.add("10011abcd");
//        double radius = 10;
//        String term = "10011acd"; // 待纠错的词
//        checkSpell(list, term, radius);
    }
}