package main;

import data.ConvertInfo;
import data.Rule;
import data.Tuple;
import util.Log;

import java.io.*;
import java.util.*;

import static main.Main.baseURL;

/**
 * Created by gcc on 18-9-25.
 */
public class ReliableScoreAnalyse {

    public static int getTotalGammas(List<HashMap<String, ArrayList<Integer>>> domain_groups) {
        int count = 0;
        for (int i = 0; i < domain_groups.size(); i++) {
            HashMap<String, ArrayList<Integer>> groups = domain_groups.get(i);
            Iterator<Map.Entry<String, ArrayList<Integer>>> iter = groups.entrySet().iterator();
            while (iter.hasNext()) {
                ArrayList<Integer> ids = iter.next().getValue();
                count += ids.size();
            }
        }
        return count;
    }

    /**
     * 分析R-score，以gamma为最小单位
     */
    public static void reliabilityScoreAnalysisByGamma(String testDataFile, String groundDataFile, Log log) {

        List<HashMap<String, ArrayList<Integer>>> testDataGroups = readGroupFile(testDataFile);
        List<HashMap<String, ArrayList<Integer>>> groundDataGroups = readGroupFile(groundDataFile);
        int trueFixSize = 0;
        int totalFixSize = getTotalGammas(testDataGroups);
        int totalGroundSize = getTotalGammas(groundDataGroups);
        for (int i = 0; i < testDataGroups.size(); i++) {
            HashMap<String, ArrayList<Integer>> test_groups = testDataGroups.get(i);
            totalFixSize += test_groups.size();
            HashMap<String, ArrayList<Integer>> ground_groups = groundDataGroups.get(i);
            totalGroundSize += ground_groups.size();
            Iterator<Map.Entry<String, ArrayList<Integer>>> iter = test_groups.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, ArrayList<Integer>> entry = iter.next();
                String content = entry.getKey();
                ArrayList<Integer> cleanedIDs = entry.getValue();
                ArrayList<Integer> true_cleanedIDs = (ArrayList<Integer>) cleanedIDs.clone();
                if (ground_groups.containsKey(content)) {
                    ArrayList<Integer> groundIDs = ground_groups.get(content);
                    true_cleanedIDs.retainAll(groundIDs);
                    trueFixSize += true_cleanedIDs.size();
                }
            }
        }
        System.out.println(">>>>>>Reliability Score Analysis<<<<<<");
        log.write("\n>>>>>>Reliability Score Analysis<<<<<<\n");
        double precision = (double) trueFixSize / totalFixSize;
        double recall = (double) trueFixSize / totalGroundSize;
        System.out.print("precision = " + precision + "\n");
        System.out.print("recall = " + recall + "\n");
        log.write("rs precision = " + precision + "\n");
        log.write("rs recall = " + recall + "\n");
        System.out.println();
    }

    /**
     * 分析R-score，以group为最小单位
     */
    public static void reliabilityScoreAnalysis(String testDataFile, String groundDataFile, Log log) {

        List<HashMap<String, ArrayList<Integer>>> testDataGroups = readGroupFile(testDataFile);
        List<HashMap<String, ArrayList<Integer>>> groundDataGroups = readGroupFile(groundDataFile);
        int trueFixSize = 0;
        int totalFixSize = 0;
        int totalGroundSize = 0;
        for (int i = 0; i < testDataGroups.size(); i++) {
            HashMap<String, ArrayList<Integer>> test_groups = testDataGroups.get(i);
            totalFixSize += test_groups.size();
            HashMap<String, ArrayList<Integer>> ground_groups = groundDataGroups.get(i);
            totalGroundSize += ground_groups.size();
            Iterator<Map.Entry<String, ArrayList<Integer>>> iter = test_groups.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, ArrayList<Integer>> entry = iter.next();
                String content = entry.getKey();
                if (ground_groups.get(content) != null) {
                    trueFixSize++;
                }
            }
        }
        System.out.println(">>>>>>Reliability Score Analysis<<<<<<");
        log.write("\n>>>>>>Reliability Score Analysis<<<<<<\n");
        double precision = (double) trueFixSize / totalFixSize;
        double recall = (double) trueFixSize / totalGroundSize;
        System.out.print("precision = " + precision + "\n");
        System.out.print("recall = " + recall + "\n");
        log.write("rs precision = " + precision + "\n");
        log.write("rs recall = " + recall + "\n");
        System.out.println();
    }

    public static List<HashMap<String, ArrayList<Integer>>> readGroupFile(String filename) {

        List<HashMap<String, ArrayList<Integer>>> groupsList = new ArrayList<>();

        FileReader reader;
        String fileURL = filename;
        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            int Domain_i = 0;
            String content = "";
            while ((str = br.readLine()) != null) {
                if (str.contains("Domain")) {
                    Domain_i = Integer.parseInt(str.replaceAll("Domain", ""));
                    HashMap<String, ArrayList<Integer>> map = new HashMap<>();
                    groupsList.add(map);
                } else if (str.contains("content:")) {
                    content = str.replaceAll("content:", "");
//                    groupsList.get(Domain_i).put(content,new ArrayList<>());
                } else if (str.contains("TupleID:")) {
                    str = str.replaceAll("TupleID:", "");
                    String[] ids = str.split(",");
                    ArrayList<Integer> list = new ArrayList<>();
                    for (String tupleID : ids) {
                        list.add(Integer.parseInt(tupleID));
                    }
                    groupsList.get(Domain_i).put(content, list);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return groupsList;
    }

    /**
     * 根据convertInfo,将ground_truth的groups写入文件
     */
    public static void writeGroundGroupsToFile(String outFile, List<HashMap<String, ConvertInfo>> groupsList) {
        File file = new File(outFile);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (file.exists()) {// 判断文件是否存在
                System.out.println("文件已存在: " + outFile);
            } else if (!file.getParentFile().exists()) {// 判断目标文件所在的目录是否存在
                // 如果目标文件所在的文件夹不存在，则创建父文件夹
                System.out.println("目标文件所在目录不存在，准备创建它！");
                if (!file.getParentFile().mkdirs()) {// 判断创建目录是否成功
                    System.out.println("创建目标文件所在的目录失败！");
                }
            } else {
                file.createNewFile();
            }
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);

            for (int i = 0; i < groupsList.size(); i++) {
                bw.write("Domain" + i + "\n");
                HashMap<String, ConvertInfo> groupValueMap = groupsList.get(i);//每个domain下的groupValueMap
                Iterator<Map.Entry<String, ConvertInfo>> iter = groupValueMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, ConvertInfo> entry = iter.next();
                    ConvertInfo convertInfo = entry.getValue();
                    String content = convertInfo.content;
                    bw.write("content:" + content + "\n");
                    ArrayList<Integer> tupleIDlist = convertInfo.idlist;
                    bw.write("TupleID:");
                    for (int k = 0; k < tupleIDlist.size(); k++) {
                        Integer tupleID = tupleIDlist.get(k);
                        if (k != tupleIDlist.size() - 1) {
                            bw.write(tupleID + ",");
                        } else {
                            bw.write(tupleID + "\n");
                        }
                    }
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeGroupsToFile(String filename, List<HashMap<String, ArrayList<Integer>>> groupsList) {
        String outFile = filename;
        File file = new File(outFile);
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (file.exists()) {// 判断文件是否存在
                System.out.println("文件已存在: " + outFile);
            } else if (!file.getParentFile().exists()) {// 判断目标文件所在的目录是否存在
                // 如果目标文件所在的文件夹不存在，则创建父文件夹
                System.out.println("目标文件所在目录不存在，准备创建它！");
                if (!file.getParentFile().mkdirs()) {// 判断创建目录是否成功
                    System.out.println("创建目标文件所在的目录失败！");
                }
            } else {
                file.createNewFile();
            }
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);

            for (int i = 0; i < groupsList.size(); i++) {
                bw.write("Domain" + i + "\n");
                HashMap<String, ArrayList<Integer>> groupValueMap = groupsList.get(i);//每个domain下的groupValueMap
                Iterator<Map.Entry<String, ArrayList<Integer>>> iter = groupValueMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, ArrayList<Integer>> entry = iter.next();
                    String content = entry.getKey();
                    bw.write("content:" + content + "\n");
                    ArrayList<Integer> tupleIDlist = entry.getValue();
                    bw.write("TupleID:");
                    for (int k = 0; k < tupleIDlist.size(); k++) {
                        Integer tupleID = tupleIDlist.get(k);
                        if (k != tupleIDlist.size() - 1) {
                            bw.write(tupleID + ",");
                        } else {
                            bw.write(tupleID + "\n");
                        }
                    }
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args, Log log, int threshold) {

    }
}
