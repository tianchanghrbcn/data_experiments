package util;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by gcc on 18-6-16.
 */
public class Duplicate {
    public static float[] allRate = {0.05f};//0.05f, 0.1f, 0.15f, 0.2f, 0.25f, 0.3f, 0.35f, 0.4f
    public static float[] ratio = {0, 0.25f, 0.5f, 0.75f, 1};//0, 0.25f, 0.5f, 0.75f, 1
    public static float replaceRate = 0.0f;
    public static float substrRate = 0.0f;
    public static String baseURL = "/data/nw/DC_ED/References_inner_and_outer/mlnclean/dataset/tpch/1w/";
    public static String inFile = baseURL + "RDBSCleaner_cleaned.csv";
    public static String ground_file = "/data/nw/DC_ED/References_inner_and_outer/mlnclean/dataset/tpch/1w/testData0.csv";


    public static void main(String[] args) {

        for (int i = 0; i < allRate.length; i++) {
            for (float curr_ratio : ratio) {
                replaceRate = curr_ratio * allRate[i];
                substrRate = (1 - curr_ratio) * allRate[i];

                DecimalFormat decimalFormat = new DecimalFormat(".00");
                String s1 = "";
                String s2 = "";
                if (Float.toString(replaceRate).length() < 4) {
                    s1 = (Float.toString(replaceRate)) + "0";
//                    if (s1.equals("0.0")) s1 = "0.00";
                } else {
                    s1 = (Float.toString(replaceRate)).substring(0, 4);
                }
                if (Float.toString(allRate[i]).length() < 4) {
                    s2 = (Float.toString(allRate[i])) + "0";
                } else {
                    s2 = (Float.toString(allRate[i])).substring(0, 4);
                }
                System.out.print(s1 + "_" + s2 + ":\t");
                String cleaned_file = inFile.replaceAll("RDBSCleaner_cleaned.csv", s1 + "_" + s2 + "/RDBSCleaner_cleaned.csv");
                deduplicate(cleaned_file, ground_file);
            }
        }
    }

    public static HashMap<String, ArrayList<String>> readFile(String curr_inFile) {
        HashMap<String, ArrayList<String>> map = new HashMap<>();

        try {
            FileReader file = file = new FileReader(curr_inFile);
            BufferedReader br = new BufferedReader(file);
            String str = "";

            while ((str = br.readLine()) != null) {
                String key_content = str.substring(str.indexOf("|") + 1);
                String id = str.substring(0, str.indexOf("|"));
                if (map.containsKey(key_content)) {
                    map.get(key_content).add(id);
                } else {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(id);
                    map.put(key_content, list);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static boolean ifContain(String value, ArrayList<String> list) {
        boolean result = false;
        for (String str : list) {
            if (str.equals(value)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static void deduplicate(String cleanedFile, String groundFile) {
        HashMap<String, ArrayList<String>> cleaned_map = readFile(cleanedFile);
        HashMap<String, ArrayList<String>> ground_map = readFile(groundFile);
        int total_size = 0;
        int true_num = 0;
        Iterator<Map.Entry<String, ArrayList<String>>> ground_iter = ground_map.entrySet().iterator();
        while (ground_iter.hasNext()) {
            Map.Entry<String, ArrayList<String>> entry = ground_iter.next();
            String key_content = entry.getKey();
            ArrayList<String> ground_idList = entry.getValue();
            int curr_size = ground_idList.size();
            total_size += curr_size;
            if (cleaned_map.containsKey(key_content)) {
                ArrayList<String> cleand_idList = cleaned_map.get(key_content);
                for (String id : cleand_idList) {
                    if (ifContain(id, ground_idList)) {
                        true_num++;
                    }
                }
            }
        }
        double precision = (double) true_num / total_size;
        System.out.println("precision = " + precision);
    }

}
