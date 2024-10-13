package main;

import data.ConvertInfo;
import data.Domain;
import data.Rule;
import data.Tuple;
import util.Log;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

import static main.Main.baseURL;

/**
 * Created by zju on 18-9-18.
 */
public class OutlierAnalyse {
    public static String filename = "outlier_analysis.txt";

    public static HashMap<Integer, String[]> minusDataset(HashMap<Integer, String[]> oldData, ArrayList<Integer> abnormalIDs) {
        HashMap<Integer, String[]> newData = new HashMap<>(oldData.size() - abnormalIDs.size());
        Iterator<Map.Entry<Integer, String[]>> iter = oldData.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, String[]> entry = iter.next();
            Integer tupleID = entry.getKey();
            if (!abnormalIDs.contains(tupleID)) {
                String[] tuple = entry.getValue();
                newData.put(tupleID, tuple);
            }
        }
        return newData;
    }

    public static void writeDatasetToFile(HashMap<Integer, String[]> dataset, String outFile, String[] header) {
        File file = new File(outFile);
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    System.out.println("mkdir");
                }
            } else {
                file.createNewFile();
            }
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);

            writer.write("ID," + Arrays.toString(header)
                    .replaceAll("[\\[\\]]", "")
                    .replaceAll(" ", ""));
            writer.newLine();//����

            Iterator<Map.Entry<Integer, String[]>> iter = dataset.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Integer, String[]> entry = iter.next();
                Integer tupleID = entry.getKey();
                String str = tupleID + ",";
                String[] tuple = entry.getValue();
                for (int i = 0; i < tuple.length; i++) {
                    if (i != tuple.length - 1) {
                        str += tuple[i] + ",";
                    } else str += tuple[i];
                }
                writer.write(str);
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 模拟crowd anser的结果的精度
     */
    public static ArrayList<ArrayList<Integer>> imitateCrowAns(double precision, ArrayList<ArrayList<Integer>> abnormalIDsList) {
        int rule_size = abnormalIDsList.size();
        ArrayList<ArrayList<Integer>> true_detected_abnormals = new ArrayList<>(rule_size);
        for (ArrayList<Integer> abnormalIDs : abnormalIDsList) {
            int total_size = abnormalIDs.size();
            int true_detect_size = (int) Math.round(total_size * precision);
            int count = 0;
            ArrayList<Integer> list = new ArrayList<>(true_detect_size);
            while (count < true_detect_size) {
                list.add(abnormalIDs.get(count));
                count++;
            }
            true_detected_abnormals.add(list);
        }
        return true_detected_abnormals;
    }

    /**
     * 根据百分比模拟crowdsourcing的过程
     */
    public static List<List<HashMap<Integer, Tuple>>> generateGroup(String database, double precision, String dirtyDataURL, String groundTruthURL, String ruleURL) {

        String[] header = new Rule().getHead(dirtyDataURL, ",");

        //Read rules file from disk
        List<Tuple> ruleList = new Rule().loadRules(ruleURL, ",", header);
        List<List<HashMap<Integer, Tuple>>> Domain_to_Groups = new ArrayList<>(ruleList.size());

        try {
            HashMap<Integer, String[]> dirty_data = Test.readWithID(dirtyDataURL);

            ArrayList<String> ground_data = Test.pickData(dirty_data, groundTruthURL);
            ground_data.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    int index1 = o1.indexOf(",");
                    int index2 = o2.indexOf(",");
                    int id1 = Integer.parseInt(o1.substring(0, index1));
                    int id2 = Integer.parseInt(o2.substring(0, index2));
                    if (id1 > id2) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
            String ground_sampleFile = groundTruthURL.substring(0, groundTruthURL.lastIndexOf("/")) + "/ground_sampleData.csv";
            Main.writeToFile(header, ground_data, ground_sampleFile);
            HashMap<Integer, String[]> ground_truth = Test.readWithID(ground_sampleFile);

            //检测哪些是abnormal tuples
            ArrayList<ArrayList<Integer>> old_abnormalIDsList = abnormalDetect(header, dirty_data, ground_truth, ruleList);

            ArrayList<ArrayList<Integer>> abnormalIDsList = imitateCrowAns(precision, old_abnormalIDsList);

            int ruleIndex = 0;
            //将每个rule对应的abnormalIDs加入对应的group中去
            for (ArrayList<Integer> abnormalIDs : abnormalIDsList) {
                List<Tuple> rules = new ArrayList<>();
                rules.add(ruleList.get(ruleIndex));

                //将删去abnormal的数据写入一个文件，并根据该数据集构建group
                HashMap<Integer, String[]> curr_dirty_data = minusDataset(dirty_data, abnormalIDs);
                String tmp_dataURL = dirtyDataURL.replaceAll("testData", "testData_tmp");
                writeDatasetToFile(curr_dirty_data, tmp_dataURL, header);

                //根据删除abnormal tuples的curr_dirty_data生成对应的group
                String splitString = ",";
                boolean ifHeader = true;
                Rule rule = new Rule();
                Domain domain = new Domain();
                rule.initData(tmp_dataURL, splitString, ifHeader);
                ArrayList<Integer> ignoredIDs = new Rule().findIgnoredIDs(rules, header);
                domain.header = header;
                List<HashMap<String, ConvertInfo>> convert_domains = new ArrayList<>(rules.size());
                domain.init(tmp_dataURL, splitString, ifHeader, rules, convert_domains, ignoredIDs);
                System.out.println(">>> Completed!");

                HashMap<Integer, Tuple> block = domain.domains.get(0);

                System.out.println(">>> Add abnormalities into corresponding group...");
                //将abnormal tuple添加到该rule对应的group中，并形成group
                List<HashMap<Integer, Tuple>> groups = addAbnormalToGroup(block, convert_domains.get(0), ruleList.get(ruleIndex), abnormalIDs, dirty_data, ground_truth);
                Domain_to_Groups.add(groups);
                System.out.println(">>> Completed!");

                //将该rule对应的所有groups的内容写入文件
                System.out.println(">>> Write groups info to file...");
                //String groupFile = dirtyDataURL.replaceAll("testData", "groups_of_r" + ruleIndex).replaceAll("\\.csv", ".txt");
                //writeGroupsOfsingleRule(groups, groupFile);
                System.out.println(">>> Completed!");
                ruleIndex++;
            }
            //将domain_to_Groups写入文件
            writeGroupsToFile(Domain_to_Groups, database + "/groups_reorganize.txt");//groupFileURL
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Domain_to_Groups;
    }


    /**
     * 通过与ground truth比较，找到真实的abnormal tuples
     * 返回对应于每个rule的abnormal tupleIDs
     */
    public static ArrayList<ArrayList<Integer>> abnormalDetect(String[] header, HashMap<Integer, String[]> dirty_data, HashMap<Integer, String[]> ground_truth, List<Tuple> ruleList) {
        ArrayList<ArrayList<Integer>> abnormalIDsList = null;
        try {
            abnormalIDsList = new ArrayList<>(ruleList.size());

            //挑出哪些attribute是reason
            ArrayList<int[]> reason_of_rules = new ArrayList<>(ruleList.size());
            for (Tuple rule : ruleList) {
                int[] reasonIDs = rule.reasonAttributeIndex;
                int[] curr_ids = new int[reasonIDs.length];

                for (int i = 0; i < reasonIDs.length; i++) {
                    curr_ids[i] = reasonIDs[i];
                }
                reason_of_rules.add(curr_ids);
                System.out.print(Arrays.toString(curr_ids) + " ");
            }
            System.out.println();


            for (int[] reasonIDs : reason_of_rules) {
                ArrayList<Integer> abnormalIDs = new ArrayList<>();
                Iterator<Map.Entry<Integer, String[]>> iter_ground = ground_truth.entrySet().iterator();
                Iterator<Map.Entry<Integer, String[]>> iter_dirty = dirty_data.entrySet().iterator();
                while (iter_ground.hasNext() && iter_dirty.hasNext()) {
                    Map.Entry<Integer, String[]> entry_ground = iter_ground.next();
                    Map.Entry<Integer, String[]> entry_dirty = iter_dirty.next();
                    String[] ground_tuple = entry_ground.getValue();
                    Integer dirty_tupleID = entry_dirty.getKey();
                    String[] dirty_tuple = entry_dirty.getValue();
                    //如果不相等，则说明存在error
                    boolean flag = false;
                    int abnormalID = 0;
                    if (!Arrays.toString(ground_tuple).equals(Arrays.toString(dirty_tuple))) {
                        //判断error是否在reason上
                        for (int i = 0; i < ground_tuple.length; i++) {
                            if (!ground_tuple[i].equals(dirty_tuple[i])) {
                                for (int reasonId : reasonIDs) {
                                    if (reasonId == i) {
                                        flag = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (flag) {
                        abnormalIDs.add(dirty_tupleID);
                        System.out.println(dirty_tupleID);
                    }
                }
                abnormalIDsList.add(abnormalIDs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return abnormalIDsList;
    }

    public static void writeConflictToFile(ArrayList<Integer> conflicts, String filename) {
        String outFile = baseURL + filename;
        File file = new File(outFile);
        FileWriter fw = null;
        BufferedWriter writer = null;
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
            writer = new BufferedWriter(fw);
            int i = 0;
            for (Integer tupleID : conflicts) {
                if (i == conflicts.size() - 1) {
                    writer.write("" + tupleID);
                } else {
                    writer.write(tupleID + ",");
                    i++;
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HashSet<Integer> readConflicts(String filename) {// read whereIsDirtyData.txt
        FileReader reader;
        String fileURL = filename;
        HashSet<Integer> hashSet = new HashSet<>();
        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            int key = 0;
            if ((str = br.readLine()) != null) {
                String[] tupleIDs = str.split(",");
                for (String t : tupleIDs) {
                    hashSet.add(Integer.parseInt(t));
                }

            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hashSet;
    }


    public static void conflictAnalysis(String data_args, Log log, int threshold) {

        HashMap<Integer, String[]> dataSet_v1 = readDataSet(data_args + "/clean_within_group.txt");

        HashSet<Integer> hashSet = readConflicts(data_args + "/conflicts_" + threshold + ".txt");
        int true_conflict_num = hashSet.size(); //true conflict tuple number
        int cleaned_conflict_num = 0;
        int total_error_num = 0;
        HashMap<Integer, String[]> dataset_ground_truth = readDataSet(data_args + "/ground_sampleData.csv"); //read ground truth file
        HashMap<Integer, String[]> dataset_dirty = readDataSet(data_args + "/RDBSCleaner_cleaned.txt"); //read ground truth file

        Iterator<Integer> iter = hashSet.iterator();
        while (iter.hasNext()) {
            int tupleID = iter.next();
            String tuple_ground_truth = Arrays.toString(dataset_ground_truth.get(tupleID));
            String tuple_dirty = Arrays.toString(dataset_dirty.get(tupleID));
            if (tuple_dirty.equals(tuple_ground_truth)) {
                cleaned_conflict_num++;
            }
        }

        Iterator<Map.Entry<Integer, String[]>> iter2 = dataSet_v1.entrySet().iterator();
        while (iter2.hasNext()) {
            Map.Entry<Integer, String[]> entry = iter2.next();
            Integer tupleID = entry.getKey();
            String tuple_v1 = Arrays.toString(entry.getValue());
            String tuple_ground_truth = Arrays.toString(dataset_ground_truth.get(tupleID));
            if (!tuple_v1.equals(tuple_ground_truth)) {
                total_error_num++;
            }
        }

        double precision = (double) cleaned_conflict_num / true_conflict_num;
        double recall = (double) cleaned_conflict_num / total_error_num;

        log.write("\nTotal error tuple number after clean_step1 = " + total_error_num);
        log.write("conflict cleaned_conflict_num = " + cleaned_conflict_num);
        log.write("conflict true_conflict_num = " + true_conflict_num+"\n");

        System.out.println("conflict resolution precision = " + precision);
        log.write("conflict resolution precision = " + precision);

        System.out.println("conflict resolution recall = " + recall);
        log.write("conflict resolution recall = " + recall + "\n");
    }

    public static List<HashMap<Integer, Tuple>> readDomainFile(String filename) {
        FileReader reader;
        String fileURL = baseURL + filename;
        List<HashMap<Integer, Tuple>> domainList = new ArrayList<>();
        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            int domain_index = 0;
            int[] idlist = null;
            String[] content = null;
            int key = 0;
            while ((str = br.readLine()) != null) {
                if (str.contains("domain")) {
                    str = str.replaceAll("domain", "");
                    domain_index = Integer.parseInt(str);
                    domainList.add(new HashMap<>());
                } else if (str.contains("AttrID:")) {
                    String[] ids = str.replaceAll("ID:", "").replaceAll(", ", ",").split(",");
                    idlist = new int[ids.length];
                    for (int i = 0; i < ids.length; i++) {
                        idlist[i] = Integer.parseInt(ids[i]);
                    }
                    domainList.get(domain_index).get(key).setAttributeIndex(idlist);
                } else if (str.contains("content:")) {
                    content = str.replaceAll("content:", "").replaceAll(", ", ",").split(",");
                    domainList.get(domain_index).get(key).setContext(content);
                } else {
                    key = Integer.parseInt(str);//tuple ID
                    domainList.get(domain_index).put(key, new Tuple());
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return domainList;
    }

    public static void writeDomainToFile(List<HashMap<Integer, Tuple>> domainList, String filename) {
        System.out.println("Write Domain List to FileURL=" + filename);
        String outFile = filename;
        File file = new File(outFile);
        FileWriter fw = null;
        BufferedWriter writer = null;
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
            writer = new BufferedWriter(fw);

            for (int i = 0; i < domainList.size(); i++) {
                HashMap<Integer, Tuple> domain = domainList.get(i);
                writer.write("domain" + i + "\n");
                Iterator<Map.Entry<Integer, Tuple>> iter = domain.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Integer, Tuple> entry = iter.next();
                    Tuple t = entry.getValue();
                    writer.write(entry.getKey() + "\n");  //tupleID
                    writer.write("AttrID:" + Arrays.toString(t.getAttributeIndex())
                            .replaceAll("\\[", "")
                            .replaceAll("]", "") + "\n");
                    writer.write("content:" + Arrays.toString(t.getContext())
                            .replaceAll("\\[", "")
                            .replaceAll("]", "") + "\n");
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Tuple setGamma(String[] tuple, Tuple rule) {
        Tuple t = new Tuple();
        t.reasonAttributeIndex = rule.reasonAttributeIndex;
        t.resultAttributeIndex = rule.resultAttributeIndex;
        t.setAttributeIndex(rule.getAttributeIndex());
        String[] content = new String[rule.getAttributeIndex().length];
        t.reason = new String[rule.reason.length];
        t.result = new String[rule.result.length];

        for (int i = 0; i < rule.reason.length; i++) {
            t.reason[i] = tuple[rule.reasonAttributeIndex[i]];
        }
        for (int i = 0; i < rule.result.length; i++) {
            t.result[i] = tuple[rule.resultAttributeIndex[i]];
        }
        for (int i = 0; i < rule.getAttributeIndex().length; i++) {
            content[i] = tuple[rule.getAttributeIndex()[i]];

        }
        t.setContext(content);
        return t;
    }

    /**
     * 对于一个rule，将abnormal加入对应的group中
     */
    public static List<HashMap<Integer, Tuple>> addAbnormalToGroup(HashMap<Integer, Tuple> domain, HashMap<String, ConvertInfo> convert_domain, Tuple rule,
                                                                   ArrayList<Integer> abnormalIDs, HashMap<Integer, String[]> dirty_data, HashMap<Integer, String[]> ground_data) {

        for (Integer tupleID : abnormalIDs) {
            String[] ground_t = ground_data.get(tupleID);
            Tuple ground_tuple = setGamma(ground_t, rule);

            String reasonKey = "";
            for (int k = 0; k < ground_tuple.reason.length; k++) {
                reasonKey += ground_tuple.reason[k];
                if (k != ground_tuple.reason.length - 1) {
                    reasonKey += ",";
                }
            }
            //将abnormal tuple add到对应的group中
            if (convert_domain.containsKey(reasonKey)) {
                convert_domain.get(reasonKey).idlist.add(tupleID);
            } else {
                ArrayList<Integer> idList = new ArrayList<>();
                idList.add(tupleID);
                ConvertInfo convertInfo = new ConvertInfo(idList, "");

                convert_domain.put(reasonKey, convertInfo);
            }

        }

        List<HashMap<Integer, Tuple>> groups = new ArrayList<>();
        Iterator<Map.Entry<String, ConvertInfo>> c_iter = convert_domain.entrySet().iterator();
        while (c_iter.hasNext()) {
            Map.Entry<String, ConvertInfo> entry = c_iter.next();
            ArrayList<Integer> ids = entry.getValue().idlist;
            HashMap<Integer, Tuple> group = new HashMap<>();
            for (Integer id : ids) {
                if (domain.get(id) != null) {
                    group.put(id, domain.get(id));
                } else {//说明是新加的abnormal id
                    String[] dirty_t = dirty_data.get(id);
                    Tuple dirty_tuple = setGamma(dirty_t, rule);
                    group.put(id, dirty_tuple);
                }
            }
            groups.add(group);
        }
        return groups;
    }

    /**
     * 只写入一个rule对应的group的内容
     */
    public static void writeGroupsOfsingleRule(List<HashMap<Integer, Tuple>> groups, String filename) {
        String outFile = filename;
        File file = new File(outFile);
        FileWriter fw = null;
        BufferedWriter writer = null;
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
            writer = new BufferedWriter(fw);

            for (int i = 0; i < groups.size(); i++) {
                writer.write("group" + i + "\n");

                HashMap<Integer, Tuple> group = groups.get(i);
                Iterator<Map.Entry<Integer, Tuple>> iter = group.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Integer, Tuple> entry = iter.next();
                    Integer tupleID = entry.getKey();
                    Tuple t = entry.getValue();
                    writer.write(tupleID + "\n");  //tupleID
                    writer.write("ID:" + Arrays.toString(t.getAttributeIndex())
                            .replaceAll("\\[", "")
                            .replaceAll("]", "") + "\n");
                    writer.write("content:" + Arrays.toString(t.getContext())
                            .replaceAll("\\[", "")
                            .replaceAll("]", "") + "\n");
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeGroupsToFile(List<List<HashMap<Integer, Tuple>>> domain_to_groups, String filename) throws IOException {
//        filename = "total_group_info.txt";
        String outFile = filename;
        File file = new File(outFile);
        FileWriter fw = null;
        BufferedWriter writer = null;
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
            writer = new BufferedWriter(fw);


            for (int i = 0; i < domain_to_groups.size(); i++) {
                List<HashMap<Integer, Tuple>> groups = domain_to_groups.get(i);

                if (groups.size() != 0) {
                    writer.write("domain" + i + "\n");
                }
                int group_index = 0;
                for (HashMap<Integer, Tuple> group : groups) {
                    writer.write("group" + group_index + "\n");
                    Iterator<Map.Entry<Integer, Tuple>> iter = group.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<Integer, Tuple> entry = iter.next();
                        Tuple t = entry.getValue();
                        writer.write(entry.getKey() + "\n");  //tupleID
                        writer.write("ID:" + Arrays.toString(t.getAttributeIndex())
                                .replaceAll("\\[", "")
                                .replaceAll("]", "") + "\n");
                        writer.write("content:" + Arrays.toString(t.getContext())
                                .replaceAll("\\[", "")
                                .replaceAll("]", "") + "\n");
                    }
                    group_index++;
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeOutlierToFile(List<List<Tuple>> domain_outlier, String filename) throws IOException {
        String outFile = baseURL + filename;
        File file = new File(outFile);
        FileWriter fw = null;
        BufferedWriter writer = null;
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
            writer = new BufferedWriter(fw);


            int size = 0;
            for (int i = 0; i < domain_outlier.size(); i++) {
                List<Tuple> tuples = domain_outlier.get(i);
                size += tuples.size();
                if (tuples.size() != 0) {
                    writer.write("domain" + i + "\n");
                }
                for (int j = 0; j < tuples.size(); j++) {
                    Tuple t = tuples.get(j);
                    writer.write(t.tupleID + "\n");
                    writer.write(Arrays.toString(t.getAttributeIndex())
                            .replaceAll("\\[", "")
                            .replaceAll("]", "") + "\n");
                    writer.write(Arrays.toString(t.getContext())
                            .replaceAll("\\[", "")
                            .replaceAll("]", "") + "\n");
                }
            }
            System.out.println("\noutlier tuple.size=" + size + "\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HashMap<Integer, String[]> readDataSet(String filename) {
        FileReader reader;
        String fileURL = filename;
        HashMap<Integer, String[]> dataset = new HashMap<>();
        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            int i = 0;
            while ((str = br.readLine()) != null) {
                if (i == 0) {
                    i++;
                    continue;
                }
                int tupleID = Integer.parseInt(str.substring(0, str.indexOf(",")));
                dataset.put(tupleID, str.substring(str.indexOf(",") + 1).split(","));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataset;
    }

    /**
     * 从文件中读取一个rule对应的group
     */
    public static List<HashMap<Integer, Tuple>> readGroupFromFile(String fileURL) {
        List<HashMap<Integer, Tuple>> groups = new ArrayList<>();
        try {
            FileReader reader;
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            int group_index = 0;
            int[] idlist = null;
            String[] content = null;
            int key = 0;
            while ((str = br.readLine()) != null) {
                if (str.contains("group")) {
                    str = str.replaceAll("group", "");
                    group_index = Integer.parseInt(str);
                    groups.add(new HashMap<>());
                } else if (str.contains("ID:")) {
                    String[] ids = str.replaceAll("ID:", "").replaceAll(", ", ",").split(",");
                    idlist = new int[ids.length];
                    for (int i = 0; i < ids.length; i++) {
                        idlist[i] = Integer.parseInt(ids[i]);
                    }
                    groups.get(group_index).get(key).setAttributeIndex(idlist);
                } else if (str.contains("content:")) {
                    content = str.replaceAll("content:", "").replaceAll(", ", ",").split(",");
                    groups.get(group_index).get(key).setContext(content);
                } else {
                    key = Integer.parseInt(str);
                    groups.get(group_index).put(key, new Tuple());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return groups;
    }

    public static List<List<HashMap<Integer, Tuple>>> readOutlierFile(String filename) throws IOException {
        FileReader reader;
        String fileURL = filename;
        List<List<HashMap<Integer, Tuple>>> domain_to_groups = new ArrayList<>();
        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            int domain_index = 0;
            int group_index = 0;
            int[] idlist = null;
            String[] content = null;
            int key = 0;
            while ((str = br.readLine()) != null) {
                if (str.contains("domain")) {
                    str = str.replaceAll("domain", "");
                    domain_index = Integer.parseInt(str);
                    domain_to_groups.add(new ArrayList<>());
                } else if (str.contains("group")) {
                    str = str.replaceAll("group", "");
                    group_index = Integer.parseInt(str);
                    while (domain_to_groups.size() <= domain_index) {
                        domain_to_groups.add(new ArrayList<>());
                    }
                    domain_to_groups.get(domain_index).add(new HashMap<>());
                } else if (str.contains("ID:")) {
                    String[] ids = str.replaceAll("ID:", "").replaceAll(", ", ",").split(",");
                    idlist = new int[ids.length];
                    for (int i = 0; i < ids.length; i++) {
                        idlist[i] = Integer.parseInt(ids[i]);
                    }
                    domain_to_groups.get(domain_index).get(group_index).get(key).setAttributeIndex(idlist);
                } else if (str.contains("content:")) {
                    content = str.replaceAll("content:", "").replaceAll(", ", ",").split(",");
                    domain_to_groups.get(domain_index).get(group_index).get(key).setContext(content);
                } else {
                    key = Integer.parseInt(str);
                    Tuple t = new Tuple();
                    t.tupleID = key;
                    domain_to_groups.get(domain_index).get(group_index).put(key, t);
                }
            }
            br.close();
//            OutlierAnalyse.writeGroupsToFile(domain_to_groups,"000.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return domain_to_groups;
    }

    /*public static void main(String args, Log log, int threshold) {
        if (args.contains("0.05_0.05")) {
            conflictAnalysis(args, log, threshold);
        } else {
            outlierAnalysis(args,log, threshold);
//            conflictAnalysis(args, log, threshold);
        }
    }*/

    public static void main(String[] agrs) {
        String crowd_groupURL = Main.baseURL + "syn-car/crowdsourcing_data/3q/0.02_0.05/groups_reorganize.txt";
        String ground_groupURL = Main.baseURL + "syn-car/crowdsourcing_data/3q/0.02_0.05/ground_group.txt";
        String outURL = Main.baseURL + "syn-car/crowdsourcing_data/3q/0.02_0.05/CrowdGroupAnalyse.txt";
        crowdGroupAnalysis(crowd_groupURL, ground_groupURL, new Log(outURL));
        //用模拟精度生成crowd的group
        /*String dirtyDataURL = Main.baseURL + "syn-car/0.02_0.05/crowdsourcing_data/1q/testData.csv";
        String groundTruthURL = Main.baseURL + "syn-car/0.02_0.05/ground_truth-hasID.csv";
        String ruleURL = Main.baseURL + "syn-car/0.02_0.05/rules.txt";
        double precision = 0.95;
        generateGroup(precision, dirtyDataURL, groundTruthURL, ruleURL);*/
    }

    public static void conflictAnalysis(HashMap<Integer, String[]> dataSet_v1, ArrayList<Integer> conflicts, String data_args, Log log) {
        int true_conflict_num = conflicts.size(); //true conflict tuple number
        int total_error_num = 0;

        int cleaned_conflict_num = 0;
        HashMap<Integer, String[]> dataset_ground_truth = readDataSet(data_args + "/ground_sampleData.csv"); //read ground truth file
        HashMap<Integer, String[]> dataset_dirty = readDataSet(data_args + "/RDBSCleaner_cleaned.txt"); //read ground truth file

        for (Integer tupleID : conflicts) {
            String tuple_ground_truth = Arrays.toString(dataset_ground_truth.get(tupleID));
            String tuple_dirty = Arrays.toString(dataset_dirty.get(tupleID));
            if (tuple_dirty.equals(tuple_ground_truth)) {
                cleaned_conflict_num++;
            }
        }

        Iterator<Map.Entry<Integer, String[]>> iter = dataSet_v1.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, String[]> entry = iter.next();
            Integer tupleID = entry.getKey();
            String tuple_v1 = Arrays.toString(entry.getValue());
            String tuple_ground_truth = Arrays.toString(dataset_ground_truth.get(tupleID));
            if (!tuple_v1.equals(tuple_ground_truth)) {
                total_error_num++;
            }
        }

        double precision = (double) cleaned_conflict_num / true_conflict_num;
        double recall = (double) cleaned_conflict_num / total_error_num;

        log.write("total error tuple number after clean_step1 = " + total_error_num + "\n");
        log.write("\nconflict cleaned_conflict_num = " + cleaned_conflict_num + "\n");
        log.write("conflict true_conflict_num = " + true_conflict_num + "\n");

        System.out.println("conflict resolution precision = " + precision);
        log.write("\nconflict resolution precision = " + precision + "\n");

        System.out.println("conflict resolution recall = " + recall);
        log.write("\nconflict resolution recall = " + recall + "\n");
    }


    public static boolean ifSameGroup(HashMap<Integer, Tuple> crowd_group, HashMap<Integer, Tuple> ground_group) {
        Iterator<Map.Entry<Integer, Tuple>> crowd_iter = crowd_group.entrySet().iterator();
        int size = crowd_group.size();
        int count = 0;
        while (crowd_iter.hasNext()) {
            Map.Entry<Integer, Tuple> crowd_entry = crowd_iter.next();
            int tupleID = crowd_entry.getKey();
            if (ground_group.containsKey(tupleID)) {
                count++;
            }
        }
        if (count == size) {
            return true;
        } else return false;
    }

    /**
     * 评估根据Crowd产生的group的准确度
     * 包括precision, recall, and F1-score
     */
    public static void crowdGroupAnalysis(String crowdGroupURL, String groundGroupURL, Log log) {
        int num_ground = 0;
        int num_crowd = 0;
        int inter = 0;//交集
        try {
            List<List<HashMap<Integer, Tuple>>> crowd_domain_groups = readOutlierFile(crowdGroupURL);
            List<List<HashMap<Integer, Tuple>>> ground_domain_groups = readOutlierFile(groundGroupURL);

            //最外面一层list代表rule的个数。
            for (int i = 0; i < crowd_domain_groups.size(); i++) {
                List<HashMap<Integer, Tuple>> crowd_groups = crowd_domain_groups.get(i);
                num_crowd += crowd_groups.size();
                List<HashMap<Integer, Tuple>> ground_groups = ground_domain_groups.get(i);
                num_ground += ground_groups.size();

                for (int j = 0; j < crowd_groups.size(); j++) {
                    HashMap<Integer, Tuple> crowd_group = crowd_groups.get(j);
                    int crowd_group_size = crowd_group.size();
                    for (HashMap<Integer, Tuple> ground_group : ground_groups) {
                        int ground_group_size = ground_group.size();
                        if (crowd_group_size != ground_group_size) continue;
                        if (ifSameGroup(crowd_group, ground_group)) {
                            inter++;
                        }
                    }
                }
            }
            double precision = (double)inter / num_crowd;
            double recall = (double)inter / num_ground;
            double f1_score = (double) 2*precision*recall / (precision+recall);

            System.out.println("Crowd precision = " + precision);
            System.out.println("Crowd recall = " + recall);
            System.out.println("Crowd F1-score = " + f1_score);
            log.write("\n===Group Analysis by Crowsourcing===");
            log.write("Crowd precision = " + precision);
            log.write("Crowd recall = " + recall);
            log.write("Crowd F1-score = " + f1_score+"\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void outlierAnalysis(String data_args, Log log, int threshold) {
        try {
            List<List<HashMap<Integer, Tuple>>> domain_to_groups = readOutlierFile(data_args + "/total_group_info_" + threshold + ".txt");
            List<List<HashMap<Integer, Tuple>>> domain_to_outlier_groups = readOutlierFile(data_args + "/outlier_analysis_" + threshold + ".txt");
            List<List<HashMap<Integer, Tuple>>> domain_to_outlier_groups_after = readOutlierFile(data_args + "/total_group_info_after_merge_" + threshold + ".txt");
            List<List<HashMap<Integer, Tuple>>> true_domain_to_outlier_groups = new ArrayList<>();
            HashMap<Integer, String[]> dataset = readDataSet(data_args + "/ground_sampleData.csv"); //read ground truth file

            int trueSize = 0; //true outlier group size
            int trueDetectSize = 0;//true detected group size
            int trueAddSize = 0;//outlier groups are truely added to correct group
            //calculate the detected outlier group size
            int detect_size = 0;
            for (List<HashMap<Integer, Tuple>> list : domain_to_outlier_groups) {
                detect_size += list.size();
            }


            //detect
            for (int i = 0; i < domain_to_groups.size(); i++) {
                List<HashMap<Integer, Tuple>> groups = domain_to_groups.get(i);
                List<HashMap<Integer, Tuple>> outlier_groups = domain_to_outlier_groups.get(i);
                List<HashMap<Integer, Tuple>> true_outlier_groups = new ArrayList<>();

                List<HashSet<String>> outlier_groups_hashset = new ArrayList<>();
                for (HashMap<Integer, Tuple> og : outlier_groups) {
                    Iterator<Map.Entry<Integer, Tuple>> iter = og.entrySet().iterator();
                    HashSet<String> set = new HashSet<>();
                    while (iter.hasNext()) {
                        Tuple t = iter.next().getValue();
                        String str_content = Arrays.toString(t.getContext());
                        set.add(str_content);
                    }
                    outlier_groups_hashset.add(set);
                }

                for (HashMap<Integer, Tuple> group : groups) {
                    Iterator<Map.Entry<Integer, Tuple>> iter = group.entrySet().iterator();

                    HashSet<String> hashSet_dirty = new HashSet<>();
                    HashSet<String> hashSet_groundtruth = new HashSet<>();

                    //以下为同一个group中的tuples
                    while (iter.hasNext()) {
                        Map.Entry<Integer, Tuple> entry = iter.next();
                        Tuple t = entry.getValue();
                        int tupleID = entry.getKey();
                        int[] attr_idlist = t.getAttributeIndex();
                        String[] content = t.getContext();
                        String[] tuple = dataset.get(tupleID);
                        String[] ground_content = new String[attr_idlist.length];
                        for (int j = 0; j < attr_idlist.length; j++) {
                            ground_content[j] = tuple[attr_idlist[j]];
                        }
                        String str_ground_content = Arrays.toString(ground_content);

                        String str_content = Arrays.toString(content);
                        hashSet_dirty.add(str_content);
                        hashSet_groundtruth.add(str_ground_content);
                    }

                    Iterator<String> iter2 = hashSet_dirty.iterator();
                    boolean flag = true;
                    String tuple = "";
                    while (iter2.hasNext()) {
                        tuple = iter2.next();

                        if (hashSet_groundtruth.contains(tuple)) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {

                        HashMap<Integer, Tuple> true_outlier_group = new HashMap<>();
                        Iterator<Map.Entry<Integer, Tuple>> true_iter = group.entrySet().iterator();
                        while (true_iter.hasNext()) {
                            Map.Entry<Integer, Tuple> entry = true_iter.next();
                            Integer tupleID = entry.getKey();
                            Tuple t = entry.getValue();
                            true_outlier_group.put(tupleID, t);
                        }
                        true_outlier_groups.add(true_outlier_group);

                        trueSize++;
                        Iterator<String> iter3 = hashSet_dirty.iterator();
                        boolean flg = false;
                        while (iter3.hasNext()) {
                            String tmp_tuple = iter3.next();
                            for (HashSet<String> set : outlier_groups_hashset) {
                                if (set.contains(tmp_tuple)) {
                                    trueDetectSize++;
                                    flg = true;
                                    break;
                                }
                            }
                            if (flg) break;
                        }
                    }
                }
                true_domain_to_outlier_groups.add(true_outlier_groups);
            }

            //add
            for (int k = 0; k < true_domain_to_outlier_groups.size(); k++) {
                if (true_domain_to_outlier_groups.get(k).size() != 0) {
                    List<HashMap<Integer, Tuple>> groups = true_domain_to_outlier_groups.get(k);
                    for (HashMap<Integer, Tuple> group : groups) {
                        Iterator<Map.Entry<Integer, Tuple>> iter = group.entrySet().iterator();

                        if (iter.hasNext()) {
                            Map.Entry<Integer, Tuple> entry = iter.next();
                            Tuple t = entry.getValue();
                            int tupleID = entry.getKey();
                            int[] attr_idlist = t.getAttributeIndex();
                            String[] content = t.getContext();

                            String[] tuple = dataset.get(tupleID);
                            String[] ground_content = new String[attr_idlist.length];
                            for (int j = 0; j < attr_idlist.length; j++) {
                                ground_content[j] = tuple[attr_idlist[j]];
                            }
                            String str_ground_content = Arrays.toString(ground_content);


                            List<HashMap<Integer, Tuple>> after_merge_groups = domain_to_outlier_groups_after.get(k);
                            for (HashMap<Integer, Tuple> grp : after_merge_groups) {
                                Iterator<Map.Entry<Integer, Tuple>> itr = grp.entrySet().iterator();
                                boolean flag = false;//找到outlier group被添加到的那个group
                                boolean true_added_flag = false;//添加到的group确实是正确的group
                                while (itr.hasNext()) {
                                    Map.Entry<Integer, Tuple> etry = itr.next();
                                    int m_tupleID = etry.getKey();
                                    Tuple m_t = etry.getValue();
                                    String[] m_content = m_t.getContext();
                                    if (m_tupleID == tupleID && Arrays.toString(m_content).equals(Arrays.toString(content))) {
                                        flag = true;
                                        break;
                                    }
                                }
                                if (flag) {
                                    Iterator<Map.Entry<Integer, Tuple>> itr2 = grp.entrySet().iterator();
                                    while (itr2.hasNext()) {
                                        Map.Entry<Integer, Tuple> etry = itr2.next();
                                        Tuple m_t = etry.getValue();
                                        String[] m_content = m_t.getContext();
                                        String str_m_content = Arrays.toString(m_content);
                                        if (str_ground_content.equals(str_m_content)) {
                                            true_added_flag = true;
                                            trueAddSize++;
                                            break;
                                        }
                                    }
                                }
                                if (true_added_flag) {
                                    break;
                                }

                            }

                        }
                    }
                }
            }

            double detect_precision = (double) trueDetectSize / detect_size;
            double detect_recall = (double) trueDetectSize / trueSize;

            double add_precision = (double) trueAddSize / detect_size;
            double add_recall = (double) trueAddSize / trueSize;

            log.write("\ntrueDetectSize = " + trueDetectSize + "\n");
            log.write("detect_size = " + detect_size + "\n");
            log.write("trueAddSize = " + detect_size + "\n");

            System.out.println("outlier detect precision = " + detect_precision);
            log.write("\noutlier detect precision = " + detect_precision + "\n");

            System.out.println("outlier detect recall = " + detect_recall);
            log.write("\noutlier detect recall = " + detect_recall + "\n");

            System.out.println("outlier add precision = " + add_precision);
            log.write("\noutlier add precision = " + add_precision + "\n");

            System.out.println("outlier add recall = " + add_recall);
            log.write("\noutlier add recall = " + add_recall + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
