package data;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import com.sun.org.apache.xpath.internal.operations.Bool;
import info.debatty.java.stringsimilarity.*;
// import javafx.scene.input.DataFormat;
import jdk.nashorn.internal.ir.annotations.Ignore;
import main.OutlierAnalyse;
import spellchecker.SpellChecker;
import util.Log;

public class Domain {

    public static double MIN_DOUBLE = 0.0001;
    public static double MIN_NEGNATIVE = -9;
    public static double MAX_NEGNATIVE = -99999999;
    public static double MAX_DOUBLE = 9999;

    public static double THRESHOLD = 0.01;

    public static int AVGNum = 0;

    public HashMap<Integer, String[]> dataSet = new HashMap<>();
    public HashMap<Integer, ArrayList<String>> dataSet_noIgnor = new HashMap<>();

    //	public List<String[]> dataSet = new ArrayList<String[]>();
    public List<HashMap<Integer, Tuple>> domains = null;
    //	public List<HashMap<Integer, Tuple>> groups = null;
    public List<List<HashMap<Integer, Tuple>>> Domain_to_Groups = null;    //List<groups> 存放group by key后，Domain中包含的group的编号

//    public HashMap<Integer, Conflicts> conflicts = new HashMap<>();    //记录冲突的元组，并按Domain分类 <DomainID, ConflictTuple>

    public ArrayList<Integer> conflicts = new ArrayList<>();//记录冲突元组所在的行ID

    //属性列，如果数据集中没有给出，则构造一个属性列：Attr1,Attr2,Attr3,...,AttrN
    public static String[] header = null;

    public Domain() {
    }

    // 这个函数的作用是创造 mln 文件。
//	public void createMLN(String[] header, String rulesURL){
//		File file = new File(rulesURL);
//		BufferedReader reader = null;
//		ArrayList<String> lines = new ArrayList<String>();
//		try {
//			reader = new BufferedReader(new FileReader(file));
//			String line = null;
//			while ((line = reader.readLine()) != null && line.length()!=0) {
//				lines.add(line);
//			}
//			reader.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (reader != null) {
//				try {
//					reader.close();
//				} catch (IOException e1) {
//				}
//			}
//		}
//
//		String mlnURL = baseURL+"dataSet\\HAI\\prog.mln";//prog.mln;
//		String content = null;
//		try {
//			//打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
//			FileWriter writer = new FileWriter(mlnURL, false);
//			for(int i=0;i<header.length;i++){
//				content = header[i]+"(value"+header[i]+")\n";
//				writer.write(content);
//			}
//			writer.write("\n");
//			for(int i=0;i<lines.size();i++){
//				writer.write("1\t"+lines.get(i)+"\n");
//			}
//			writer.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}


    /**
     * 按rules对数据集进行纵向划分 Partition DataSet into Domains
     *
     * @param fileURL
     * @param splitString
     * @param ifHeader
     * @param rules
     */
    public HashMap<Integer, String[]> init(String fileURL, String splitString, boolean ifHeader, List<Tuple> rules,
                                           List<HashMap<String, ConvertInfo>> convert_domains, ArrayList<Integer> ignoreIDs) {
        // read file content from file 读取文件内容
        FileReader reader;
        int rules_size = rules.size();
        domains = new ArrayList<>(rules_size);

        for (int i = 0; i < rules_size; i++) {
            domains.add(new HashMap<>());
            convert_domains.add(new HashMap<>());
        }
        try {
            reader = new FileReader(fileURL);
            BufferedReader br = new BufferedReader(reader);
            String str = null;
            int key; //tuple index

            if (ifHeader && (str = br.readLine()) != null) {  //The data has header
                //   	header=str.split(splitString);
                while ((str = br.readLine()) != null) {
                    //	System.out.println(str);
                    //dataSet.add(str.split(splitString));
//                    str = str.replaceAll(" ", "");
                    key = Integer.parseInt(str.substring(0, str.indexOf(",")));
                    String[] tuple = str.substring(str.indexOf(",") + 1).split(",");
                    int t_length = tuple.length;
                    if (str.charAt(str.length() - 1) == ',' ) {
                        t_length = t_length + 1;
                    }
                    for (int i = 0; i < t_length; i++) {
                        if (tuple[i] == null || tuple[i].isEmpty()) {
                            tuple[i] = "null";
                        }
                    }
                    dataSet.put(key, tuple);
                    ArrayList<String> tuple_noIgnor = new ArrayList(tuple.length - ignoreIDs.size());
                    for (String s : tuple) {
                        tuple_noIgnor.add(s);
                    }
                    for (int id : ignoreIDs) {
                        tuple_noIgnor.remove(tuple[id]);
                    }//tuple_noIgnor中不包含被ignored的属性
                    dataSet_noIgnor.put(key, tuple_noIgnor);

                    //为每一条rule划分数据集区域Di
                    for (int i = 0; i < rules_size; i++) {
                        Tuple curr_rule = rules.get(i);
                        String[] attributeNames = curr_rule.getAttributeNames();
                        int[] IDs = Rule.findAttributeIndex(attributeNames, header);
                        String[] reason = curr_rule.reason;
                        String[] result = curr_rule.result;
                        int[] reasonIDs = Rule.findAttributeIndex(reason, header);
                        int[] resultIDs = Rule.findResultIDs(IDs, reasonIDs);
                        String[] reasonContent = new String[reasonIDs.length];
                        String[] resultContent = new String[resultIDs.length];
                        String[] tupleContext = new String[IDs.length];
                        int[] tupleContextID = new int[IDs.length];

                        int reason_index = 0;
                        int result_index = 0;

                        for (int j = 0; j < IDs.length; j++) {
                            tupleContext[j] = tuple[IDs[j]];//放入属于该区域的tuple内容
                            tupleContextID[j] = IDs[j];    //放入对应的ID
                            if (ifReason(IDs[j], reasonIDs)) {    //如果是reason,则放入reasonContent
                                reasonContent[reason_index++] = tuple[IDs[j]];
                            } else {
                                resultContent[result_index++] = tuple[IDs[j]];
                            }
                        }

                        Tuple t = new Tuple();
                        t.tupleID = key;
                        t.setContext(tupleContext);
                        t.setAttributeIndex(tupleContextID);

                        t.setReason(reasonContent);
                        t.setReasonAttributeIndex(reasonIDs);

                        t.setResult(resultContent);
                        t.setResultAttributeIndex(resultIDs);

                        t.setAttributeNames(attributeNames);

                        //区域Di划分完毕,放入hashMap
                        domains.get(i % rules_size).put(key, t); //<K,V>  K = tuple ID , from 0 to n

                        String tuple_content = "";
                        for (int k = 0; k < tupleContext.length; k++) {
                            tuple_content += tupleContext[k];
                            if (k != tupleContext.length - 1) {
                                tuple_content += ",";
                            }
                        }

                        String tuple_reason = "";
                        for (int k = 0; k < reasonContent.length; k++) {
                            tuple_reason += reasonContent[k];
                            if (k != reasonContent.length - 1) {
                                tuple_reason += ",";
                            }
                        }


                        if (convert_domains.get(i % rules_size).get(tuple_reason) != null) {
                            convert_domains.get(i % rules_size).get(tuple_reason).idlist.add(t.tupleID);
                        } else {
                            ArrayList<Integer> ids = new ArrayList<>();
                            ids.add(t.tupleID);
                            ConvertInfo ci = new ConvertInfo(ids, tuple_content);
                            convert_domains.get(i % rules_size).put(tuple_reason, ci);
                        }
                    }
                    //key++;
                }
            } else {
                int length = 0;
//	        	boolean flag = false;
                while ((str = br.readLine()) != null) {
                    //dataSet.add(str.split(splitString));
                    key = Integer.parseInt(str.substring(0, str.indexOf(",")));
                    String[] tuple = str.substring(str.indexOf(",") + 1).split(",");
                    dataSet.put(key, tuple);

                    for (int i = 0; i < rules_size; i++) {    //为每一条rule划分数据集区域Di
                        Tuple curr_rule = rules.get(i);
                        String[] attributeNames = curr_rule.getAttributeNames();

                        int[] IDs = Rule.findAttributeIndex(attributeNames, header);

                        String[] reason = curr_rule.reason;
//                        String[] result = curr_rule.result;

                        int[] reasonIDs = Rule.findAttributeIndex(reason, header);
                        int[] resultIDs = Rule.findResultIDs(IDs, reasonIDs);
                        String[] reasonContent = new String[reasonIDs.length];
                        String[] resultContent = new String[resultIDs.length];
                        String[] tupleContext = new String[IDs.length];

                        int[] tupleContextID = new int[IDs.length];

                        int reason_index = 0;
                        int result_index = 0;
                        for (int j = 0; j < IDs.length; j++) {
                            tupleContext[j] = tuple[IDs[j]];//放入属于该区域的tuple内容
                            tupleContextID[j] = IDs[j];    //放入对应的ID
                            if (ifReason(IDs[j], reasonIDs)) {    //如果是reason,则放入reasonContent
                                reasonContent[reason_index++] = tuple[IDs[j]];
                            } else {
                                resultContent[result_index++] = tuple[IDs[j]];
                            }
                        }

                        Tuple t = new Tuple();
                        t.setContext(tupleContext);
                        t.setAttributeIndex(tupleContextID);
                        t.setReason(reasonContent);

                        t.setReasonAttributeIndex(reasonIDs);

                        t.setResult(resultContent);
                        t.setResultAttributeIndex(resultIDs);
                        t.setAttributeNames(attributeNames);
                        //一个 t 是 一个 区域
                        //区域Di划分完毕,放入hashMap
                        domains.get(i % rules_size).put(key, t); //<K,V>  K = tuple ID , from 0 to n
                    }
                    key++;
                }
                System.out.println("header length = " + length);
            }

            br.close();
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataSet;
    }

    public static boolean ifContains(int[] IDs, int[] bigIDs) {
        boolean result = false;
        int count = 0;
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>(bigIDs.length);
        for (int i = 0; i < bigIDs.length; i++) {
            map.put(bigIDs[i], 1);
        }
        int i = 0;
        for (; i < IDs.length && IDs[i] != -1; i++) {
            Integer value = map.get(IDs[i]);
            if (null != value) {
                count++;
            }
        }
        if (count == i) result = true;
        return result;
    }

    public static boolean ifReason(int[] IDs, int[] reasonIDs) {
        boolean result = false;
        int count = 0;
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>(reasonIDs.length);
        for (int i = 0; i < reasonIDs.length; i++) {
            map.put(reasonIDs[i], 1);
        }
        for (int i = 0; i < IDs.length; i++) {
            Integer value = map.get(IDs[i]);
            if (null != value) {
                count++;
            }
        }
        if (count == IDs.length) result = true;
        return result;
    }


    public static boolean ifReason(int index, int[] reasonIDs) {
        boolean result = false;
        int[] copy_reasonIDs = Arrays.copyOfRange(reasonIDs, 0, reasonIDs.length);
        Arrays.sort(copy_reasonIDs);
        for (int i = 0; i < copy_reasonIDs.length; i++) {
            if (index == copy_reasonIDs[i]) {
                result = true;
                break;
            }
        }
        return result;
    }



    /**
     * 二次划分，根据reason predicates 建立索引
     */
    public List<List<Tuple>> groupByKey(String data_args, List<HashMap<Integer, Tuple>> domains, List<Tuple> rules,
                                        List<HashMap<String, ConvertInfo>> convert_domains, Log log, int threshold) throws IOException {

        HashMap<Integer, Tuple> domain = null;
        HashMap<String, ConvertInfo> convert_domain = null;
        int size = domains.size();
        Domain_to_Groups = new ArrayList<>(size);

        List<List<HashMap<Integer, Tuple>>> Domain_to_Groups_for_analysis = new ArrayList<>(size); //for outlier analysis
        List<List<HashMap<Integer, Tuple>>> Domain_to_OutlierGroups_for_analysis = new ArrayList<>(size); //for outlier analysis

        List<List<Tuple>> domain_outlier = new ArrayList<>();
        for (int i = 0; i < size; i++) {

            domain = domains.get(i);
            convert_domain = convert_domains.get(i);

            int domain_size = domain.size(); //元组对数
            HashSet<Integer> flags = new HashSet<>();

            List<HashMap<Integer, Tuple>> groups = new ArrayList<>();
            List<HashMap<Integer, Tuple>> groups_for_analysis = new ArrayList<>(); //for outlier analysis
            List<HashMap<Integer, Tuple>> outlierGroup_for_analysis = new ArrayList<>(); //for outlier analysis

            List<Tuple> outlierTuple = new ArrayList<>();

            Iterator<Entry<String, ConvertInfo>> c_iter = convert_domain.entrySet().iterator();
            while (c_iter.hasNext()) {
                Entry<String, ConvertInfo> entry = c_iter.next();
                ArrayList<Integer> ids = entry.getValue().idlist;
                HashMap<Integer, Tuple> group = new HashMap<>();
                HashMap<Integer, Tuple> group_for_analysis = new HashMap<>();   //for outlier analysis
                HashMap<Integer, Tuple> outlierT_for_analysis = new HashMap<>();   //for outlier analysis

                AVGNum = threshold;

                //-----for outlier analysis-----
                for (Integer id : ids) {
                    group_for_analysis.put(id, domain.get(id));
                }
                groups_for_analysis.add(group_for_analysis);
                //-----for outlier analysis-----

                if (ids.size() > AVGNum) {  //regular group
                    for (Integer id : ids) {
                        group.put(id, domain.get(id));
                    }
                    groups.add(group);
                } else { //outlier group
                    for (Integer id : ids) {
                        outlierTuple.add(domain.get(id));
                        outlierT_for_analysis.put(id,domain.get(id));
                    }
                    outlierGroup_for_analysis.add(outlierT_for_analysis);
                }
            }
            if (groups.size() > 0) {
                Domain_to_Groups.add(groups);
                Domain_to_Groups_for_analysis.add(groups_for_analysis);
                Domain_to_OutlierGroups_for_analysis.add(outlierGroup_for_analysis);
            }
            domain_outlier.add(outlierTuple);

        }
        int d_index = 0;
        for (List<HashMap<Integer, Tuple>> d : Domain_to_Groups) {
            //if (d_index == 1) {
                System.out.println("\n*******Domain " + (d_index + 1) + "*******");
                log.write("\n*******Domain " + (d_index + 1) + "*******");
                printGroup(d, log);
            //}
            ++d_index;
        }

        OutlierAnalyse.writeGroupsToFile(Domain_to_Groups_for_analysis, data_args+"/total_group_info_"+threshold+".txt");

        OutlierAnalyse.writeGroupsToFile(Domain_to_OutlierGroups_for_analysis, data_args+"/outlier_analysis_"+threshold+".txt");

        return domain_outlier;
    }

    class DistanceInfo {
        int tupleID;
        double distance;
        String[] reason;
        int num; //最相似的group中有几个tuple

        DistanceInfo(int tupleID, double distance, int num) {
            this.tupleID = tupleID;
            this.distance = distance;
            this.num = num;
        }
    }

    public static ArrayList<DistanceInfo> insertSort(ArrayList<DistanceInfo> disList, DistanceInfo insertInfo, int MAXSIZE) {
        int size = disList.size();
        DistanceInfo MaxDis = disList.get(size - 1);
        if (insertInfo.distance < MaxDis.distance) {    //如果 insertInfo.distance > MaxDis.distance 不添加到disList中，否则执行以下操作，依次比较，排序
            for (int i = 0; i < size; i++) {
                DistanceInfo currDis = disList.get(i);
                if (insertInfo.distance < currDis.distance) {
                    disList.add(i, insertInfo);
                    break;
                }
            }
        }
        if (disList.size() > MAXSIZE)
            disList.remove(disList.size() - 1);//删除
        return disList;
    }

    public void smoothOutlierToGroup(String data_args, List<List<Tuple>> domain_outlier, List<List<HashMap<Integer, Tuple>>> Domain_to_Groups,
                                     HashMap<Integer, String[]> dataSet, List<HashMap<String, ConvertInfo>> convert_domains,
                                     HashMap<Integer, ArrayList<String>> dataSet_noIgnor, int threshold) {

        //HashMap<String, Double> attributesPROB = attributesPROBList.get(0);
        int top_k = 1;
        int MAXSORTSIZE = 1;

        //convert domain_outlier(List) to HashSet
        HashSet<Integer> hashSet = new HashSet<>();
        for (List<Tuple> tuples : domain_outlier) {
            for (Tuple t : tuples) {
                hashSet.add(t.tupleID);
            }
        }

        //compute tuple minDistance
        for (int i = 0; i < Domain_to_Groups.size(); i++) {
            List<HashMap<Integer, Tuple>> groupList = Domain_to_Groups.get(i);
            List<Tuple> outliers = domain_outlier.get(i);
            HashMap<String, ConvertInfo> convert_groups = convert_domains.get(i);

            for (Tuple outlierT : outliers) {
                String[] s_reason = outlierT.reason;
                String[] s_content = outlierT.getContext();

                /*if (outlierT.tupleID == 186) {
                    System.err.println("debug");
                }*/


                String out_content = "";
                for (int j = 0; j < s_content.length; j++) {
                    if (j != s_content.length - 1) {
                        out_content += s_content[j] + ",";
                    } else out_content += s_content[j];
                }

                String out_reason = "";
                for (int j = 0; j < s_reason.length; j++) {
                    if (j != s_reason.length - 1) {
                        out_reason += s_reason[j] + ",";
                    } else out_reason += s_reason[j];
                }
                double minDis = MAX_DOUBLE;
                int minID = 0;
                int mingi = -1;

                ArrayList<DistanceInfo> disList = new ArrayList<>();

                //复杂度太高
//                HashSet<String> flagBox = new HashSet<>();//存放已经比较过的tupleContext
                Iterator<Entry<String, ConvertInfo>> iter = convert_groups.entrySet().iterator();
//                Iterator<Entry<Integer, ArrayList<String>>> iter = dataSet_noIgnor.entrySet().iterator();
//                Iterator<Entry<Integer, String[]>> iter = dataSet.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, ConvertInfo> entry = iter.next();
                    if (entry.getValue().idlist.size() <= AVGNum) {
                        continue;//this is outlier group
                    }
                    String reason = entry.getKey();

                    String content = entry.getValue().content;

                    Cosine cosine = new Cosine();

                    /*if (reason.equals("10007")) {
                        System.err.println("debug");
                    }*/

                    double t_dis = SpellChecker.distance(out_content, content);
                    if (t_dis > 1.0) continue;//筛选距离超过0.1的group，不可能为候选项
                    /*if (i == 0) {
                        t_dis = cosine.distance(out_reason, reason);
                        if (t_dis > 0.06) continue;
                    }
                    if (i == 1) {
                        t_dis = SpellChecker.distance(out_reason, reason);
                        if (t_dis > 1) continue;
                    }*/
                    int key = entry.getValue().idlist.get(0);//记录该group中的任意一个tupleID
                    int num = entry.getValue().idlist.size();
                    DistanceInfo disInfo = new DistanceInfo(key, t_dis, num);
                    if (disList.size() == 0) {
                        disList.add(disInfo); //DistanceInfo = <TupleID,dis>
                    } else {
                        insertSort(disList, disInfo, MAXSORTSIZE);
                    }
                }
                /*disList.sort(new Comparator<DistanceInfo>() {
                    @Override
                    public int compare(DistanceInfo o1, DistanceInfo o2) {
                        if (o1.distance > o2.distance) {
                            return 1;
                        } else if (o1.distance == o2.distance) {
                            return 0;
                        } else {
                            return -1;
                        }
                    }
                });*/
                int n = top_k;
                int size = disList.size();
                if (size < top_k) {
                    n = size;
                }

                List<HashMap<String, Integer>> reverse_group_list = new ArrayList<>(groupList.size());
                for (int gi = 0; gi < groupList.size(); gi++) {
                    HashMap<Integer, Tuple> group = groupList.get(gi);
                    HashMap<String, Integer> reverse_group = new HashMap<>();
                    Iterator<Entry<Integer, Tuple>> it = group.entrySet().iterator();
                    while (it.hasNext()) {
                        Entry<Integer, Tuple> entry = it.next();

                        String[] value = entry.getValue().getContext();
                        String content = "";
                        for (int j = 0; j < value.length; j++) {
                            if (j != value.length - 1) {
                                content += value[j] + ",";
                            } else content += value[j];
                        }
                        if (null == reverse_group.get(content)) {
                            reverse_group.put(content, 1);
                        } else {
                            reverse_group.put(content, reverse_group.get(content) + 1);
                        }
                    }
                    reverse_group_list.add(reverse_group);

                }
                double minCOST = MAX_DOUBLE;
                for (int k = 0; k < n; k++) {
                    DistanceInfo disInfo = disList.get(k);
                    double dis = disInfo.distance;
                    int tupleID = disInfo.tupleID;
                    int num = disInfo.num;
                    int gi = 0;
                    double t_cost = 0;
                    for (; gi < groupList.size(); gi++) {
                        HashMap<Integer, Tuple> group = groupList.get(gi);
                        if (group.containsKey(tupleID)) {
                            /*String[] value = group.get(tupleID).getContext();
                            String content = "";
                            for (int j = 0; j < value.length; j++) {
                                if (j != value.length - 1) {
                                    content += value[j] + ",";
                                } else content += value[j];
                            }
                            int num = reverse_group_list.get(gi).get(content);*/
                            t_cost = dis / num;
                            break;
                        }
                    }
                    if (t_cost < minCOST) {
                        minCOST = t_cost;
                        mingi = gi;
                    }
                }

                if (mingi != -1)
                    groupList.get(mingi).put(outlierT.tupleID, outlierT);//把outlierT加到最相似的group中
            }
        }

        //analysis for experiment--true outlier added
        try {
            OutlierAnalyse.writeGroupsToFile(Domain_to_Groups, data_args + "/total_group_info_after_merge_"+threshold+".txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    class Tuple2 {
        String content;
        int tupleID;

        Tuple2(int tupleID, String content) {
            this.content = content;
            this.tupleID = tupleID;
        }
    }

    public int replaceNCost(String B, ArrayList<Tuple2> list) { //replace A in list -> B cost N
        //that is to compute how many tuples differ from B
        int N = 0;
        for (int i = 0; i < list.size(); i++) {
            String A = list.get(i).content;
            if (!B.equals(A)) {
                N++;
            } else continue;
        }
        return N;
    }

    public int distanceCost(String[] A, String[] B) {
        Cosine cosine = new Cosine();
        int distance = -1;
        if (A.length != B.length) {
            System.err.println("Error: A != B");
        } else {
            distance = 0;
            for (int i = 0; i < A.length; i++) {
                distance += SpellChecker.distance(A[i], B[i]);
            }
        }

        return distance;
    }

    public HashMap<String, Candidate> spellCheck(HashMap<Integer, Tuple> group) {//返回每个tuple被替换的最小cost
        //System.out.println("--------spellCheck-------");

        HashMap<String, Candidate> cMap = new HashMap<>();
        ArrayList<Tuple2> tupleList = new ArrayList<>();
        Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();

        //put tuples into ArrayList
        while (iter.hasNext()) {
            Entry<Integer, Tuple> current = iter.next();
            Tuple tuple = current.getValue();
            int tupleID = current.getKey();
            int length = tuple.getContext().length;
            String[] content = new String[length];
            System.arraycopy(tuple.getContext(), 0, content, 0, length);
            Arrays.sort(content);
            String t = Arrays.toString(content);
//			String t = Arrays.toString(content).replaceAll("\\[","").replaceAll("]","");
            tupleList.add(new Tuple2(tupleID, t));
        }

        for (int i = 0; i < tupleList.size(); i++) {
            Tuple2 t = tupleList.get(i);
            String tuple = t.content;
            double dis = 1000;
            String candidate = tuple;
            //找到该Tuple的最小distance\
            int tupleID = t.tupleID;

            Cosine cosine = new Cosine();

            for (int j = 0; j < tupleList.size(); j++) {
                Tuple2 tuple2 = tupleList.get(j);
                String tmp_candidate = tuple2.content;
                //tupleID = tuple2.tupleID;
                if (tuple.equals(tmp_candidate)) continue;

                /*MetricLCS lcs = new MetricLCS();
                NormalizedLevenshtein l = new NormalizedLevenshtein();
                JaroWinkler jw = new JaroWinkler();
                QGram qGram = new QGram();*/
                double distance = SpellChecker.distance(tuple, tmp_candidate);
                int N = replaceNCost(tmp_candidate, tupleList);
                double tmp_cost = distance * N;
                if (tmp_cost < dis) {
                    tupleID = tuple2.tupleID;
                    dis = tmp_cost;
                    candidate = tmp_candidate;
                    //tupleID = tupleList.get(j).tupleID;
                }
            }
//			if(tupleID == -1){
//				tupleID = tupleList.get(0).tupleID;
//				candidate = tupleList.get(0).content;
//				cost = 0;
//			}
            cMap.put(tuple, new Candidate(tupleID, candidate, dis));
            //System.out.println("tupleID = "+tupleID+"; "+tuple+" -> "+candidate);
        }
        return cMap;
    }


    /**
     * 根据MLN的概率修正错误数据
     */
    public void correctByMLN(List<List<HashMap<Integer, Tuple>>> Domain_to_Groups, List<HashMap<String, Double>> attributesPROBList, String[] header,
                             List<HashMap<Integer, Tuple>> domains, List<HashMap<String,ArrayList<Integer>>> list) throws IOException {
        int DGindex = 0;

        for (List<HashMap<Integer, Tuple>> groups : Domain_to_Groups) {
			System.out.println("---------------"+(DGindex+1)+"--------------------");

            for (int i = 0; i < groups.size(); i++) {
                HashMap<Integer, Tuple> group = groups.get(i);
                HashMap<Integer, Integer> weight = new HashMap<>();

                HashMap<String, Candidate> cMap = spellCheck(group);

                for (int t = 0; t < attributesPROBList.size(); t++) {
                    HashMap<String, Double> attributesPROB = attributesPROBList.get(t);

                    Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();
                    double pre_cost = MAX_NEGNATIVE;
                    int tupleID = 0;

                    //遍历group
                    while (iter.hasNext()) {
                        Entry<Integer, Tuple> current = iter.next();
                        Tuple tuple = current.getValue();

                        /*if(tuple.tupleID==457 && Arrays.toString(tuple.getContext()).contains("2563864556")){
                            System.out.println("debug here");
                        }*/

                        int length = tuple.getContext().length;
                        String[] tmp_context = new String[length];
                        System.arraycopy(tuple.getContext(), 0, tmp_context, 0, length);
                        Arrays.sort(tmp_context);
                        String values = Arrays.toString(tmp_context);

                        Double prob = attributesPROB.get(values);
                        Candidate c = cMap.get(values);
                        String candidate = c.candidate;
                        if (prob == null) {
                            prob = MIN_DOUBLE;    //说明这个团只在数据集里出现过一次，姑且认为该团不具代表性，概率=MIN_DOUBLE
                        }
                        double cost;
                        if (c.cost == 0) {
                            cost = MIN_DOUBLE;
                        } else{
                            cost = prob * c.cost;
                        }
                        if (cost > pre_cost) {
                            pre_cost = cost;
                            tupleID = tuple.tupleID;
//                            tupleID = c.tupleID;
                        }
                    }
                    if (weight.get(tupleID) == null)
                        weight.put(tupleID, 1);
                    else {
                        weight.put(tupleID, weight.get(tupleID) + 1);
                    }
                }
                // 如何找出作为基准的tupleID
                int resultNum = 0;
                int resultTupleID = 0;

                for (Entry<Integer, Integer> entry : weight.entrySet()) {
                    int num = entry.getValue();
                    if (num > resultNum) {
                        resultNum = num;
                        resultTupleID = entry.getKey();
                    }
                }

                // tupleID 是被选择作为干净数据的，这里要用到
                Tuple cleanTuple = group.get(resultTupleID);
                Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();
                //修正错误的值，即用正确的去替换它
                if (cleanTuple != null) {
                    while (iter.hasNext()) {
                        Entry<Integer, Tuple> current = iter.next();
                        Tuple copyT = cleanTuple;
//                        Tuple copyT = copyTuple(cleanTuple);
//                        copyT.tupleID = current.getKey();
                        group.put(current.getKey(), copyT);
                        domains.get(DGindex).put(current.getKey(), copyT);
                    }
                }
            }
            DGindex++;
        }

        //存储修正后的Group结果
        System.out.println("\n=======After Correct Values By MLN Probability=======");
        for (List<HashMap<Integer, Tuple>> groups : Domain_to_Groups) {
            HashMap<String,ArrayList<Integer>> groupValueMap = storeGroup(groups);
            list.add(groupValueMap);
        }
    }

    public static HashMap<String, ArrayList<Integer>> storeGroup(List<HashMap<Integer, Tuple>> groups) {
        HashMap<String, ArrayList<Integer>> map = new HashMap<>();
        for (int i = 0; i < groups.size(); i++) {
            HashMap<Integer, Tuple> group = groups.get(i);
            Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, Tuple> entry = iter.next();
                Integer tupleID = entry.getKey();
                Tuple t = entry.getValue();
                String[] tupleContext = t.getContext();
                String tuple_content = "";
                for (int k = 0; k < tupleContext.length; k++) {
                    tuple_content += tupleContext[k];
                    if (k != tupleContext.length - 1) {
                        tuple_content += ",";
                    }
                }
                if (map.get(tuple_content) == null) {
                    ArrayList<Integer> tupleIDList = new ArrayList<>();
                    tupleIDList.add(tupleID);
                    map.put(tuple_content, tupleIDList);
                } else {
                    map.get(tuple_content).add(tupleID);
                }
            }
        }
        return map;
    }

    public static Tuple copyTuple(Tuple t1) {
        Tuple t2 = new Tuple();
        t2.setContext(t1.getContext());
        t2.AttributeIndex = t1.AttributeIndex;
        t2.AttributeNames = t1.getAttributeNames();
        t2.reason = t1.reason;
        t2.result = t1.result;
        t2.reasonAttributeIndex = t1.reasonAttributeIndex;
        t2.resultAttributeIndex = t1.resultAttributeIndex;
        return t2;
    }

    /**
     * 删除重复数据
     */
    public void deleteDuplicate(List<List<Integer>> keyList_list, HashMap<Integer, String[]> dataSet) {
//		System.out.println("\tDuplicate keys: ");
        for (List<Integer> keyList : keyList_list) {
            if (keyList == null) continue;
            for (int i = 0; i < keyList.size() - 1; i++) {
                int key1 = keyList.get(i);
                String[] pre_tuple = dataSet.get(key1);
//				System.out.print("\tGROUP "+i+":"+key1+" ");
                for (int j = i + 1; j < keyList.size(); j++) {
                    int key2 = keyList.get(j);
                    String[] curr_tuple = dataSet.get(key2);
                    if (Arrays.toString(pre_tuple).equals(Arrays.toString(curr_tuple))) { //之所以要比较是因为有些属性没有出现，可能不同
//						System.out.print(key2+" ");
                        dataSet.remove(key2);
                    }
                }
//				System.out.println();
            }
        }
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static int[] concat(int[] first, int[] second) {
        int[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


    /**
     * 合并两个Domain中的tuple部分，它们原属于同一个Tuple
     */

    public Tuple combineTuple(Tuple t1, Tuple t2, int[] sameID) {

        Tuple t = new Tuple();
        // 分是否存在相同的属性
        if (sameID.length == 0 || (sameID.length > 0 && sameID[0] == -1)) { //不存在相同的属性
            int[] attributeIndex = concat(t1.getAttributeIndex(), t2.getAttributeIndex());
            String[] TupleContext = concat(t1.getContext(), t2.getContext());
            t.setContext(TupleContext);
            t.setAttributeIndex(attributeIndex);
        } else { //存在相同的属性

            int[] t1IDs = t1.getAttributeIndex();
            int[] t2IDs = t2.getAttributeIndex();

            // 求得 sameIDlength 的长度
            int sameIDlength = 0;
            for (int j = 0; j < sameID.length; j++) {
                if (sameID[j] == -1) break;
                sameIDlength++;
            }


            // 新的 attributeindex
            int[] attributeIndex = Arrays.copyOf(t1IDs, t1IDs.length + t2IDs.length - sameIDlength);
            //新的tuplecontext
            String[] t1context = t1.getContext();
            String[] t2context = t2.getContext();
            String[] TupleContext = Arrays.copyOf(t1context, attributeIndex.length);

            int k = t1IDs.length;

            for (int i = 0; i < t2IDs.length; i++) {
                boolean flag = false;
                for (int j = 0; j < t1IDs.length; j++) {
                    if (t2IDs[i] == t1IDs[j]) {
                        flag = true;
                        break;
                    }
                }
                if (flag) continue; // 对于当前 t2 元组的属性，t1 中有和他一样的属性
                attributeIndex[k] = t2IDs[i];
                TupleContext[k] = t2context[i];
                k++;
            }


//			for(int i=0; i<t2IDs.length; i++){
//				for(int ID: sameID){
//					if(ID==-1)break;//初始化为-1，即已遍历完有值的部分
//					if(t2IDs[i]==ID)continue;
//					attributeIndex[k] = t2IDs[i];
//					TupleContext[k++] = t2context[i];
//				}
//			}
            t.setContext(TupleContext);
            t.setAttributeIndex(attributeIndex);
        }
        return t;
    }


    /**
     * 找到两个数组的交集
     */
    public static int[] findSameID(int[] IDs1, int[] IDs2) {
        int[] sameID = null;
        //初始化
        if (IDs1.length > IDs2.length)
            sameID = new int[IDs2.length];
        else
            sameID = new int[IDs1.length];
        for (int i = 0; i < sameID.length; i++)
            sameID[i] = -1;

        //findSameID
        int[] newIDs1 = Arrays.copyOfRange(IDs1, 0, IDs1.length);
        int[] newIDs2 = Arrays.copyOfRange(IDs2, 0, IDs2.length);
        Arrays.sort(newIDs1);
        Arrays.sort(newIDs2);
        int i = 0, j = 0, index = 0;
        while (i < newIDs1.length && j < newIDs2.length) {
            if (newIDs1[i] == newIDs2[j]) {
                sameID[index++] = newIDs1[i];
                ++i;
                ++j;
            } else if (newIDs1[i] < newIDs2[j])
                ++i;
            else
                ++j;
        }
        return sameID;
    }

    /**
     * 比较两个元组Tuple1和Tuple2中是否存在相同属性，但值不同
     */


    public static boolean checkConflict(Tuple t1, Tuple t2, int[] sameID) {
        boolean result = false;


        String[] context1 = t1.getContext();
        String[] context2 = t2.getContext();

        int[] attributeID1 = t1.getAttributeIndex();
        int[] attributeID2 = t2.getAttributeIndex();


        HashMap<Integer, String> map1 = new HashMap<Integer, String>(context1.length);    // MAP<Attribute name,Attribute value>
        HashMap<Integer, String> map2 = new HashMap<Integer, String>(context2.length);


        // 把对应的值放到map 里

        for (int i = 0; i < context1.length; i++)
            map1.put(attributeID1[i], context1[i]);


        for (int i = 0; i < context2.length; i++)
            map2.put(attributeID2[i], context2[i]);


        Iterator<Entry<Integer, String>> iter = null;

        if (context1.length < context2.length) {
            int i = 0;
            iter = map1.entrySet().iterator();

            while (iter.hasNext()) {
                Entry<Integer, String> entry = iter.next();
                int tupleID = entry.getKey(); // header 的索引

                String value1 = entry.getValue();
                String value2 = map2.get(tupleID);

                if (value2 == null) continue; // 说明 tuple2 中没有该属性
                sameID[i++] = tupleID; // 记录相同的属性

                if (!value1.equals(value2)) {//存在冲突
                    result = true;
                    break;
                }

            }

        } else {
            int i = 0;
            iter = map2.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, String> entry = iter.next();
                int tupleID = entry.getKey();
                String value1 = entry.getValue();
                String value2 = map1.get(tupleID);
                if (value2 == null) continue;
                sameID[i++] = tupleID;
                if (!value1.equals(value2)) {//存在冲突
                    result = true;
                    break;
                }
            }
        }
        return result;
    }


    public HashMap<Integer, Tuple> combineGroup(List<Integer> keyList, HashMap<Integer, Tuple> group1, HashMap<Integer, Tuple> group2, int Domain1, int Domain2) {

        HashMap<Integer, Tuple> newGroup = new HashMap<>(group1.size()); // new group 的大小肯定不会超过其中任意一个group 的大小

        for (int ki = 0; ki < keyList.size(); ) {

            int key = keyList.get(ki);
            Tuple t1 = group1.get(key);
            Tuple t2 = group2.get(key);

            ConflictTuple ct1 = new ConflictTuple(t1);
            ConflictTuple ct2 = new ConflictTuple(t2);

            int[] sameID = new int[t1.getAttributeIndex().length];

            for (int i = 0; i < sameID.length; i++) {
                sameID[i] = -1;
            }
            if (!checkConflict(t1, t2, sameID)) { //不冲突
                newGroup.put(key, combineTuple(t1, t2, sameID));
                ki++;
            } else {
                /*System.out.println("Conflict:");
                System.out.println("\tcontext1 = "+Arrays.toString(group1.get(key).TupleContext));
				System.out.println("\tcontext2 = "+Arrays.toString(group2.get(key).TupleContext));*/

                // 把group1 和 group2 中的元组都去掉？
                group1.remove(key);
                group2.remove(key);
                //System.out.println("\tID = "+key);

//				for(int m = 0;m<keyList.size();m++){
//					System.out.println(keyList.get(m));
//				}

                // 把keylist 中的ki 对应的元组都去掉
                keyList.remove(ki);

                //根据DomainID将冲突的Tuple记录下来
                // 元组将冲突的属性记录下来
//                ct1.setConflictIDs(sameID);
//                ct2.setConflictIDs(sameID);
                // 记录所在的domain
//                ct1.domainID = Domain1;
//                ct2.domainID = Domain2;

                // 冲突的元组数
//                addConflict(key, ct1);
//                addConflict(key, ct2);
                conflicts.add(key);


                // 每个元组都是冲突的，return null
                if (keyList.isEmpty()) {
                    return null;
                }

            }
        }
        return newGroup;
    }


    /*public void addConflict(int tupleID, ConflictTuple t) {
        Conflicts oldct = conflicts.get(tupleID);
        if (null == oldct) {    //该Domain中尚未添加冲突元组
            Conflicts newct = new Conflicts();
            newct.tuples.add(t);
            conflicts.put(tupleID, newct);
        } else {
            oldct.tuples.add(t);
        }
    }*/

    /**
     * 根据key值求两个group的交集
     */
    public static List<Integer> interset(Integer[] a1, Integer[] a2) {
        int len1 = a1.length;
        int len2 = a2.length;
        int len = a1.length + a2.length;
        Map<Integer, Integer> m = new HashMap<Integer, Integer>(len);

        List<Integer> ret = new ArrayList<Integer>();

        for (int i = 0; i < len1; i++) {
            if (m.get(a1[i]) == null)
                m.put(a1[i], 1);
        }

        for (int i = 0; i < len2; i++) {
            if (m.get(a2[i]) != null)
                m.put(a2[i], 2);
        }

        for (Entry<Integer, Integer> e : m.entrySet()) {
            if (e.getValue() == 2) {
                ret.add(e.getKey());
            }
        }

        return ret;
    }


    /**
     * 返回该group的所有key值
     */
    public static Integer[] calculateKeys(HashMap<Integer, Tuple> group) {
        Integer[] keys = new Integer[group.size()];
        Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();
        int i = 0;
        while (iter.hasNext()) {
            Entry<Integer, Tuple> entry = (Entry<Integer, Tuple>) iter.next();
            keys[i++] = entry.getKey();
        }
        return keys;
    }

    public boolean ifSameValue(int[] conflictIDs, Tuple t, Tuple ct) {
        boolean result = false;
        int count = 0;
        int i = 0;
        int ti = 0, cti = 0;
        for (; i < conflictIDs.length && conflictIDs[i] != -1; i++) {
            while (ti < t.AttributeIndex.length) {
                if (t.AttributeIndex[ti] == conflictIDs[i]) {
                    break;
                } else ti++;
            }
            while (cti < ct.AttributeIndex.length) {
                if (ct.AttributeIndex[cti] == conflictIDs[i]) {
                    break;
                } else cti++;
            }
            if (cti == ct.AttributeIndex.length || ti == t.AttributeIndex.length) return false;
            if (t.TupleContext[ti].equals(ct.TupleContext[cti])) count++;
        }
        if (count == i) result = true;
        return result;
    }

    public boolean ifSameValue(int[] conflictIDs, Tuple t, ConflictTuple ct) {
        boolean result = false;
        int count = 0;
        int i = 0;
        int ti = 0, cti = 0;
        for (; i < conflictIDs.length && conflictIDs[i] != -1; i++) {
            while (ti < t.AttributeIndex.length) {
                if (t.AttributeIndex[ti] == conflictIDs[i]) {
                    break;
                } else ti++;
            }
            while (cti < ct.AttributeIndex.length) {
                if (ct.AttributeIndex[cti] == conflictIDs[i]) {
                    break;
                } else cti++;
            }
            if (cti == ct.AttributeIndex.length || ti == t.AttributeIndex.length) return false;
            if (t.TupleContext[ti].equals(ct.TupleContext[cti])) count++;
        }
        if (count == i) result = true;
        return result;
    }

    /**
     * 为冲突元组找到候选的替换方案(new)
     */
    public void findCandidate(ArrayList<Integer> conflicts, List<HashMap<Integer, Tuple>> domains, HashMap<String, Double> attributesPROB, ArrayList<Integer> ignoredIDs) {

        for (Integer id : conflicts) {
            double prob = -1;
            Tuple fixTuple = new Tuple();
            boolean ischange = false;

            for (int k = 0; k < domains.size(); k++) {
                Tuple candidateTuple = new Tuple();
                ArrayList<String[]> tmp_list = new ArrayList<>();//记录该冲突元组修复方案中对每一个domain的选择
                HashMap<Integer, Tuple> first_domain = domains.get(k);
                Tuple ct = first_domain.get(id);
                int tupleID = ct.tupleID;

                tmp_list.add(ct.getContext());

                if (null != ct) {
                    Tuple combinedTuple = ct;
                    for (int i = 0; i < domains.size(); i++) {  //还需加一个限制，排除base Domain
                        if (i == k) {
                            continue;
                        }
                        HashMap<Integer, Tuple> domain = domains.get(i);
                        Tuple firstTuple = domain.entrySet().iterator().next().getValue();

                        int[] sameID = findSameID(firstTuple.AttributeIndex, combinedTuple.AttributeIndex);//排序过
                        if (sameID[0] != -1) {//说明有交集的属性
                            Iterator<Entry<Integer, Tuple>> iter = domain.entrySet().iterator();
                            double maxProb = MIN_DOUBLE;
                            Tuple maxTuple = null;
                            while (iter.hasNext()) {
                                Entry<Integer, Tuple> en = iter.next();
                                Tuple t = en.getValue();
                                //说明属性值也相同
                                if (ifSameValue(sameID, t, combinedTuple)) {  //ifContains(sameID, t.AttributeIndex) &&

                                    String[] tmp_context = new String[t.getContext().length];
                                    System.arraycopy(t.getContext(), 0, tmp_context, 0, t.getContext().length);
                                    Arrays.sort(tmp_context);
                                    String value = Arrays.toString(tmp_context);
                                    Double curr_prob = attributesPROB.get(value);

                                    if (curr_prob == null) {
                                        curr_prob = MIN_DOUBLE;
                                    }
                                    if (curr_prob > maxProb) {
                                        maxProb = curr_prob;
                                        maxTuple = t;
                                    }
                                    /*System.err.println("3.current maxTuple = " + Arrays.toString(maxTuple.TupleContext));
                                    System.err.println("attribute id = " + Arrays.toString(maxTuple.AttributeIndex));*/
                                }
                            }
                            if (null != maxTuple) {
                                tmp_list.add(maxTuple.getContext());
                                combinedTuple = combineTuple(combinedTuple, maxTuple, sameID);

                                /*System.err.println("3.combinedTuple = " + Arrays.toString(combinedTuple.TupleContext));
                                System.err.println("attribute id = " + Arrays.toString(combinedTuple.AttributeIndex));*/
                            } else {
//                                System.out.println("debug here");
                            }
                        } else {//没有交集，直接合并
                            Tuple t = domain.get(tupleID);
                            combinedTuple = combineTuple(combinedTuple, t, sameID);
                            tmp_list.add(t.getContext());

                            /*System.err.println("4.combinedTuple = " + Arrays.toString(combinedTuple.TupleContext));
                            System.err.println("attribute id = " + Arrays.toString(combinedTuple.AttributeIndex));*/

                        }
                    }
                    candidateTuple = combinedTuple;
                } else {
                    System.out.println("Can't find candidate for conflict tuple");
                }

                double tmp_prob = 1;
                for (String[] value : tmp_list) {

                    String[] tmp_context = new String[value.length];
                    System.arraycopy(value, 0, tmp_context, 0, value.length);
                    Arrays.sort(tmp_context);
                    Double curr_prob = attributesPROB.get(Arrays.toString(tmp_context));
                    if (null == curr_prob) {
                        curr_prob = MIN_DOUBLE;
                    }
                    tmp_prob *= curr_prob;
                }
                String[] old_tuple_part = new String[candidateTuple.AttributeIndex.length];
                for (int i = 0; i < candidateTuple.AttributeIndex.length; i++) {
                    int index = candidateTuple.AttributeIndex[i];
                    old_tuple_part[i] = dataSet.get(id)[index];
                }
                double dis = distanceCost(candidateTuple.getContext(), old_tuple_part);
                if (dis == 0) {
                    dis = MIN_DOUBLE;
                }
                tmp_prob /= dis;
                if (tmp_prob > prob) {
                    ischange = true;
                    prob = tmp_prob;
                    fixTuple = candidateTuple;
//                    System.out.print("P = " + prob + " , " + Arrays.toString(fixTuple.getContext()) + "\n");
                }
            }

            //修改dataset中这一条的数据
            if (ischange) {
                String[] ignoredValues = null;
                if (ignoredIDs.size() > 0) {
                    ignoredValues = new String[ignoredIDs.size()];
                    for (int i = 0; i < ignoredIDs.size(); i++) {
                        ignoredValues[i] = dataSet.get(id)[ignoredIDs.get(i)];
                    }
                }

                String[] newTupleContext = new String[header.length];
                for (int i = 0; i < header.length; i++) {
                    if (ignoredIDs.size() > 0) {
                        for (int j = 0; j < ignoredIDs.size(); j++) {
                            if (i == ignoredIDs.get(j)) {
                                newTupleContext[i] = ignoredValues[j];
                            }
                        }
                    }
                    for (int k = 0; k < fixTuple.AttributeIndex.length; k++) {
                        if (i == fixTuple.AttributeIndex[k]) {
                            newTupleContext[i] = fixTuple.TupleContext[k];
                        }
                    }
                }
                String[] old_tuple = dataSet.get(id);
                for (int index = 0; index < newTupleContext.length; index++) {
                    if (null == newTupleContext[index]) {
                        newTupleContext[index] = old_tuple[index];
                    }
                }
                dataSet.put(id, newTupleContext);
//                System.out.println("ID = [" + id + "] fix Tuple = " + Arrays.toString(newTupleContext));
//                System.out.println("fix content = " + Arrays.toString(fixTuple.getContext()));
//                System.out.println("attribute id = " + Arrays.toString(fixTuple.AttributeIndex));
            }

        }
        System.out.println();
    }

    /**
     * 为冲突元组找到候选的替换方案(old)
     */
    public void findCandidate(HashMap<Integer, Conflicts> conflicts, List<List<HashMap<Integer, Tuple>>> Domain_to_Groups, List<HashMap<Integer, Tuple>> domains, List<HashMap<String, Double>> attributesPROBList, ArrayList<Integer> ignoredIDs) {

        Tuple candidateTuple = new Tuple();
        //根据冲突元组，按不同的组合形成候选的修正方案

        Iterator<Entry<Integer, Conflicts>> conflict_iter = conflicts.entrySet().iterator();

        while (conflict_iter.hasNext()) {
            Boolean ischange = false;

            Entry<Integer, Conflicts> entry = conflict_iter.next();
            int tupleID = entry.getKey();
            Conflicts conf = entry.getValue();
            List<ConflictTuple> list = conf.tuples;

            HashMap<Tuple, Integer> weight = new HashMap<>();

            for (int tt = 0; tt < attributesPROBList.size(); tt++) {

                HashMap<String, Double> attributesPROB = attributesPROBList.get(tt);

                for (ConflictTuple ct : list) {

                    int[] conflictIDs = ct.conflictIDs;
                    int conflictDomainID = ct.domainID;
                    int length = Domain_to_Groups.size();
                    boolean[] flag = new boolean[length];
                    Tuple combinedTuple = ct;
                    //去匹配下一个区域的元组值
                    for (int i = 0; i < length; i++) {
                        if (flag[i] || i == conflictDomainID) continue;
                        HashMap<Integer, Tuple> domain = domains.get(i);
                        Tuple firstTuple = domain.entrySet().iterator().next().getValue();
                        int[] sameID = findSameID(firstTuple.AttributeIndex, combinedTuple.AttributeIndex);
                        if (sameID[0] != -1) { //说明有交集的属性
                            List<HashMap<Integer, Tuple>> cur_groups = Domain_to_Groups.get(i);
                            for (HashMap<Integer, Tuple> group : cur_groups) {
                                Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();
                                while (iter.hasNext()) {
                                    Entry<Integer, Tuple> en = iter.next();
                                    Tuple t = en.getValue();
                                    if (ifContains(sameID, t.AttributeIndex) && ifSameValue(sameID, t, ct)) {
                                        flag[i] = true;
                                        combinedTuple = combineTuple(combinedTuple, t, sameID);

                                        //System.err.println("1.combinedTuple = " + Arrays.toString(combinedTuple.TupleContext));
                                    /*for (int a = 0; a < combinedTuple.TupleContext.length; a++) {
                                        if (combinedTuple.TupleContext[a] == null) {
											System.err.println("debug here 1");
										}
									}*/
                                        break;
                                    }
                                }
                                if (flag[i]) break;
                            }
                        } else {
                            combinedTuple = combineTuple(combinedTuple, domain.get(tupleID), sameID);
                            //System.err.println("2.combinedTuple = " + Arrays.toString(combinedTuple.TupleContext));

						/*for (int a = 0; a < combinedTuple.TupleContext.length; a++) {
                            if (combinedTuple.TupleContext[a] == null) {
								System.err.println("debug here 2");
							}
						}*/
                            flag[i] = true;
                        }
                    }

                    //若group中匹配不到，则去Domain中匹配
                    for (int fi = 0; fi < flag.length; fi++) {
                        //System.out.println(Arrays.toString(flag));
                        if (flag[fi] || fi == conflictDomainID) continue;
                        HashMap<Integer, Tuple> domain = domains.get(fi);
                        Tuple firstTuple = domain.entrySet().iterator().next().getValue();

                        int[] sameID = findSameID(firstTuple.AttributeIndex, combinedTuple.AttributeIndex);
                        if (sameID[0] != -1) {
                            Iterator<Entry<Integer, Tuple>> iter = domain.entrySet().iterator();
                            while (iter.hasNext()) {
                                Entry<Integer, Tuple> en = iter.next();
                                Tuple t = en.getValue();
                                if (ifContains(sameID, t.AttributeIndex) && ifSameValue(sameID, t, ct)) {
                                    flag[fi] = true;
                                    combinedTuple = combineTuple(combinedTuple, t, sameID);
                                    System.err.println("3.combinedTuple = " + Arrays.toString(combinedTuple.TupleContext));
                                    for (int a = 0; a < combinedTuple.TupleContext.length; a++) {
                                        if (combinedTuple.TupleContext[a] == null) {
                                            System.err.println("debug here 3");
                                        }
                                    }
                                    break;
                                }
                            }
                            if (flag[fi]) break;
                        } else {
                            combinedTuple = combineTuple(combinedTuple, domain.get(tupleID), sameID);
                            System.err.println("4.combinedTuple = " + Arrays.toString(combinedTuple.TupleContext));
                            for (int a = 0; a < combinedTuple.TupleContext.length; a++) {
                                if (combinedTuple.TupleContext[a] == null) {
                                    System.err.println("debug here 4");
                                }
                            }
                            flag[fi] = true;
                        }

                    }
                    //System.out.println(Arrays.toString(flag));
                    int count = 0;
                    for (int i = 0; i < flag.length; i++)
                        if (flag[i] == false) count++;

                    if (count == 1) {
                        String[] content = combinedTuple.TupleContext;
                        int[] contentID = combinedTuple.AttributeIndex;

                        //计算该tuple的probability
                        String[] tmp_context = new String[content.length];
                        System.arraycopy(content, 0, tmp_context, 0, content.length);
                        Arrays.sort(tmp_context);
                        String value = Arrays.toString(tmp_context);
                        Double prob = attributesPROB.get(value);
                        if (prob == null) prob = 0.0;
//					for (int j = 0; j < length; j++) {
//						String result = header[contentID[j]] + "(" + content[j] + ")";
//						Double tmpProb = attributesPROB.get(result);
//						if (tmpProb == null) continue;//表明该result的概率为1
//						prob *= tmpProb;
//					}
                        if (prob > candidateTuple.probablity) {
                            ischange = true;
                            candidateTuple = combinedTuple;
                            candidateTuple.probablity = prob;
                        }

                    }
                }
                if (weight.get(candidateTuple) == null) weight.put(candidateTuple, 1);
                else weight.put(candidateTuple, weight.get(candidateTuple) + 1);

            }
            int resultNum = 0;
            Tuple resultTuple = null;
            for (Entry<Tuple, Integer> entryy : weight.entrySet()) {
                int num = entryy.getValue();
                if (num > resultNum) {
                    resultNum = num;
                    resultTuple = entryy.getKey();
                }
            }
            candidateTuple = resultTuple;

            //修改dataset中这一条的数据
            if (ischange) {

                String[] ignoredValues = null;
                if (ignoredIDs.size() > 0) {
                    ignoredValues = new String[ignoredIDs.size()];
                    for (int i = 0; i < ignoredIDs.size(); i++) {
                        ignoredValues[i] = dataSet.get(tupleID)[ignoredIDs.get(i)];
                    }
                }

                String[] newTupleContext = new String[header.length];
                for (int i = 0; i < header.length; i++) {
                    if (ignoredIDs.size() > 0) {
                        for (int j = 0; j < ignoredIDs.size(); j++) {
                            if (i == ignoredIDs.get(j)) {
                                newTupleContext[i] = ignoredValues[j];
                            }
                        }
                    }
                    for (int k = 0; k < candidateTuple.AttributeIndex.length; k++) {
                        if (i == candidateTuple.AttributeIndex[k]) {
                            newTupleContext[i] = candidateTuple.TupleContext[k];
                        }
                    }
                }
                String[] old_tuple = dataSet.get(tupleID);
                for (int index = 0; index < newTupleContext.length; index++) {
                    if (null == newTupleContext[index]) {
                        newTupleContext[index] = old_tuple[index];
                    }
                }
                dataSet.put(tupleID, newTupleContext);
                System.out.println("\ntupleID = [" + tupleID + "] candidate Tuple = " + Arrays.toString(newTupleContext));
            }
        }
    }


    public List<List<Integer>> combineDomain(List<List<HashMap<Integer, Tuple>>> Domain_to_Groups) {
        List<List<Integer>> keysList = null;
        //只有一个Domain，不需要合并
        if (Domain_to_Groups.size() > 1) {
            List<HashMap<Integer, Tuple>> pre_groups = Domain_to_Groups.get(0);
            keysList = new ArrayList<>(pre_groups.size());

            int preDomainID = 0;
            int curDomainID = 0;


            //第一个Domain中所有group的keyList
            //	for(HashMap<Integer, Tuple> group: pre_groups){
            //		keysList.add(Arrays.asList(calculateKeys(group)));
            //	}

//		for(List<Integer> list :keysList){
//			for(Integer output :list)
//				System.out.print(output);
//				System.out.println();
//		}

            for (int i = 1; i < Domain_to_Groups.size(); i++) {

                curDomainID = i;
                List<HashMap<Integer, Tuple>> cur_groups = Domain_to_Groups.get(i);
                int pre_groups_index = 0;
                int pre_groups_size = pre_groups.size();
                int cur_groups_index = 0;
                int cur_groups_size = cur_groups.size();
                List<HashMap<Integer, Tuple>> temp = new ArrayList<HashMap<Integer, Tuple>>();
                while (pre_groups_index < pre_groups_size && cur_groups_index < cur_groups_size) {
                    HashMap<Integer, Tuple> cur_group = cur_groups.get(cur_groups_index);
                    HashMap<Integer, Tuple> pre_group = pre_groups.get(pre_groups_index);
                    //求两个group的交集
                    List<Integer> keyList = interset(calculateKeys(pre_group), calculateKeys(cur_group));
                    if (keyList.size() != 0) {
                        pre_group = combineGroup(keyList, pre_group, cur_group, preDomainID, curDomainID);
                        if (keyList.size() > 1) {
                            keysList.add(keyList);
                            temp.add(pre_group);
                        }
                    }
                    cur_groups_index++;// 如果当前group与前一个 domain 的group没有交集，那么进行下一个group的匹配
                    //如果当前domain 已经没有 可以用来 匹配的 group 的话 ，那么 就匹配前一个 domain 中的下一个group
                    if (cur_groups_index == cur_groups_size) {
                        cur_groups_index = 0;
                        pre_groups_index++;
                    }
                }
                pre_groups = temp;
                preDomainID = i;
            }
        }


        //===========test==========
//		int d_index = 0;
//		for(List<HashMap<Integer, Tuple>> groups: Domain_to_Groups){
//			System.out.println("\n*******Domain "+(++d_index)+"*******");
//			printGroup(groups);
//		}
        //===========test==========


        System.out.println(">>> Fix the error values...");
        int i = 0;
        for (List<HashMap<Integer, Tuple>> groups : Domain_to_Groups) {
            i++;
            if (i == 9) {
                System.out.println("debug here");
            }
            for (HashMap<Integer, Tuple> group : groups) {
                Iterator<Entry<Integer, Tuple>> iter = group.entrySet().iterator();
                //修正错误的值，即用正确的去替换它
                while (iter.hasNext()) {
                    Entry<Integer, Tuple> entry = iter.next();
                    int currentKey = entry.getKey();
                    Tuple cleanTuple = entry.getValue();
                    int[] AttrIDs = cleanTuple.getAttributeIndex();
                    String[] values = cleanTuple.getContext();
                    //   String[] tuple = null;
                    String[] tuple = dataSet.get(currentKey);
                    for (int k = 0; k < AttrIDs.length; k++) {
                        //tuple = dataSet.get(currentKey);
                        tuple[AttrIDs[k]] = values[k];
                    }
                    dataSet.put(currentKey, tuple);
                }
            }
        }
        System.out.println(">>> Completed!");
        return keysList;
    }


    /**
     * 标记重复数据
     * */
    /*
    public List<List<Integer>> checkDuplicate(List<List<HashMap<Integer, Tuple>>> Domain_to_Groups){

		List<List<Integer>> keyList_list = new ArrayList<List<Integer>>();
		int round = 0;

		if(Domain_to_Groups.size()<2){	//不存在重复数据
			return keyList_list;
		}

		for(List<HashMap<Integer, Tuple>> groups: Domain_to_Groups){
			if(round == 0){	//第一次循环,标记第一个Domain的groups中的重复数据
				for(HashMap<Integer, Tuple> group: groups){
					List<Integer> keyList = new ArrayList<Integer>();
					for (int key: group.keySet()) {
						//save keys
						keyList.add(key);
					}
					keyList_list.add(keyList); 	//第一个Domain中，有多少个group 就有多少个keyList_list
				}
				round++;
			}else{

				for(int k=0;k<keyList_list.size();k++){
					List<Integer> keyList = keyList_list.get(k);
					List<Integer> newList = new ArrayList<Integer>();
					for(int i=0;i<keyList.size();i++){
						int key = keyList.get(i);

						for(int index = 0;index < groups.size();index++){
							HashMap<Integer, Tuple> group = groups.get(index);
							if(group.containsKey(key)){
								newList.add(key);
								break;
							}else continue;
						}
					}
					if(newList.size()==0){
						keyList_list.remove(keyList);
					}else{
						keyList_list.set(k, newList);
					}
				}
			}
		}
		//合并完所有区域
		return keyList_list;
	}

	*/

    /**
     * 根据属性列的编号ID和当前遍历到的Tuple，获得对应的属性值values
     *
     * @return values
     */

    public static String[] getValues(Tuple t, int[] attributeIDs) {
        int length = attributeIDs.length;
        String[] values = new String[length];
        for (int i = 0; i < length; i++) {
            values[i] = t.reason[attributeIDs[i]];
        }
        return values;
    }

    /**
     * 打印划分后的所有domain
     *
     * @param domains
     */
    public void printDomainContent(List<HashMap<Integer, Tuple>> domains) {
        HashMap<Integer, Tuple> domain = null;
        for (int i = 0; i < domains.size(); i++) {
            domain = domains.get(i);
            Iterator iter = domain.entrySet().iterator();
            System.out.println("\n---------Domain " + (i + 1) + "---------");

            while (iter.hasNext()) {
                Entry entry = (Entry) iter.next();
                Object key = entry.getKey();
                Tuple value = (Tuple) entry.getValue();
                // System.out.println("key = " + key + " value = " + Arrays.toString(value.getContext()));
            }
        }

        //System.out.println("\nDomain.size = "+domains.size()+"\n");
        System.out.println("\n>>> Completed!");
    }

    /**
     * 打印划分后的所有Group,写入log文件
     */
    public static void writePrintDomain(List<HashMap<Integer, Tuple>> groups, String outFile) throws IOException {
        FileWriter fw = new FileWriter(outFile);
        BufferedWriter bw = new BufferedWriter(fw);
        HashMap<Integer, Tuple> group = null;
        for (int i = 0; i < groups.size(); i++) {
            group = groups.get(i);
            Iterator iter = group.entrySet().iterator();
            bw.write("\n---------Group " + (i + 1) + "---------\n");

            while (iter.hasNext()) {
                Entry entry = (Entry) iter.next();
                Object key = entry.getKey();
                Tuple value = (Tuple) entry.getValue();
                bw.write("key = " + key + " value = " + Arrays.toString(value.getContext()) + "\n");
                // if(key.equals(457))
                // System.out.println("key = " + key + " value = " + Arrays.toString(value.getContext()));
            }
        }
        bw.write(">>> Completed!");
        bw.newLine();
        System.out.println(">>> Completed!");
    }

    /**
     * 打印划分后的所有Group
     */
    public static void printGroup(List<HashMap<Integer, Tuple>> groups, Log log) {
        HashMap<Integer, Tuple> group = null;
        for (int i = 0; i < groups.size(); i++) {
            group = groups.get(i);
            Iterator iter = group.entrySet().iterator();
            // System.out.println("\n---------Group " + (i + 1) + "---------");
//            log.write("\n---------Group " + (i + 1) + "---------");
            while (iter.hasNext()) {
                Entry entry = (Entry) iter.next();
                Object key = entry.getKey();
                Tuple value = (Tuple) entry.getValue();
//                if ((Integer) key == 0)
                System.out.println("key = " + key + " value = " + Arrays.toString(value.getContext()));
//                log.write("key = " + key + " value = " + Arrays.toString(value.getContext()));
            }
        }
//		System.out.println("\nGroup.size = "+groups.size()+"\n");
        System.out.println(">>> Completed!");
    }

    /**
     * 打印划分后的所有Group
     */
    public static void printGroup(List<HashMap<Integer, Tuple>> groups) {
        HashMap<Integer, Tuple> group = null;
        for (int i = 0; i < groups.size(); i++) {
            group = groups.get(i);
            Iterator iter = group.entrySet().iterator();
            // System.out.println("\n---------Group " + (i + 1) + "---------");

            while (iter.hasNext()) {
                Entry entry = (Entry) iter.next();
                Object key = entry.getKey();
                Tuple value = (Tuple) entry.getValue();
                // System.out.println("key = " + key + " value = " + Arrays.toString(value.getContext()));
            }
        }
		System.out.println("\nGroup.size = "+groups.size()+"\n");
        System.out.println(">>> Completed!");
    }


    /**
     * 打印整个数据集的内容
     */
    public void printDataSet(HashMap<Integer, String[]> dataSet) {
        System.out.println("\n==========DataSet Content==========");

        Iterator iter = dataSet.entrySet().iterator();
        while (iter.hasNext()) {
            Entry entry = (Entry) iter.next();
            Object key = entry.getKey();
            String[] value = (String[]) entry.getValue();
            // System.out.println("key = " + key + " value = " + Arrays.toString(value));
        }
        System.out.println();
    }

    public void printConflicts(ArrayList<Integer> conflicts) {
        conflicts.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if (o1 < o2) {
                    return -1;
                } else return 1;
            }
        });
        System.out.print("Conflict ID: [");
        for (Integer id : conflicts) {
            System.out.print("" + id + " ");
        }
        System.out.print("]\n");
    }

    public void printConflicts(HashMap<Integer, Conflicts> conflicts) {
        System.out.println("Conflict tuples:");
        Iterator<Entry<Integer, Conflicts>> iter = conflicts.entrySet().iterator();
        if (conflicts.size() == 0) {
            System.out.println("No Conflict tuples.");
            return;
        }
        while (iter.hasNext()) {
            Entry<Integer, Conflicts> entry = (Entry<Integer, Conflicts>) iter.next();
            Object key = entry.getKey();
            Conflicts value = entry.getValue();
            List<ConflictTuple> tuples = value.tuples;
            System.out.print("Tuple ID = " + key + "\n" + "Content = ");
            for (Tuple t : tuples) {
                System.out.print(Arrays.toString(t.getContext()) + " ");
            }
            System.out.println();
        }
    }
}
