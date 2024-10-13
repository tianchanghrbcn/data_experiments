package data;

import java.io.*;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.*;

import main.Main;
import tuffy.util.Config;
import org.postgresql.Driver;

import static main.Main.baseURL;

public class Rule {

    String predicate = null;
    String value = null;
    public String[] header = null;
    public ArrayList<Tuple> tupleList = new ArrayList<Tuple>();


    public Rule() {
    }

    public ArrayList<Integer> findIgnoredIDs(List<Tuple> rules, String[] header) {
        ArrayList<Integer> ignoredIDs = new ArrayList<Integer>();
        HashMap<String, Integer> map = new HashMap<>(header.length);

        for (Tuple rule : rules) {
            int i = 0;
            while (i < rule.reason.length) {
                map.put(rule.reason[i++], 1);
            }
            int j = 0;
            while (j < rule.result.length) {
                map.put(rule.result[j++], 1);
            }
        }

        for (int i = 0; i < header.length; i++) {
            String predicate = header[i];
            Integer result = map.get(predicate);
            if (null == result) {//find ignored tuple predicate
                ignoredIDs.add(i);
            }
        }

        // 打印所有被ignore的ID
        System.out.print("Ignored Tuple ID:");
        for (int i : ignoredIDs) {
            System.out.print(i + " ");
        }
        System.out.println();
        return ignoredIDs;
    }

    public static String[] getHead(String DBurl, String splitString) {
        String[] header = null;
        try {
            FileReader reader;
            reader = new FileReader(DBurl);
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            if ((line = br.readLine()) != null) {
                header = line.substring(line.indexOf(",") + 1).split(splitString);
            } else {
                System.err.println("Error: No header!");
            }
            br.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return header;
    }

    //
    public ArrayList<Integer> findIgnoredTuples(List<Tuple> rules) {
        ArrayList<Integer> ignoredIDs = new ArrayList<Integer>();
        HashMap<String, Integer> map = new HashMap<String, Integer>(header.length);

        for (Tuple rule : rules) {
            int i = 0;
            while (i < rule.reason.length) {
                map.put(rule.reason[i++], 1);
            }
            int j = 0;
            while (j < rule.result.length) {
                map.put(rule.result[j++], 1);
            }
        }

        for (int i = 0; i < header.length; i++) {
            String predicate = header[i];
            Integer result = map.get(predicate);
            if (null == result) {//find ignored tuple predicate
                ignoredIDs.add(i);
            }
        }

        // 打印所有被ignore的ID
        System.out.print("Ignored Tuple ID:");
        for (int i : ignoredIDs) {
            System.out.println(i + " ");
        }
        return ignoredIDs;
    }


    /**
     * 閺夆晜鏌ㄥú鏍础閺囨岸鍤嬮悘鐐靛仦閿熺獤鍐╁?抽柣銊ュ婢у秹宕烽妸銉ョ仚闁汇劌瀚槐顏堝矗閿燂拷
     *
     * @return Attribute Index
     */
    public static int findAttributeIndex(String name, String[] header) {
        int index = 0;
        for (; index < header.length; index++) {
            if (header[index].equals(name))
                break;
        }
        return index;
    }

    /**
     *
     * @return Attribute Indexes
     */

    public static int[] findAttributeIndex(String[] name, String[] header) {
        boolean[] flag = new boolean[header.length];
        int[] attributeIDs = new int[name.length];
        for (int i = 0; i < flag.length; i++) {
            flag[i] = false;
        }
        for (int i = 0; i < name.length; i++) {
            for (int index = 0; index < header.length; index++) {
                if (flag[index] == false && header[index].equals(name[i])) {
                    attributeIDs[i] = index;
                    flag[index] = true;
                    break;
                }
            }
        }
        return attributeIDs;
    }


    /**
     * @param tupleIDs
     * @param reasonIDs
     */

    public static int[] findResultIDs(int[] tupleIDs, int[] reasonIDs) {
        int[] resultIDs = new int[tupleIDs.length - reasonIDs.length];
        int index = 0;

        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < tupleIDs.length; ++i) {
            boolean bContained = false;
            for (int j = 0; j < reasonIDs.length; ++j) {
                if (tupleIDs[i] == reasonIDs[j]) {
                    bContained = true;
                    break;
                }
            }

            if (!bContained) {
                resultIDs[index++] = tupleIDs[i];
                //list.add(tupleIDs[i]);
            }
        }
//        int res[] = new int[list.size()];
//        for(int i = 0; i < list.size(); ++i)
//            res[i] = list.get(i);

        return resultIDs;
    }


    public static void partitionData(String dataFile, int partitionNum, String dataName, ArrayList<String> rules) {
        try {
            BufferedWriter dataBw[] = new BufferedWriter[partitionNum];
            BufferedWriter rulesBw[] = new BufferedWriter[partitionNum];
            ArrayList<String> datalist = new ArrayList<>();

            //read source dataFile
            FileReader reader = new FileReader(dataFile);
            BufferedReader br = new BufferedReader(reader);
            String line;
            String header = br.readLine();//read header
            header = header.substring(header.indexOf(",") + 1);
            while ((line = br.readLine()) != null && line.length() != 0) {
                int index = line.indexOf(",");
                String tuple = line.substring(index + 1);
                String[] data = tuple.split(",");
                for (int i = 0; i < data.length; i++) {
                    if (data[i] == null || data[i].isEmpty()) {
                        data[i] = "null";
                    }
                }
                String tuple_concat = String.join(",", data);
                datalist.add(tuple_concat);
            }
            br.close();

            for (int i = 0; i < partitionNum; i++) {
//                System.out.println("  Begin partition <" + i + "> ...");
                File rulesWriteFile = new File(baseURL + "/"  + dataName + "/rules-new" + i + ".txt");
                File dataWriteFile = new File(baseURL + "/"  + dataName + "/data-new" + i + ".txt");
                if (!rulesWriteFile.exists()) {
                    rulesWriteFile.createNewFile();
                }
                if (!dataWriteFile.exists()) {
                    dataWriteFile.createNewFile();
                }

                FileWriter fw1 = new FileWriter(rulesWriteFile);
                rulesBw[i] = new BufferedWriter(fw1);
                FileWriter fw2 = new FileWriter(dataWriteFile);
                dataBw[i] = new BufferedWriter(fw2);
//                dataBw[i].write("HospitalName,City,State,PhoneNumber\n");
                dataBw[i].write(header + "\n");
            }

            //partition dataList
            int size = datalist.size();
//            System.out.println("size="+size);
            int part_i = 0;
            int number = size / partitionNum;//每份的数据量
            ArrayList<ArrayList<String>> part_dataList = new ArrayList<>();
            for (int i = 0; i < partitionNum; i++) {
                part_dataList.add(new ArrayList<>(number));
            }

            for (int i = 0; i < size; i++) {
//                System.out.println(datalist.get(i));
                dataBw[part_i].write(datalist.get(i) + "\n");
                part_dataList.get(part_i).add(datalist.get(i));
                if (part_i < partitionNum - 1 && i == number * (part_i + 1) - 1) {
                    dataBw[part_i].close();
//                    System.out.println("  Writing to rules-new" + part_i + ".txt");
                    groundRules(header, dataFile, part_dataList.get(part_i), rules, part_i);
                    part_i++;
                } else if (part_i == partitionNum - 1 && i == size - 1) {
                    dataBw[part_i].close();
                    groundRules(header, dataFile, part_dataList.get(part_i), rules, part_i);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void groundRules(String head, String filename, ArrayList<String> dataList, ArrayList<String> rules, int part_i) {
        HashMap<String, GroundRule> map = new HashMap<>();
        String[] header = head.split(",");
        for (int i = 0; i < dataList.size(); i++) {
            String tmp = dataList.get(i);
            String[] tuple = tmp.split(",");
            for (int k = 0; k < rules.size(); k++) {
                String currentRule = rules.get(k);
                for (int j = 0; j < header.length; j++) {
                    try{
                        if (currentRule.indexOf(header[j]) != -1) {
                            currentRule = currentRule.replaceAll("value" + header[j], "\"" + tuple[j] + "\"");
                        }
                    }
                    catch (ArrayIndexOutOfBoundsException e){
                        System.out.print(i);
                        System.out.print(k);
                        System.out.println(j);
                    }
                }
                if (map.get(currentRule) == null) {
                    GroundRule gr = new GroundRule("", 1);
                    map.put(currentRule, gr);
                } else {
                    map.get(currentRule).number += 1;
                    map.put(currentRule, map.get(currentRule));
                }
            }
        }
        //grounding rules and write to file '.gr'
        try {
            String writeFile = filename.replaceAll("trainData.csv", "rules-new" + part_i + "\\.txt");
            File writefile = new File(writeFile);
            if (!writefile.exists()) {
                writefile.createNewFile();
            }
            FileWriter fw = new FileWriter(writefile);
            BufferedWriter bw = new BufferedWriter(fw);

            /*bw.write("HospitalName(valueHospitalName)\n" +
                    "City(valueCity)\n" +
                    "State(valueState)\n" +
                    "PhoneNumber(valuePhoneNumber)\n\n");*/
            // for (int i = 0; i<header.length(); i++){
            //     bw.write(header.get(i) + '\n');
            // }
            for (int i = 0; i<header.length; i++){
                bw.write(header[i] + "(value" + header[i] + ")" + '\n');
            }

            bw.write('\n');
            Iterator<Map.Entry<String, GroundRule>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, GroundRule> entry = iter.next();
                String key = entry.getKey();
                GroundRule value = entry.getValue();
                double weight = (double) value.number * 100 / map.size();
                if (weight >= 5) weight = 5;
                map.get(key).weight = String.format("%.2f", weight);
                String result = map.get(key).weight + "\t" + key + "\n";
                bw.write(result);
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void partitionMLN(String file, ArrayList<String> rules, int partitionNum, String dataName) {
        System.out.println(">>> grounding Rules into '.gr' File");
        String fileURL = groundRules(file, rules);
        /*
        HashMap<String, String> map = combineRulesFile(clean_fileURL, dirty_fileURL, dataName);
        String newcleanFile = cleanfile.replaceAll(".csv", "-hasID.csv");
        Main.setLineID(cleanfile, newcleanFile);
        */
        System.out.println(">>> grounding finished!");
        HashMap<String, String> map = readFileToHash(fileURL);
        ArrayList<String> tuples = readFileNoHeader(file);//list里包含ID
        Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator();

        try {
            // 分布式情况下n个节点
            BufferedWriter rulesBw[] = new BufferedWriter[partitionNum];
            BufferedWriter dataBw[] = new BufferedWriter[partitionNum];
            HashMap<Integer, String> dataset[] = new HashMap[partitionNum];
            //初始化rule-new.txt和data-new.txt
            for (int i = 0; i < partitionNum; i++) {
                System.out.println(">>> Begin partition <" + i + ">...");
                File rulesWriteFile = new File("/home/gecongcong/experiment/dataSet/" + dataName + "/rules-new" + i + ".txt");
                File dataWriteFile = new File("/home/gecongcong/experiment/dataSet/" + dataName + "/data-new" + i + ".txt");
                if (!rulesWriteFile.exists()) {
                    rulesWriteFile.createNewFile();
                }
                if (!dataWriteFile.exists()) {
                    dataWriteFile.createNewFile();
                }
                dataset[i] = new HashMap<>();

                FileWriter fw1 = new FileWriter(rulesWriteFile);
                rulesBw[i] = new BufferedWriter(fw1);
                FileWriter fw2 = new FileWriter(dataWriteFile);
                dataBw[i] = new BufferedWriter(fw2);
                // ID,model,make,type,year,condition,wheelDrive,doors,engine
                rulesBw[i].write("ID(valueID)\n" +
                        "model(valuemodel)\n" +
                        "make(valuemake)\n" +
                        "type(valuetype)\n" +
                        "year(valueyear)\n" +
                        "condition(valuecondition)\n" +
                        "wheelDrive(valuewheelDrive)\n" +
                        "doors(valuedoors)\n" +
                        "engine(valueengine)\n\n");
                dataBw[i].write("ID,model,make,type,year,condition,wheelDrive,doors,engine\n");
            }

            //写入rules-new.txt
            int i = 0;
            int number_i = 0;
            int size = map.size();
            int number = size / partitionNum;//每份的数据量

            while (iter.hasNext()) {
                if (number_i == number) {
                    System.out.println(">>> write to rules-new" + i + ".txt");
                    number_i = 0;
                    if (i != partitionNum - 1) {
                        i++;
                    }
                }
                Map.Entry<String, String> entry = iter.next();
                String mln = entry.getKey();
                String prob = entry.getValue();
                String rule = mln.replaceAll("\\)v", ") v ");
                rulesBw[i].write(prob + "\t" + rule + "\n");
                ArrayList<String> result = findMatchedTuple(rule, tuples);
//                System.out.println("222");
                for (String tuple : result) {
                    int index = tuple.indexOf(",");
                    dataset[i].put(Integer.parseInt(tuple.substring(0, index)), tuple.substring(index + 1));
                }
                number_i++;
            }

            System.out.println("333");

            for (int k = 0; k < partitionNum; k++) {
                //根据ID排序
                Collections.sort(new ArrayList<>(dataset[k].entrySet()), new Comparator<Map.Entry<Integer, String>>() {
                    @Override
                    public int compare(Map.Entry<Integer, String> o1, Map.Entry<Integer, String> o2) {
                        return o1.getKey().compareTo(o2.getKey());
                    }
                });
                Iterator<Map.Entry<Integer, String>> iterator = dataset[k].entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, String> entry = iterator.next();
                    dataBw[k].write(entry.getValue() + "\n");
                }
                dataBw[k].close();
            }

            // 最后要关闭文件流
            for (int k = 0; k < partitionNum; k++) {
//                dataBw[k].close();
                rulesBw[k].close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Using clean data and dirty data to generate MLN,
     * partition MLN and clean data
     */
    public static void partitionMLN(String dirtyfile, String cleanfile, ArrayList<String> rules, int partitionNum, String dataName) {
        String dirty_fileURL = groundRules(dirtyfile, rules);
        String clean_fileURL = groundRules(cleanfile, rules);
        HashMap<String, String> map = combineRulesFile(clean_fileURL, dirty_fileURL, dataName);
        String newcleanFile = cleanfile.replaceAll(".csv", "-hasID.csv");
        Main.setLineID(cleanfile, newcleanFile,",");
        ArrayList<String> tuples = readFileNoHeader(newcleanFile);//list里包含ID
        Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator();


        try {
            // 分布式情况下n个节点
            BufferedWriter rulesBw[] = new BufferedWriter[partitionNum];
            BufferedWriter dataBw[] = new BufferedWriter[partitionNum];
            HashMap<Integer, String> dataset[] = new HashMap[partitionNum];
            //初始化rule-new.txt和data-new.txt
            for (int i = 0; i < partitionNum; i++) {
                File rulesWriteFile = new File("/home/gecongcong/experiment/dataSet/" + dataName + "/rules-new" + i + ".txt");
                File dataWriteFile = new File("/home/gecongcong/experiment/dataSet/" + dataName + "/data-new" + i + ".txt");
                if (!rulesWriteFile.exists()) {
                    rulesWriteFile.createNewFile();
                }
                if (!dataWriteFile.exists()) {
                    dataWriteFile.createNewFile();
                }
                dataset[i] = new HashMap<>();

                FileWriter fw1 = new FileWriter(rulesWriteFile);
                rulesBw[i] = new BufferedWriter(fw1);
                FileWriter fw2 = new FileWriter(dataWriteFile);
                dataBw[i] = new BufferedWriter(fw2);
                rulesBw[i].write("model(valuemodel)\n" +
                        "make(valuemake)\n" +
                        "type(valuetype)\n" +
                        "year(valueyear)\n" +
                        "condition(valuecondition)\n" +
                        "wheelDrive(valuewheelDrive)\n" +
                        "doors(valuedoors)\n" +
                        "engine(valueengine)\n\n");
                dataBw[i].write("model,make,type,year,condition,wheelDrive,doors,engine\n");
            }

            //写入rules-new.txt
            int i = 0;
            int number_i = 0;
            int size = map.size();
            int number = size / partitionNum;//每份的数据量

            while (iter.hasNext()) {
                if (number_i == number) {
                    number_i = 0;
                    if (i != partitionNum - 1)
                        i++;
                }
                Map.Entry<String, String> entry = iter.next();
                String mln = entry.getKey();
                String prob = entry.getValue();
                String rule = mln.replaceAll("\\)v", ") v ");
                rulesBw[i].write(prob + "\t" + rule + "\n");
                ArrayList<String> result = findMatchedTuple(rule, tuples);
                for (String tuple : result) {
                    int index = tuple.indexOf(",");
                    dataset[i].put(Integer.parseInt(tuple.substring(0, index)), tuple.substring(index + 1));
                }
                number_i++;
            }

            for (int k = 0; k < partitionNum; k++) {
                //根据ID排序
                Collections.sort(new ArrayList<>(dataset[k].entrySet()), new Comparator<Map.Entry<Integer, String>>() {
                    @Override
                    public int compare(Map.Entry<Integer, String> o1, Map.Entry<Integer, String> o2) {
                        return o1.getKey().compareTo(o2.getKey());
                    }
                });
                Iterator<Map.Entry<Integer, String>> iterator = dataset[k].entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, String> entry = iterator.next();
                    dataBw[k].write(entry.getValue() + "\n");
                }
            }

            // 最后要关闭文件流
            for (int k = 0; k < partitionNum; k++) {
                dataBw[k].close();
                rulesBw[k].close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Find tupleList that matches rule R in specific Tuples.
     */
    public static ArrayList<String> findMatchedTuple(String rule, ArrayList<String> tuples) {
        ArrayList<String> result = new ArrayList<>();
        String[] ruleValues = rule.split(" v ");
        //提取value in rule R
        for (int i = 0; i < ruleValues.length; i++) {
            /*ruleValues[i] = ruleValues[i].replaceAll("\"", "")
                    .replaceAll(".*\\(", "")
                    .replaceAll("\\)", "");*/
            ruleValues[i] = ruleValues[i].replaceAll(".*\\(\"", "")
                    .replaceAll("\"\\).*", "");
        }
        Arrays.sort(ruleValues);

        //Select tuples including rule values, then save them.
        for (int i = 0; i < tuples.size(); i++) {//i=0: is header, so escape
            String str = tuples.get(i);
            int index = str.indexOf(",");
            String id = str.substring(0, index);
            String[] tuple = str.substring(index + 1).split(",");
            Arrays.sort(tuple);

            //ifContains
            int count = 0;
            int key = 0;
            for (int k = 0; k < ruleValues.length; k++) {
                while (key < tuple.length) {
                    if (ruleValues[k].equals(tuple[key])) {
                        count++;
                        key++;
                        break;
                    } else {
                        key++;
                    }
                }
            }
            if (count == ruleValues.length) {//contains
                result.add(str);
            }

        }

        return result;
    }

    /**
     * combine rules from two files
     */
    public static HashMap<String, String> combineRulesFile(String cleanfile, String dirtyfile, String dataName) {
        HashMap<String, String> clean_map = readFileToHash(cleanfile);
        ArrayList<String> dirty_list = readFile(dirtyfile);

        Iterator<Map.Entry<String, String>> clean_iter = clean_map.entrySet().iterator();
        while (clean_iter.hasNext()) {
            Map.Entry<String, String> clean_entry = clean_iter.next();
            String clean_mln = clean_entry.getKey();
//            String clean_prob = clean_entry.getValue();
            for (int i = 0; i < dirty_list.size(); i++) {
                String rule = dirty_list.get(i);
                String dirty_mln = rule.substring(4).replaceAll(" ", "");
                if (clean_mln.equals(dirty_mln)) {
                    dirty_list.remove(rule);
                }
            }
        }
        for (int i = 0; i < dirty_list.size(); i++) {
            String rule = dirty_list.get(i);
            String dirty_mln = rule.substring(5).replaceAll(" ", "");
            String dirty_prob = rule.substring(0, 4);
            clean_map.put(dirty_mln, dirty_prob);
        }

        //Write combined rules to a new File
        Iterator<Map.Entry<String, String>> iter = clean_map.entrySet().iterator();
        try {
            String writeFile = "/home/gecongcong/experiment/dataSet/" + dataName + "/groundRules.txt";
            File writefile = new File(writeFile);
            FileWriter fw = new FileWriter(writefile);
            BufferedWriter bw = new BufferedWriter(fw);
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = iter.next();
                String mln = entry.getKey();
                String prob = entry.getValue();
                bw.write(prob + "\t" + mln.replaceAll("\\)v", ") v "));
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clean_map;
    }

    /**
     * 生成对应数据集的grounding rules
     */
    public static String groundRules(String filename, ArrayList<String> rules) {
        HashMap<String, GroundRule> map = new HashMap<>();
        String writeFile = filename.replaceAll("\\.csv", "\\.gr");
        try {
            File file = new File(filename);//读文件
            File writefile = new File(writeFile);
            if (!writefile.exists()) {
                writefile.createNewFile();
            }
            FileWriter fw = new FileWriter(writefile);
            BufferedWriter bw = new BufferedWriter(fw);
            BufferedReader br = null;
            br = new BufferedReader(new FileReader(file));
            String tmp = null;
            String[] header = null;
            int i = 0;
            // 一次读入一行，直到读入null为文件结束
            while ((tmp = br.readLine()) != null) {
                if (i == 0) {
                    i++;
                    header = tmp.split(",");
                    continue;
                } else {
                    String[] tuple = tmp.split(",");
                    for (int k = 0; k < rules.size(); k++) {
                        String currentRule = rules.get(k);
                        for (int j = 0; j < header.length; j++) {
                            if (currentRule.indexOf(header[j]) != -1) {
                                currentRule = currentRule.replaceAll("value" + header[j], "\"" + tuple[j] + "\"");
                            }
                        }
                        if (map.get(currentRule) == null) {
                            GroundRule gr = new GroundRule("", 1);
                            map.put(currentRule, gr);
                        } else {
                            map.get(currentRule).number += 1;
                            map.put(currentRule, map.get(currentRule));
                        }
                    }
                }
            }
            Iterator<Map.Entry<String, GroundRule>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, GroundRule> entry = iter.next();
                String key = entry.getKey();
                GroundRule value = entry.getValue();
                double weight = (double) value.number * 100 / map.size();
                if (weight >= 5) weight = 5;
                map.get(key).weight = String.format("%.2f", weight);
                String result = map.get(key).weight + "\t" + key + "\n";
                bw.write(result);
            }
            br.close();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writeFile;
    }

    public static HashMap<String, GroundRule> createMLN(String dirtyfile, String cleanfile, ArrayList<String> rules, int partitionNum, String dataName) {
        //HashMap<String,GroundRule> results = new HashMap<String,GroundRule>();
        HashMap<String, GroundRule> map = new HashMap<>();
        //partitionNum = 3;
        try {
            // 分布式情况下n个节点
            BufferedWriter rulesBw[] = new BufferedWriter[partitionNum];
            BufferedWriter dataBw[] = new BufferedWriter[partitionNum];
            for (int i = 0; i < partitionNum; i++) {
                File rulesWriteFile = new File("/home/gecongcong/experiment/dataSet/" + dataName + "/rules-new" + i + ".txt");
                File dataWriteFile = new File("/home/gecongcong/experiment/dataSet/" + dataName + "/data-new" + i + ".txt");
                if (!rulesWriteFile.exists()) {
                    rulesWriteFile.createNewFile();
                }
                if (!dataWriteFile.exists()) {
                    dataWriteFile.createNewFile();
                }
                FileWriter fw1 = new FileWriter(rulesWriteFile);
                rulesBw[i] = new BufferedWriter(fw1);
                FileWriter fw2 = new FileWriter(dataWriteFile);
                dataBw[i] = new BufferedWriter(fw2);
                rulesBw[i].write("model(valuemodel)\n" +
                        "make(valuemake)\n" +
                        "type(valuetype)\n" +
                        "year(valueyear)\n" +
                        "condition(valuecondition)\n" +
                        "wheelDrive(valuewheelDrive)\n" +
                        "doors(valuedoors)\n" +
                        "engine(valueengine)\n\n");
                dataBw[i].write("model,make,type,year,condition,wheelDrive,doors,engine\n");
            }
            File file = new File(dirtyfile);//读文件

            BufferedReader br = null;
            br = new BufferedReader(new FileReader(file));
            String tmp = null;
            String[] header = null;
            int i = 0;
            // 一次读入一行，直到读入null为文件结束

            ArrayList<String> data = new ArrayList<>();
            // 文件读出的数据
            while ((tmp = br.readLine()) != null) {
                if (i == 0) {
                    header = tmp.replaceAll(" ", "").split(",");
                } else {
                    String[] tuple = tmp.replaceAll(" ", "").split(",");
                    // 一行数据
                    String tupleStr = (i - 1) + "," + tmp + "\n";
                    data.add(tupleStr);
                    // 构造带有索引的数据并放入数组中
                    for (int k = 0; k < rules.size(); k++) {
                        String currentRule = rules.get(k);
                        for (int j = 0; j < header.length; j++) {
                            if (currentRule.indexOf(header[j]) != -1) {
                                currentRule = currentRule.replaceAll("value" + header[j], "\"" + tuple[j] + "\"");
                                //rules.set(k,currentRule);
                            }
                        }

                        if (map.get(currentRule) == null) {
                            GroundRule gr = new GroundRule("", 1);
                            map.put(currentRule, gr);
                        } else {
                            map.get(currentRule).number += 1;
                            map.put(currentRule, map.get(currentRule));
                        }
                        map.get(currentRule).dataList.add(i - 1);
//                        System.out.println(currentRule);
                    }
                }
                i++;
            }
            Iterator<Map.Entry<String, GroundRule>> iter = map.entrySet().iterator();
            HashSet<Integer> dataSet[] = new HashSet[partitionNum];
            for (int j = 0; j < partitionNum; j++) {
                dataSet[j] = new HashSet<>();
            }
            while (iter.hasNext()) {
                int index = (int) (Math.random() * partitionNum);
                Map.Entry<String, GroundRule> entry = iter.next();
                String key = entry.getKey();
                GroundRule value = entry.getValue();
                double weight = (double) value.number * 100 / map.size();
                if (weight >= 5) weight = 5;
                map.get(key).weight = String.format("%.2f", weight);
                String result = map.get(key).weight + "\t" + key + "\n";

                rulesBw[index].write(result);

                for (Integer dataIndex : map.get(key).dataList) {
                    dataSet[index].add(dataIndex);
                }
                //results.put(result, 1);
            }
            br.close();
            for (int j = 0; j < partitionNum; j++) {
                for (Integer dataIndex : dataSet[j]) {
                    dataBw[j].write(data.get(dataIndex));
                }
                dataBw[j].close();
                rulesBw[j].close();
                // 最后要关闭文件流
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public void getHeader(String DBurl, String splitString) {
        try {
            FileReader reader;
            reader = new FileReader(DBurl);
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            if ((line = br.readLine()) != null) {
                header = line.split(splitString);
            } else {
                System.err.println("Error: No header!");
            }
            br.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Tuple> loadRules(String fileURL, String splitString, String[] header){
        System.out.println(">>> Getting Predicates.......");
        FileReader reader;
        String[] reason_predicates = null;
        String[] result_predicates = null;
        List<Tuple> list = new ArrayList<Tuple>();
//        getHeader(DBurl, splitString);

        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String line = null;

            //  String current = "";
            int index = 0;


            // 用来存储出现过的规则
            List<String> store = new ArrayList<String>();

            while ((line = br.readLine()) != null && line.length() != 0) {  //The data has header

                line = line.replaceAll("1\t", "").replaceAll(" ", "");

                String[] line_partiton = line.split("=>");

                String[] reason = line_partiton[0].split(splitString);
                String[] result = line_partiton[1].split(splitString);

                int reason_length = reason.length;
                int result_length = result.length;

                reason_predicates = new String[reason_length];
                result_predicates = new String[result_length];

                for (int i = 0; i < reason_length; i++) {
                    reason_predicates[i] = reason[i].replaceAll("\\(.*\\)", "");
                }
                for (int i = 0; i < result_length; i++) {
                    result_predicates[i] = result[i].replaceAll("\\(.*\\)", "");
                }

                Tuple t = new Tuple();
                t.reason = reason_predicates;
                t.result = result_predicates;

                t.setReasonAttributeIndex(findAttributeIndex(reason_predicates, header));
                t.setResultAttributeIndex(findAttributeIndex(result_predicates, header));

                String[] combine = new String[reason_length + result_length]; //将 reason 和 result 合并

                System.arraycopy(reason_predicates, 0, combine, 0, reason_length);
                System.arraycopy(result_predicates, 0, combine, reason_length, result_length);
                t.setAttributeNames(combine);
                t.setAttributeIndex(findAttributeIndex(combine, header));
                //t.index = index++;
                Arrays.sort(combine);
                if (store.contains(Arrays.toString(combine))) continue;
                store.add(Arrays.toString(combine));
                System.out.println(Arrays.toString(combine));
                list.add(t);
            }
            br.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(">>> Completed!");
        return list;
    }

    /**
     * getting predicates
     *
     * @param fileURL
     * @param splitString
     * @return List<String[]>
     * @throws IOException
     */
    public List<Tuple> loadRules(String fileURL, String splitString) throws IOException {
        System.out.println(">>> Getting Predicates.......");
        FileReader reader;
        String[] reason_predicates = null;
        String[] result_predicates = null;
        List<Tuple> list = new ArrayList<Tuple>();
//        getHeader(DBurl, splitString);

        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String line = null;

            //  String current = "";
            int index = 0;


            // 用来存储出现过的规则
            List<String> store = new ArrayList<String>();

            while ((line = br.readLine()) != null && line.length() != 0) {  //The data has header

                line = line.replaceAll("1\t", "").replaceAll(" ", "");

                String[] line_partiton = line.split("=>");

                String[] reason = line_partiton[0].split(splitString);
                String[] result = line_partiton[1].split(splitString);

                int reason_length = reason.length;
                int result_length = result.length;

                reason_predicates = new String[reason_length];
                result_predicates = new String[result_length];

                for (int i = 0; i < reason_length; i++) {
                    reason_predicates[i] = reason[i].replaceAll("\\(.*\\)", "");
                }
                for (int i = 0; i < result_length; i++) {
                    result_predicates[i] = result[i].replaceAll("\\(.*\\)", "");
                }

                Tuple t = new Tuple();
                t.reason = reason_predicates;
                t.result = result_predicates;

//                t.setReasonAttributeIndex(findAttributeIndex(reason_predicates, header));

                String[] combine = new String[reason_length + result_length]; //将 reason 和 result 合并

                System.arraycopy(reason_predicates, 0, combine, 0, reason_length);
                System.arraycopy(result_predicates, 0, combine, reason_length, result_length);
                t.setAttributeNames(combine);
                //t.index = index++;
                Arrays.sort(combine);
                if (store.contains(Arrays.toString(combine))) continue;
                store.add(Arrays.toString(combine));
                System.out.println(Arrays.toString(combine));
                list.add(t);
            }
            br.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(">>> Completed!");
        return list;
    }

    /*public static HashMap<String, String> readFileNoHeader(String URL) {
        HashMap<String, String> map = new HashMap<>();

        FileReader reader;
        try {
            reader = new FileReader(URL);
            BufferedReader br = new BufferedReader(reader);
            String line;
            br.readLine();
            while ((line = br.readLine()) != null && line.length() != 0) {
                String prob = line.substring(0, 4);
                String mln = line.substring(5).replaceAll(" ", "");
                map.put(mln, prob);//<k,v>=<mln,prob>
            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }*/

    public static HashMap<String, String> readFileToHash(String URL) {
        HashMap<String, String> map = new HashMap<>();

        FileReader reader;
        try {
            reader = new FileReader(URL);
            BufferedReader br = new BufferedReader(reader);
            String line;
//            br.readLine();
            while ((line = br.readLine()) != null && line.length() != 0) {
                String prob = line.substring(0, 4);
                String mln = line.substring(5);
                map.put(mln, prob);//<k,v>=<mln,prob>
            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static ArrayList<String> readFile(String URL) {
        ArrayList<String> list = new ArrayList<>();

        FileReader reader;
        try {
            reader = new FileReader(URL);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null && line.length() != 0) {
                list.add(line);
            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static ArrayList<String> readFileNoHeader(String URL) {
        ArrayList<String> list = new ArrayList<>();

        FileReader reader;
        try {
            reader = new FileReader(URL);
            BufferedReader br = new BufferedReader(reader);
            String line;
            br.readLine();
            while ((line = br.readLine()) != null && line.length() != 0) {
                list.add(line);
            }
            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * @param outFile
     * @throws IOException
     */
    public void formatEvidence(String outFile, ArrayList<Integer> ignorIDs) throws IOException {
        String content = "";
        //Clean all the out content in 'outFile'
        FileWriter fw;
        System.out.println(">> Write Evidence to file (evidence.db) ...");

        try {
            fw = new FileWriter(outFile);
            fw.write("");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int index = 1;
        class IndexAndCount {
            int count;
            int index;
            int headerIndex;

            IndexAndCount(int count, int headerIndex) {
                this.count = count;
                this.index = 0;
                this.headerIndex = headerIndex;
            }

            public void increase() {
                count++;
            }

            public void setIndex(int index) {
                this.index = index;
            }

            public double getCount() {
                return (double) count;
            }

            public int getIndex() {
                return index;
            }

            public int getHeaderIndex() {
                return headerIndex;
            }
        }

        /*//记录一个属性下所有的值和对应的出现次数，和对应的 属性索引
        HashMap<String, IndexAndCount> map = new HashMap<>();
        ArrayList<String[]> tupleList = readFile(dataFile);
        for (int i = 0; i < header.length; i++) {
            for(int k = 0; k < tupleList.size(); k++){
                String item = tupleList.get(k)[i];
                if (!map.containsKey(item)) {
                    map.put(item, new IndexAndCount(1, i));
                } else {
                    map.get(item).increase();
                }
            }
        }*/

        //记录一个属性下所有的值和对应的出现次数，和对应的 属性索引
        HashMap<String, IndexAndCount> map = new HashMap<>();
        ArrayList<Integer> newHeaderList = new ArrayList<>(header.length);

        for (int i = 0; i < header.length; i++) {
            newHeaderList.add(i);
        }

        for (int i = 0; i < ignorIDs.size(); i++) {
            newHeaderList.remove(ignorIDs.get(i));
        }
        System.out.print("new Header = ");
        for (Integer i : newHeaderList) {
            System.out.print(i + " ");
        }
        System.out.println();

        for (int i = 0; i < newHeaderList.size(); i++) {
            for (int k = 0; k < tupleList.size(); k++) {
                try{
                    String item = tupleList.get(k).getContext()[newHeaderList.get(i)];
                    if (!map.containsKey(item)) {
                        map.put(item, new IndexAndCount(1, newHeaderList.get(i)));
                    } else {
                        map.get(item).increase();
                    }
                }catch (Exception e) {
                    System.out.println(k);
                    System.out.println(i);
                    System.out.println(newHeaderList.get(i));
                    System.out.println(tupleList.get(k));
                }
            }
        }


        for (Map.Entry<String, IndexAndCount> entry : map.entrySet()) {
            // System.out.println(entry);
            if (entry.getValue().getIndex() == 0) { //这个是标志entry的位置
                entry.getValue().setIndex(index);
                index++;
            }
//          System.out.println(entry.getValue().getIndex()+' '+entry.getKey());
            double pre = entry.getValue().getCount() / tupleList.size(); // 属性值占的百分比
            pre += 0.0001;
            if (pre > 1) pre = 1;

            DecimalFormat format = new DecimalFormat("#0.0000");
            content += format.format(pre) + " ";
            content += header[entry.getValue().getHeaderIndex()] + "(\"" + entry.getKey() + "\")" + "\n";
        }

        writeToFile(content, outFile);
        System.out.println(">> Writing Completed!");

//        String url = Config.db_url;
//        String username = Config.db_username;
//        String password = Config.db_password;
//
//        try {
//            Class.forName("org.postgresql.Driver").newInstance();
//            Connection conn = DriverManager.getConnection(url, username, password);
//            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
//
//            // 下面的代码主要是向数据库写入数据文件
//
//            String sql = "DROP TABLE IF EXISTS temp CASCADE;";
//            stmt.execute(sql);
//
//            sql = "CREATE TABLE temp(";
//            for (int i = 0; i < header.length; i++) {
//                sql += i == header.length - 1 ? header[i] + " bigint);" : header[i] + " bigint,";
//            }
//            System.out.println(sql);
//            stmt.execute(sql);
//
//            // create index ?
//            sql = "CREATE INDEX temp_idx ON temp (";
//            for (int i = 0; i < header.length; i++) {
//                sql += i == header.length - 1 ? header[i] + ");" : header[i] + ", ";
//            }
//
//            System.out.println(sql);
//            stmt.execute(sql);
//
//            for (Tuple t : tupleList) {
//                sql = "INSERT INTO temp VALUES(";
//                for (int i = 0; i < t.getContext().length; i++) {
//                    String item = t.getContext()[i];
//                    sql += i == t.getContext().length - 1 ? map.get(item).getIndex() : map.get(item).getIndex() + ",";
//                }
//                sql += ");";
//                // System.out.println(sql);
//                stmt.execute(sql);
//            }
//            stmt.close();
//            conn.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    /**
     * @param fileURL
     * @param outFile
     */
    public void formatRules(String fileURL, String outFile) {

        FileReader inFile;
        try {
            inFile = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(inFile);
            String str = null;
            String firstOrderLogic = "";
            System.out.println(">> Write first-order-logic rules to file (prog.mln) ...");

            //Clean all the out content in 'outFile'
            FileWriter fw = new FileWriter(outFile);
            fw.write("");
            fw.close();

            while ((str = br.readLine()) != null && str.length() != 0) {
                firstOrderLogic = "1\t";
                String[] line = str.split("=>");

                String[] reason = line[0].replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                String[] result = line[1].replaceAll("\\[", "").replaceAll("\\]", "").split(",");

                //reason
                for (int index = 0; index < reason.length; index++) {
                    if (reason[index].contains("=")) {
                        String[] current = reason[index].split("=");
                        setPredicate(current[0]);
                        setValue(current[1].replaceAll("'", "").replaceAll("'", ""));
                        firstOrderLogic += "!" + predicate + "(" + value + ") v ";
                    } else {
                        int value_index = 0;
                        for (int i = index; i < reason.length; i++) {
                            setPredicate(reason[i]);
                            setValue("x" + (value_index++));
                            firstOrderLogic += "!" + predicate + "(" + value + ") v ";
                        }
                        break;
                    }
                }

                //result
                for (int index = 0; index < result.length; index++) {
                    if (result[index].contains("=")) {
                        String[] current = result[index].split("=");
                        setPredicate(current[0]);
                        setValue(current[1].replaceAll("'", "").replaceAll("'", ""));
                        firstOrderLogic += predicate + "(" + value + ")";
                    } else {
                        int value_index = 0;
                        for (int i = index; i < result.length; i++) {
                            setPredicate(result[i]);
                            setValue("y" + (value_index++));
                            firstOrderLogic += predicate + "(" + value + ")";
                        }
                        break;
                    }
                }
                //System.out.println(firstOrderLogic+"\n");

                writeToFile(firstOrderLogic, outFile);
            }
            System.out.println(">> Writing Completed!");
            br.close();
            inFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    void setValue(String value) {
        this.value = value;
    }

    public static void writeToFile(String content, String outFile) {    //闁硅泛銈祇ntent闁汇劌瀚崬瀵革拷鍦攰閹风兘宕濋悩鎻掓櫢闁稿繈鍎查弸鍐╃閿燂拷
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outFile, true)));
            out.write(content + "\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * load Rules from files in disk
     */
    public static HashMap<String, Double> loadRulesFromFile(String fileURL) {
        FileReader reader;
        ArrayList<MLNClause> clauses = new ArrayList<MLNClause>();
        HashMap<String, Double> attributes = new HashMap<String, Double>();
        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            while ((str = br.readLine()) != null) {
                MLNClause mlnClause = new MLNClause();
                /*if(str.equals("4.1875,\t      !MeasureID(\"HAI_3_SIR\")  v  MeasureName(\"Surgical Site Infection from colon surgery (SSI Colon)\") ")){
                    System.out.println();
                }*/
                String weight = str.substring(0, str.indexOf(","));
                mlnClause.weight = Double.parseDouble(weight);
                str = str.substring(str.indexOf(",") + 1);

                String[] line = str.split(" v ");
                for (String value : line) {
                    value = value.replaceAll(".*\\(\"", "")
                            .replaceAll("\"\\).*", "");
                    mlnClause.values.add(value);
                }
                clauses.add(mlnClause);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (MLNClause c : clauses) {
            String[] tmp_value = new String[c.values.size()];
            for (int i = 0; i < tmp_value.length; i++) {
                tmp_value[i] = c.values.get(i);
            }
            Arrays.sort(tmp_value);
            attributes.put(Arrays.toString(tmp_value), c.weight);
        }

        return attributes;
    }

    /**
     * Init Rules
     */
    public void initRules(String fileURL) {
        //default fileURL = "E:\\eclipse_workspace\\DiscoverRules-v2.0\\rules.txt";
        FileReader reader;
        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            while ((str = br.readLine()) != null) {

                String[] line = str.split("=>");
                String[] reason = line[0].replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                String[] result = line[1].replaceAll("\\[", "").replaceAll("\\]", "").split(",");

                //reason
                System.out.print("reason: ");
                for (int index = 0; index < reason.length; index++) {
                    if (reason[index].contains("=")) {
                        String[] current = reason[index].split("=");
                        setPredicate(current[0]);
                        System.out.print("P = " + predicate + ", ");
                        setValue(current[1].replaceAll("'", "").replaceAll("'", ""));
                        System.out.print("V = " + value + ".  ");
                    } else {
                        for (int i = index; i < reason.length; i++) {
                            setPredicate(reason[i]);
                            System.out.print("P = " + predicate + ", ");
                            System.out.print("V = any value.  ");
                        }
                        break;
                    }
                }
                System.out.println();

                //result
                System.out.print("result: ");
                for (int index = 0; index < result.length; index++) {
                    if (result[index].contains("=")) {
                        String[] current = result[index].split("=");
                        setPredicate(current[0]);
                        System.out.print("P = " + predicate + ", ");
                        setValue(current[1].replaceAll("'", "").replaceAll("'", ""));
                        System.out.print("V = " + value + ".  ");
                    } else {
                        for (int i = index; i < result.length; i++) {
                            setPredicate(result[i]);
                            System.out.print("P = " + predicate + ", ");
                            System.out.print("V = any value.  ");
                        }
                        break;
                    }
                }
                System.out.println();
            }
            br.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Init Data
     */


    public void initData(String fileURL, String splitString, boolean ifHeader) {//check if the data has header
        // read file content from file
        FileReader reader;
        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            int index = 0; //tuple index
            if (ifHeader && (str = br.readLine()) != null) {  //The data has header

//                header = str.substring(str.indexOf(",") + 1).split(splitString);
                header = str.split(splitString);

                while ((str = br.readLine()) != null) {
                    Tuple t = new Tuple();
                    // str = str.substring(str.indexOf(",") + 1);
                    String[] data = str.split(",");
                    for (int i = 0; i < data.length; i++) {
                        if (data[i] == null || data[i].isEmpty()) {
                            data[i] = "null";
                        }
                    }
                    String str_concat = String.join(",", data);
                    t.init(str_concat, splitString, index);//init the tuple,split with ","
                    tupleList.add(t);
                    index++;
                }
            } else { //如果没有header
                while ((str = br.readLine()) != null) {
                    Tuple t = new Tuple();
                    // str = str.substring(str.indexOf(",") + 1);
                    String[] data = str.split(",");
                    for (int i = 0; i < data.length; i++) {
                        if (data[i] == null || data[i].isEmpty()) {
                            data[i] = "null";
                        }
                    }
                    String str_concat = String.join(",", data);
                    t.init(str_concat, splitString, index);//init the tuple,split with ","
                    tupleList.add(t);
                    index++;
                }
                int length = tupleList.get(0).getContext().length;
                header = new String[length];
                char c = 64;
                for (int i = 1; i <= length; i++) {
                    c += 1;
                    header[i - 1] = String.valueOf(c);
                }
            }
            br.close();
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void resample(ArrayList<Tuple> newTupleList, int sanpleSize) {

        ArrayList<Tuple> newList = new ArrayList<Tuple>();
        int totalSize = tupleList.size();
        for (int i = 0; i < sanpleSize; i++) {
            int index = (int) (Math.random() * totalSize);
            newList.add(tupleList.get(index));
        }
        tupleList = newList;
    }
}