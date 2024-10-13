package main;

import clustering.DBSCAN;
import clustering.Kmeans;
import data.ConvertInfo;
import data.Domain;
import data.Rule;
import data.Tuple;
import tuffy.main.MLNmain;
import util.Log;

import java.io.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import static main.OutlierAnalyse.conflictAnalysis;
import static main.OutlierAnalyse.crowdGroupAnalysis;
import static main.OutlierAnalyse.writeConflictToFile;
import static main.ReliableScoreAnalyse.*;
import static main.Test.pickData;
import static main.Test.read;


public class Main {
    static String[] header = null;
    public static String baseURL = "/mnt/d/algorithm paper/ML algorithms codes/data_experiments/Automatic-Data-Repair/mlnclean/dataset";    // experiment baseURL 修改为计算机的实际路径
    //static String rootURL = System.getProperty("user.dir"); //Project BaseURL
    static String cleanedFileURL = baseURL + "/RDBSCleaner_cleaned.txt";
    static ArrayList<Integer> ignoredIDs = null;
    public static String rulesURL = baseURL + "/HAI/rawData/rules.txt";
    //public static String dataURL = baseURL + "/HAI/HAI-5q-10%-error.csv";

    public static HashMap<String, String> readMLNFile(String mlnFile) {
        HashMap<String, String> result = new HashMap<String, String>();
        try {
            FileReader reader = new FileReader(mlnFile);
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            //escape predicate
//            while((line = br.readLine()) != null) {
//                if(line.length()==0)break;
//            }
            while ((line = br.readLine()) != null && line.length() != 0) {
                String rule_noWeight = line.substring(line.indexOf(",") + 1).trim();
                String weight = line.substring(0, line.indexOf(","));
                result.put(rule_noWeight, weight);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *
     */
    public static void setLineID(String readURL, String writeURL, String splitStr) {
        // read file content from file
        FileReader reader = null;
        try {
            reader = new FileReader(readURL);
            BufferedReader br = new BufferedReader(reader);

            // write string to file

            FileWriter writer = new FileWriter(writeURL);
            BufferedWriter bw = new BufferedWriter(writer);

            String str = "";
            int index = 0;
            while ((str = br.readLine()) != null) {
                StringBuffer sb = new StringBuffer(str);
                if (index == 0) {
                    sb.insert(0, "ID" + splitStr);
                    bw.write(sb.toString() + "\n");
                } else {
                    sb.insert(0, index + splitStr);
                    bw.write(sb.toString() + "\n");
                }
                index++;
                //System.out.println(sb.toString());
            }
            br.close();
            reader.close();
            bw.close();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void learnwt(String[] args, ArrayList<Integer> ignoredIDs) throws SQLException, IOException {
        String dataURL = args[1];

        double startTime = System.currentTimeMillis();

        Rule rule = new Rule();
        // Domain domain = new Domain();
        String evidence_outFile = baseURL + "/" + args[0] + "/evidence.db";

        // System.out.println("rootURL=" + rootURL);
        cleanedFileURL = baseURL + "/" + args[0] + "RDBSCleaner_cleaned.txt"; //CleanFile之后的文件
        System.out.println("dataURL = " + dataURL);
        String splitString = ",";

        boolean ifHeader = true;
        //List<Tuple> rules = rule.loadRules(dataURL, rulesURL, splitString);
        rule.initData(dataURL, splitString, ifHeader);//这函数就是把dataURL的文件全部堵到了一个tuplelist
        //ArrayList<Tuple> newTupleList = rule.tupleList;
        
        //ignoredIDs = rule.findIgnoredTuples(rules);
        // domain.header = rule.header;
        // header = rule.header;
        // domain.createMLN(rule.header, rulesURL);


        ArrayList<String> list = new ArrayList<>();
        String marginal_args = "-marginal";
        //list.add(marginal_args);
        String learnwt_args = "-learnwt";
        list.add(learnwt_args);
        String nopart_args = "-nopart";
        list.add(nopart_args);
        String mln_args = "-i";
        list.add(mln_args);
//        String mlnFileURL = baseURL + "/HAI/prog-new.mln";//prog.mln
        String mlnFileURL = args[2];
        list.add(mlnFileURL);
        String evidence_args = "-e";
        list.add(evidence_args);
        String evidenceFileURL = baseURL + "/" + args[0] + "/evidence.db"; //samples/smoke/
        list.add(evidenceFileURL);
        String queryFile_args = "-queryFile";
        list.add(queryFile_args);
        String queryFileURL = baseURL + "/" + args[0] + "/query.db";
        list.add(queryFileURL);
        String outFile_args = "-r";
        list.add(outFile_args);
//        String weightFileURL = baseURL + "/HAI/out.txt";
        String weightFileURL = args[3];
        list.add(weightFileURL);
        String noDropDB = "-keepData";
        list.add(noDropDB);
        String maxIter_args = "-dMaxIter";
        list.add(maxIter_args);
        String maxIter = "100";
        list.add(maxIter);
        String mcsatSamples_args = "-mcsatSamples";
        //list.add(mcsatSamples_args);
        String mcsatSamples = "20";
        //list.add(mcsatSamples);
        String[] learnwt = list.toArray(new String[list.size()]);

        /*
        * 训练阶段
        * */

        int batch = 1;
        int sampleSize = 20;

        for (int i = 0; i < batch; i++) {
            //rule.resample(newTupleList,sampleSize);
            System.out.println(evidence_outFile);
            rule.formatEvidence(evidence_outFile, ignoredIDs);

            //using 'Diagonal Newton discriminative learning'
            MLNmain.main(learnwt);

            //updateprogMLN("/data/nw/DC_ED/References_inner_and_outer/mlnclean/dataset/HAI/out.txt" , dataURL);
        }
    }

    /**
     *
     */
    public static void getGroundGroup(String groundGroupURL, String groundGroupURL2, String[] args, String[] header, ArrayList<Integer> ignoredIDs, Log log, int threshold) {
        try {

            String dataURL = baseURL + "/" + args[0] + "/" + args[1];
            String rulesURL = baseURL + "/" + args[0] + "/rules.txt";

            String tmp_dataURL = dataURL;
            Rule rule = new Rule();
            Domain domain = new Domain();

            System.out.println("dataURL = " + tmp_dataURL);

            String splitString = ",";
            boolean ifHeader = true;
            List<Tuple> rules = rule.loadRules(rulesURL, splitString);
            rule.initData(tmp_dataURL, splitString, ifHeader);
            domain.header = header;

            System.out.println(">>> Partition dataset into Domains...");
            List<HashMap<String, ConvertInfo>> convert_domains = new ArrayList<>(rules.size());

            domain.init(tmp_dataURL, splitString, ifHeader, rules, convert_domains, ignoredIDs);

            writeGroundGroupsToFile(groundGroupURL, convert_domains);

            domain.groupByKey(args[0], domain.domains, rules, convert_domains, log, threshold);

            OutlierAnalyse.writeGroupsToFile(domain.Domain_to_Groups, groundGroupURL2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * */
    public static ArrayList<String> getGroundSampleFile(HashMap<Integer, String[]> dataSet, String groundTruthURL, String outFile, String[] header){
        ArrayList<String> ground_sampledata = pickData(dataSet, groundTruthURL);
        ground_sampledata.sort(new Comparator<String>() {
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
        Main.writeToFile(header, ground_sampledata, outFile);
        return ground_sampledata;
    }

    public static HashMap<Integer, String[]> main(String[] args, String[] header, ArrayList<Integer> ignoredIDs, Log log, int threshold) throws SQLException, IOException {

        String dataURL = baseURL + "/" + args[0] + "/" + args[1];
        String rulesURL = baseURL + "/" + args[0] + "/rules.txt";
        String tmp_dataURL = dataURL;
        Rule rule = new Rule();
        Domain domain = new Domain();

        //System.out.println("rootURL=" + rootURL);
        cleanedFileURL = baseURL + "/RDBSCleaner_cleaned.txt";
        System.out.println("dataURL = " + tmp_dataURL);

        String splitString = ",";
        boolean ifHeader = true;
        List<Tuple> rules = rule.loadRules(rulesURL, splitString);
        rule.initData(tmp_dataURL, splitString, ifHeader);
        domain.header = header;
        /*ignoredIDs = rule.findIgnoredTuples(rules);
        domain.header = rule.header;
        header = rule.header;*/


        /*
        *
        * */
        int batch = 1;
        List<HashMap<String, Double>> attributesPROBList = new ArrayList<>();
        for (int i = 0; i < batch; i++) {

            System.out.println(">>> load Clauses Weight from MLNs out.txt");
            HashMap<String, Double> attributesPROB = Rule.loadRulesFromFile(baseURL + "/" + args[0] + "/out.txt");
            attributesPROBList.add(attributesPROB);
            System.out.println(">>> completed!");
        }


        System.out.println(">>> Partition dataset into Domains...");
        List<HashMap<String, ConvertInfo>> convert_domains = new ArrayList<>(rules.size());


        domain.init(tmp_dataURL, splitString, ifHeader, rules, convert_domains, ignoredIDs); //锟斤拷始锟斤拷dataSet,dataSet_noIgnor,domains. convert_domains锟斤拷锟斤拷锟斤拷锟�<Tuple,List<tupleID>>
//        OutlierAnalyse.writeDomainToFile(domain.domains, baseURL + "/" + args[0] + "/domainList.txt");
//        System.exit(0);

        System.out.println(">>> Completed!");
        //domain.printDomainContent(domain.domains);


        System.out.println(">>> Do groupByKey process for each Domain...");

        DecimalFormat df = new DecimalFormat("#.00");
        double startTime = System.currentTimeMillis();


        //kmeans group
        //new Kmeans().train(domain.domains.get(0),10);
        //DBSCAN.run(domain.domains.get(0),1,2);
        //System.exit(0);



        /*String crow_groupsURL = Main.baseURL + "/" + args[0] + "/groups_reorganize.txt";
        domain.Domain_to_Groups = OutlierAnalyse.readOutlierFile(crow_groupsURL);*/


        double precision = 1;
        String groundTruthURL = Main.baseURL + "/" + args[0] + "/ground_truth-hasID.csv";
        domain.Domain_to_Groups = OutlierAnalyse.generateGroup(Main.baseURL + "/" + args[0], precision, dataURL, groundTruthURL, rulesURL);
//        System.exit(0);

//        List<List<Tuple>> domain_outlier = domain.groupByKey(args[0], domain.domains, rules, convert_domains, log, threshold);
//        OutlierAnalyse.writeGroupsToFile(domain.Domain_to_Groups, baseURL + "/" + args[0] + "/ground_group.txt");

//        System.exit(0);


        /*int size = 0;
        System.err.println(">>>Outlier List<<<");
        for (int i = 0; i < domain_outlier.size(); i++) {
            List<Tuple> tuples = domain_outlier.get(i);
            size += tuples.size();
            for (int j = 0; j < tuples.size(); j++) {
                System.err.println(tuples.get(j).tupleID);
                System.err.println(Arrays.toString(tuples.get(j).getAttributeIndex()));
                System.err.println(Arrays.toString(tuples.get(j).getContext()));
            }
        }

        System.out.println("\noutlier tuple.size="+size+"\n");
        log.write("\noutlier tuple.size="+size+"\n");*/


        System.out.println(">>> Completed!");
        System.out.println(">>> Smooth outliers to matched Groups...");
//        domain.smoothOutlierToGroup(args[0], domain_outlier, domain.Domain_to_Groups, domain.dataSet, convert_domains, domain.dataSet_noIgnor, threshold);

        double endTime = System.currentTimeMillis();
        double outlierTime = (endTime - startTime) / 1000;
        log.write("\noutlier Merging Time: " + df.format(outlierTime) + " s\n");
        System.out.println("outlier Merging Time: " + df.format(outlierTime) + "s");

        System.out.println(">>> Completed!");

        System.out.println(">>> Correct error Data By MLN probs...");

        List<HashMap<String, ArrayList<Integer>>> list = new ArrayList<>(domain.Domain_to_Groups.size());//瀛樺偍淇�姝ｅ悗鐨勭粨鏋�

        domain.correctByMLN(domain.Domain_to_Groups, attributesPROBList, domain.header, domain.domains, list);

        /*int d_index = 0;
        for (List<HashMap<Integer, Tuple>> d : domain.Domain_to_Groups) {
            System.out.println("\n*******Domain " + (d_index + 1) + "*******");
            log.write("\n*******Domain " + (d_index + 1) + "*******");
            domain.printGroup(d, log);
            ++d_index;
        }*/

        /** ===test reliability score===*/
        String CleanWithinGroupURL = baseURL + "/" + args[0] + "/CleanWithinGroup.txt";
        writeGroupsToFile(CleanWithinGroupURL, list);

        String groundGroupURL = baseURL + "/" + args[0] + "/groupInfo_of_ground_truth.txt";//浠�gamma, ArrayList<IDs>褰㈠紡淇濆瓨
        String groundGroupURL2 = baseURL + "/" + args[0] + "/ground_group.txt"; //璁板綍group涓�鐨勬瘡涓€鏉�gamma淇℃伅
        String[] ags = {args[0], "ground_sampleData.csv"};

        ArrayList<String> ground_data = getGroundSampleFile(domain.dataSet, baseURL  + "/" + args[0] + "/ground_truth-hasID.csv",
                baseURL  + args[0] + "/ground_sampleData.csv", header);
        getGroundGroup(groundGroupURL, groundGroupURL2, ags, header, ignoredIDs, log, threshold); //浠巊round truth file涓�鐢熸垚ground_group_file

        reliabilityScoreAnalysisByGamma(CleanWithinGroupURL, groundGroupURL, log);
        /**=============================*/

        /**test Crowd Abnormal Group Precision and Recall*/
        //crowdGroupAnalysis(crow_groupsURL, groundGroupURL2, log);

        System.out.println(">>> Completed!");

//        System.exit(0);


//        domain.printDomainContent(domain.domains);

        System.out.println(">>> Combine Domains...");
        List<List<Integer>> keysList = domain.combineDomain(domain.Domain_to_Groups);    //锟斤拷锟斤拷锟斤拷锟斤拷锟截革拷锟斤拷锟斤拷锟絫upleID,锟斤拷锟斤拷录锟截革拷元锟斤拷

        if (null == keysList || keysList.isEmpty()) System.out.println("\tNo duplicate exists.");
        else {
            System.out.println("\n>>> Delete duplicate tuples");

            // domain.printDataSet(domain.dataSet);
//             domain.deleteDuplicate(keysList, domain.dataSet);
            // domain.printDataSet(domain.dataSet);
            System.out.println(">>> completed!");
        }

        domain.printConflicts(domain.conflicts);
        writeConflictToFile(domain.conflicts, args[0] + "/conflicts_" + threshold + ".txt");
        writeToFile(baseURL + "/" + args[0] + "/clean_within_group.txt", domain.dataSet, domain.header);


        domain.findCandidate(domain.conflicts, domain.domains, attributesPROBList.get(0), ignoredIDs);

        //print dataset after cleaning
        //domain.printDataSet(domain.dataSet);

        writeToFile(cleanedFileURL, domain.dataSet, domain.header);

        return domain.dataSet;
    }

    public static void writeToFile(String[] header, ArrayList<String> list, String outFile) {
        File file = new File(outFile);
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            if (file.exists()) {
                System.out.println(": " + outFile);
            } else if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                }
            } else {
                file.createNewFile();
            }
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);

            writer.write("ID," + Arrays.toString(header)
                    .replaceAll("[\\[\\]]", "")
                    .replaceAll(" ", ""));
            writer.newLine();

            for (String str : list) {
                writer.write(str);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(String cleanedFileURL, HashMap<Integer, String[]> dataSet, String[] header) {

        List<Entry<Integer, String[]>> list = new ArrayList<>(dataSet.entrySet());

        Collections.sort(list, new Comparator<Entry<Integer, String[]>>() {
            @Override
            public int compare(Entry<Integer, String[]> o1, Entry<Integer, String[]> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        File file = new File(cleanedFileURL);
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            if (file.exists()) {
                System.out.println("锟侥硷拷锟窖达拷锟斤拷: " + cleanedFileURL);
            } else if (!file.getParentFile().exists()) {

                System.out.println("目锟斤拷锟侥硷拷锟斤拷锟斤拷目录锟斤拷锟斤拷锟节ｏ拷准锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷");
                if (!file.getParentFile().mkdirs()) {// 锟叫断达拷锟斤拷目录锟角凤拷晒锟�
                    System.out.println("锟斤拷锟斤拷目锟斤拷锟侥硷拷锟斤拷锟节碉拷目录失锟杰ｏ拷");
                }
            } else {
                file.createNewFile();
            }
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);


            writer.write("ID," + Arrays.toString(header)
                    .replaceAll("[\\[\\]]", "")
                    .replaceAll(" ", ""));
            writer.newLine();

            for (Entry<Integer, String[]> map : list) {

                String[] value = map.getValue();
                String line = "";
                for (int i = 0; i < value.length; i++) {
                    if (i != value.length - 1) {
                        line += value[i] + ",";
                    } else line += value[i];
                }
//                String line = Arrays.toString(map.getValue()).replaceAll("[\\[\\]]", "").replaceAll(" ", "");
                writer.write(map.getKey() + "," + line);
                writer.newLine();
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

