package crowdPartialOrder;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import data.*;
import data.Tuple;
import util.LoadFile;
import util.Log;

import static main.Main.baseURL;
import static main.OutlierAnalyse.readOutlierFile;
import static main.OutlierAnalyse.writeGroupsToFile;

public class Main {

    public static void main(String[] args) {
        try {
            String dataset_baseURL = baseURL + "syn-car/0.02_0.05/";
            String fileURL = dataset_baseURL + "testData.csv";
            String logURL = dataset_baseURL + "log.txt";
            String groupFileURL = dataset_baseURL + "groups.txt";
            String ignoredTupleIDURL = dataset_baseURL + "ignoredTupleIDs.txt"; //记录所有没有被similarity.txt文件包含的tupleID
            Log log = new Log(logURL);

            //Write similar info into file
            String similar_file = dataset_baseURL + "similarity.txt";
            String splitStr = ",";

            HashMap<Integer, String> dataset_map = LoadFile.readFile(fileURL, splitStr);
            String[] header = LoadFile.str_header.split(splitStr);
            System.out.println("read file from: " + fileURL + " finished.");

            Queue<Group> grouptemp = new LinkedList<>();

            //Read rules file from disk, in order to ask questions based on the rule
            List<Tuple> ruleList = new Rule().loadRules(dataset_baseURL + "rules.txt", ",", header);
            Domain domain = new Domain();
            domain.header = header;
            List<HashMap<String, ConvertInfo>> convert_domains = new ArrayList<>(ruleList.size());
            ArrayList<Integer> ignoredIDs = new Rule().findIgnoredIDs(ruleList, header);

            Group prigroup = Group.init_pairs(similar_file, dataset_map, splitStr, ignoredIDs, ignoredTupleIDURL);
            grouptemp.add(prigroup);

            //Queue<Group> grouptemp = Group.read_similar_by_id(dataset_baseURL+"similarity-from-paper.txt", splitStr);
            System.out.println("Read files successfully.\nPairs are generated.\nCreate the first big Group is finished.");

            ArrayList<Group> groupResult = Group.group_by_similarity(grouptemp, log);
            System.out.println("Group split is finished.");
            Group.group_merge_sort(groupResult, 0);

            System.out.println("Start construct range search tree.");
            Graph graph = Graph.construct_graph_by_index(groupResult);
            System.out.println("Range search tree is constructed.");
            Vector<Gnode> nodes = graph.getNodes();

            HashMap<Integer, Vector<Integer>> fatherIDMap = new HashMap<Integer, Vector<Integer>>(nodes.size());
            for (int i = 0; i < nodes.size(); i++) {
                Gnode gnode = nodes.get(i);

                System.out.print("\nChildren of (G");
                System.out.print(gnode.group.gid + ") = ");
                System.out.print(gnode.children);

                gnode.father = Graph.findFather(gnode, nodes);
                //gnode.father.remove(new Integer(gnode.group.gid));
                fatherIDMap.put(gnode.group.gid, gnode.father);
            }
            System.out.println();

            System.out.println("Start construct graph, build path");
            /*for(int i=0;i<nodes.size();i++){
                Gnode gnode = nodes.get(i);
				ArrayList<Integer> path = Graph.generate_path(gnode, fatherIDMap);
				System.out.println(path);
			}*/
            HashMap<Integer, Group> group_map = Graph.copy_list_to_hash(groupResult);
            ArrayList<ArrayList<Integer>> disjoint_paths = Graph.generate_disjoint_path(nodes, fatherIDMap);
            System.out.println("The disjoint paths are: ");
            log.write("The disjoint paths are: ");
            for (ArrayList<Integer> path : disjoint_paths) {
//                System.out.println(path);
                log.write(path.toString());
            }


            /**
             * Ask questions by crowdsourcing
             * */
            ArrayList<Vector<Pair>> pair_with_rule = Graph.question_selection(graph, disjoint_paths, group_map, ruleList, log);
            writeRelatedPairs(pair_with_rule, log);
            domain.init(fileURL, splitStr, true, ruleList, convert_domains, ignoredIDs);
            List<List<HashMap<Integer, Tuple>>> domain_to_Groups = getMLNCleanGroup(pair_with_rule, ruleList, domain.domains);

//            List<List<HashMap<Integer, Tuple>>> domain_to_Groups = readOutlierFile(dataset_baseURL + "groups.txt");

            System.out.println("read finished");

            List<List<HashMap<Integer, Tuple>>> groups = reorganizeGroups(domain_to_Groups);
            //将domain_to_Groups写入文件
            writeGroupsToFile(groups, dataset_baseURL + "groups_reorganize.txt");//groupFileURL

            System.out.println("End.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static List<List<HashMap<Integer, Tuple>>> reorganizeGroups(List<List<HashMap<Integer, Tuple>>> domain_to_Groups) {
        List<List<HashMap<String, ArrayList<Tuple>>>> new_domain_to_groups = new ArrayList<>(domain_to_Groups.size());
        List<List<HashMap<Integer, Tuple>>> out = new ArrayList<>(domain_to_Groups.size());
        for (int i = 0; i < domain_to_Groups.size(); i++) {
            List<HashMap<Integer, Tuple>> groups = domain_to_Groups.get(i);
            List<HashMap<String, ArrayList<Tuple>>> new_groups = new ArrayList<>(groups.size());
            for (int j = 0; j < groups.size(); j++) {
                HashMap<Integer, Tuple> group = groups.get(j);
                HashMap<String, ArrayList<Tuple>> new_group = new HashMap<>();
                Iterator<Map.Entry<Integer, Tuple>> iter = group.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<Integer, Tuple> entry = iter.next();
                    Tuple t = entry.getValue();
                    String gamma = Arrays.toString(t.getContext());

                    if (new_group.containsKey(gamma)) {
                        ArrayList<Tuple> list = new_group.get(gamma);
                        list.add(t);
//                        new_group.put(gamma, list);
                    } else {
                        ArrayList<Tuple> list = new ArrayList<>();
                        list.add(t);
                        new_group.put(gamma, list);
                    }
                }
                new_groups.add(new_group);
            }

            //check不同的group in "new_groups"是否含有同样的key，若有，则合并他们
            for (int j = 0; j < new_groups.size() - 1; j++) {

                HashMap<String, ArrayList<Tuple>> group = new_groups.get(j);

                for (int k = j + 1; k < new_groups.size(); k++) {
                    boolean flag = false;
                    HashMap<String, ArrayList<Tuple>> curr_group = new_groups.get(k);
                    Iterator<Map.Entry<String, ArrayList<Tuple>>> curr_iter = curr_group.entrySet().iterator();
                    while(curr_iter.hasNext()){
                        Map.Entry<String, ArrayList<Tuple>> entry = curr_iter.next();
                        String gamma = entry.getKey();
                        if(group.containsKey(gamma)){//包含相同的key
                            flag = true;
                            break;
                        }
                    }

                    if(flag){ //合并
                        curr_iter = curr_group.entrySet().iterator();
                        while(curr_iter.hasNext()){
                            Map.Entry<String, ArrayList<Tuple>> entry = curr_iter.next();
                            String gamma = entry.getKey();
                            ArrayList<Tuple> list = entry.getValue();
                            if(group.containsKey(gamma)){
                                group.get(gamma).addAll(list);
                            }else{
                                group.put(gamma, list);
                            }
                        }
                        curr_group.clear();
                    }

                }
            }
            new_domain_to_groups.add(new_groups);
        }

        for(int i = 0; i < new_domain_to_groups.size(); i++){
            List<HashMap<String, ArrayList<Tuple>>> grps = new_domain_to_groups.get(i);
            List<HashMap<Integer, Tuple>> groups = new ArrayList<>();
            for(HashMap<String, ArrayList<Tuple>> grp: grps){
                if(grp.size()==0)continue;
                Iterator<Map.Entry<String, ArrayList<Tuple>>> iter = grp.entrySet().iterator();
                HashMap<Integer, Tuple> group = new HashMap<>();
                while(iter.hasNext()){
                    Map.Entry<String, ArrayList<Tuple>> entry = iter.next();
                    ArrayList<Tuple> list = entry.getValue();
                    for(Tuple t: list){
                        group.put(t.getTupleID(),t);
                    }
                }
                groups.add(group);
            }
            out.add(groups);
        }
        return out;
    }

    public static void writeRelatedPairs(ArrayList<Vector<Pair>> pair_with_rule, Log log) {
        for (int i = 0; i < pair_with_rule.size(); i++) {
            log.write("R" + i);
            Vector<Pair> pairs = pair_with_rule.get(i);
            for (Pair p : pairs) {
                log.write("<" + p.id1 + "," + p.id2 + "> " + Arrays.toString(p.t1) + ", " + Arrays.toString(p.t2));
            }
        }
    }

    public static HashSet<Integer> findRelatedTupleID(Integer sourceID, Vector<Pair> pairs_vec) {
        HashSet<Integer> set = new HashSet<>();
        ArrayList<Pair> visitedPair = new ArrayList<>();
        for (Pair pair : pairs_vec) {
            Integer id1 = pair.id1;
            boolean flag = false;
            if (id1.equals(sourceID)) {
                set.add(pair.id2);
                flag = true;
            } else {
                Integer id2 = pair.id2;
                if (id2.equals(sourceID)) {
                    set.add(pair.id1);
                    flag = true;
                }
            }
            if (flag) {
                visitedPair.add(pair);
            }
        }

        for (Pair pair : visitedPair) {
            pairs_vec.remove(pair);//找过的删掉
        }

        return set;
    }

    /**
     * generate groups for MLNClean
     * based on the rules
     */
    public static List<List<HashMap<Integer, Tuple>>> getMLNCleanGroup(ArrayList<Vector<Pair>> pair_with_rule, List<Tuple> ruleList, List<HashMap<Integer, Tuple>> domains) {
        List<List<HashMap<Integer, Tuple>>> domain_to_Groups = new ArrayList<>(ruleList.size());
        for (int i = 0; i < ruleList.size(); i++) {
            System.out.println("rule " + i + ":");
            HashMap<Integer, Tuple> domain = domains.get(i);
            Vector<Pair> pairs_vec = pair_with_rule.get(i);
            List<HashMap<Integer, Tuple>> groups = new ArrayList<>();
            while (!pairs_vec.isEmpty()) {

                Pair pair = pairs_vec.get(0);
                int pairID1 = pair.id1;
                int pairID2 = pair.id2;

                /*HashMap<Integer, Tuple> group = new HashMap<>();
                Tuple tuple1 = domain.get(pairID1);
                group.put(pairID1, tuple1);
                Tuple tuple2 = domain.get(pairID2);
                group.put(pairID2, tuple2);
                pairs_vec.remove(pair);*/

                //add tupleIDs into group
                HashSet<Integer> set = new HashSet<>();
                set.add(pairID1);
                set.add(pairID2);
                pairs_vec.remove(pair);
                HashMap<Integer, Tuple> group = new HashMap<>();
                group.put(pairID1, domain.get(pairID1));
                group.put(pairID2, domain.get(pairID2));
                while (!set.isEmpty()) {
                    Iterator<Integer> it = set.iterator();
                    if (it.hasNext()) {
                        Integer tupleID = it.next();
                        HashSet<Integer> tmp_set = findRelatedTupleID(tupleID, pairs_vec);
                        for (Integer id : tmp_set) {
                            group.put(id, domain.get(id));
                        }
                        set.addAll(tmp_set);
                        set.remove(tupleID);
                    }
                }
                if (group.size() != 0) {
                    groups.add(group);
                }
            }
            domain_to_Groups.add(groups);
        }

        int d_index = 0;
        for (List<HashMap<Integer, Tuple>> d : domain_to_Groups) {
            //if (d_index == 1) {
            System.out.println("\n*******Domain " + (d_index + 1) + "*******");
            Domain.printGroup(d);
            //}
            ++d_index;
        }
        return domain_to_Groups;
    }

}