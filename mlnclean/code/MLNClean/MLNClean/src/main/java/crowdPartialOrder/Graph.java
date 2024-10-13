package crowdPartialOrder;

import data.*;
import data.Tuple;
import util.Log;

import java.lang.reflect.Array;
import java.util.*;

public class Graph {

    Vector<Gnode> nodes = null;

    HashMap<String, String> visitedQuestion = new HashMap<>();

    public Vector<Gnode> getNodes() {
        return nodes;
    }

    public void setNodes(Vector<Gnode> nodes) {
        this.nodes = nodes;
    }

    public Graph(int node_size) {
        this.nodes = new Vector<Gnode>(node_size);
    }

    public void addNode(Gnode gnode) {
        this.nodes.addElement(gnode);
    }

    public static boolean judge(int a, int b, Graph graph) {
        int k;
        boolean flag = false;
        for (k = 0; k < Parameters.Anum; k++) {
            if (graph.nodes.get(a).group.range[k].down > graph.nodes.get(b).group.range[k].up) {
                flag = true;
                break;
            }

        }
        return flag;
    }

    private static void printGraph(AVLNode<PartialNode> t) {
        if (t != null) {
            printGraph(t.left);
            ArrayList<Group> groups = t.data.groups;

            printAllGroup(groups);
            System.out.print(" ");

            System.out.print(t.data.groups.get(0).range[t.data.aid].down + ", ");
            printGraph(t.right);
        }
    }

    public static HashMap<Integer, Group> copy_list_to_hash(ArrayList<Group> list) {
        HashMap<Integer, Group> map = new HashMap<Integer, Group>(list.size());
        for (Group group : list) {
            map.put(group.gid, group);
        }
        return map;
    }

    public static void printAllGroup(ArrayList<Group> groups) {
        System.out.print("(");
        for (int i = 0; i < groups.size(); i++) {
            if (i != groups.size() - 1)
                System.out.print("G" + groups.get(i).gid + " ");
            else
                System.out.print("G" + groups.get(i).gid);
        }
        System.out.print(")");
    }

    public static AVLTree<PartialNode> construct_range_search_tree(ArrayList<PartialNode> groupresult, int aid) {
        AVLTree<PartialNode> search_tree = new AVLTree<PartialNode>();
        int node_size = groupresult.size();

        for (int i = 0; i < node_size; i++) {
            ArrayList<Group> groups = groupresult.get(i).groups;
            for (Group g : groups) {
                ArrayList<Group> tmp_list = new ArrayList<Group>();
                tmp_list.add(g);
                PartialNode pNode = new PartialNode(aid, tmp_list, 0);
                search_tree.insert(pNode);
            }
        }

        System.out.print("\nA" + aid + " Root = ");
        printAllGroup(search_tree.root.data.groups);
        System.out.println();
        printGraph(search_tree.root);

        ArrayList<PartialNode> nodeList = search_tree.findAll(search_tree.root);

        // 添加最下一层（leaf）
        for (int i = 0; i < nodeList.size(); i++) {
            ArrayList<Group> groups = nodeList.get(i).groups;
            for (Group g : groups) {
                ArrayList<Group> tmp_list = new ArrayList<Group>();
                tmp_list.add(g);
                PartialNode pNode = new PartialNode(aid, tmp_list, 1);
                search_tree.insert(pNode);
            }
        }

        System.out.print("\nA" + aid + " Root = ");
        printAllGroup(search_tree.root.data.groups);
        System.out.println();
        printGraph(search_tree.root);

        return search_tree;
    }

    /**
     * 对children or father list排序
     */
    public static void sort(Vector<Integer> list, HashMap<Integer, Vector<Integer>> fatherIDMap) {
        mergeSort(list, fatherIDMap, 0, list.size() - 1);
    }

    public static void merge(Vector<Integer> a, HashMap<Integer, Vector<Integer>> fatherIDMap, int left, int mid, int right) {
        int[] tmp = new int[a.size()];// 辅助数组
        int p1 = left, p2 = mid + 1, k = left;// p1、p2是检测指针，k是存放指针

        while (p1 <= mid && p2 <= right) {
            Vector<Integer> father = fatherIDMap.get(a.get(p1));// get list of Father(a[p1])
            if (father.contains(a.get(p2)))    //a[p1] <= a[p2]
                tmp[k++] = a.get(p1++);
            else
                tmp[k++] = a.get(p2++);
        }

        while (p1 <= mid)
            tmp[k++] = a.get(p1++);// 如果第一个序列未检测完，直接将后面所有元素加到合并的序列中
        while (p2 <= right)
            tmp[k++] = a.get(p2++);// 同上

        // 复制回原s数组
        for (int i = left; i <= right; i++)
            a.set(i, tmp[i]);
    }

    public static void mergeSort(Vector<Integer> a, HashMap<Integer, Vector<Integer>> fatherIDMap, int start, int end) {
        if (start < end) {// 当子序列中只有一个元素时结束递归
            int mid = (start + end) / 2;// 划分子序列
            mergeSort(a, fatherIDMap, start, mid);// 对左侧子序列进行递归排序
            mergeSort(a, fatherIDMap, mid + 1, end);// 对右侧子序列进行递归排序
            merge(a, fatherIDMap, start, mid, end);// 合并
        }
    }

    public static ArrayList<Integer> findPath(Gnode gnode, HashMap<Integer, Vector<Integer>> fatherIDMap) {
        ArrayList<Integer> path = new ArrayList<Integer>();
        int destID = gnode.group.gid;
        path.add(destID);
        Vector<Integer> fatherList = gnode.father;
        Vector<Integer> fList = fatherIDMap.get(destID);
        Vector<Integer> lost_father = new Vector<Integer>();
        for (Integer fatherID : fatherList) {
            if (fList.contains(fatherID)) {
                path.add(fatherID);
                fList = fatherIDMap.get(fatherID);
            }
        }
        return path;
    }

    public static Vector<Integer> copy(Vector<Integer> source) {
        Vector<Integer> copy_list = new Vector<Integer>(source.size());
        for (Integer i : source) {
            copy_list.add(i);
        }
        return copy_list;
    }

    public static void setColor(boolean color_flag, ArrayList<Integer> path, HashMap<Integer, Group> group_map) {
        for (Integer gid : path) {
            Group group = group_map.get(gid);
            if (color_flag == true) {
                group.color = 1;
            } else {
                group.color = 2;
            }
        }
    }

    public static ArrayList<Integer> getSubPath(int color, Integer gid, ArrayList<Integer> path) {
        ArrayList<Integer> subPath = new ArrayList<Integer>();
        int i = path.indexOf(gid);
        if (color == 1) {//green, subpath=它的parents
            i += 1;
            for (; i < path.size(); i++) {
                subPath.add(path.get(i));
            }
        } else {
            i -= 1;
            for (; i >= 0; i--) {
                subPath.add(path.get(i));
            }
        }
        return subPath;
    }

    public static String findTupleRelatedToRule(Tuple rule, String[] tuple) {
        int[] attribute_index = rule.getAttributeIndex();
        String str1 = "[";
        int i = 0;
        int size = attribute_index.length;
        for (int index : attribute_index) {
            if (i != size - 1)
                str1 += tuple[index] + ", ";
            else
                str1 += tuple[index] + "]";
            i++;
        }
        return str1;
    }

    static String plusString(String s1, String s2) {
        List<String> list = new ArrayList<>();
        list.add(s1);
        list.add(s2);
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                /*
                 * int compare(String o1, String o2) 返回一个基本类型的整型，
                 * 返回负数表示：o1 小于o2，
                 * 返回0 表示：o1和o2相等，
                 * 返回正数表示：o1大于o2
                 */
                if (o1.compareTo(o2) > 0) {
                    return 1;
                } else if (o1.compareTo(o2) > 0) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        String s = list.get(0) + " || " + list.get(1);
        return s;
    }

    public static boolean ask_question(Graph graph, Integer gid, ArrayList<Integer> path, HashMap<Integer, Group> group_map, Tuple rule, Log log) {
        Group group = group_map.get(gid);
        Pair pair = group.pairs.get(0);

        String t1_relatedRule = findTupleRelatedToRule(rule, pair.t1);
        String t2_relatedRule = findTupleRelatedToRule(rule, pair.t2);
        String tmp_t1 = Arrays.toString(pair.t1);
        String tmp_t2 = Arrays.toString(pair.t2);
        System.out.println("gamma of t" + pair.id1 + ": " + t1_relatedRule);
        System.out.println("gamma of t" + pair.id2 + ": " + t2_relatedRule);
        System.out.println("t" + pair.id1 + ": " + tmp_t1);
        System.out.println("t" + pair.id2 + ": " + tmp_t2);
        System.out.println("Are they belong to the same group? (y/n)");

        log.write("gamma of t" + pair.id1 + ": " + t1_relatedRule);
        log.write("gamma of t" + pair.id2 + ": " + t2_relatedRule);
        log.write("t" + pair.id1 + ": " + tmp_t1);
        log.write("t" + pair.id2 + ": " + tmp_t2);
        log.write("Are they belong to the same group? (y/n)");
        String ans;
        String plusStr = plusString(t1_relatedRule, t2_relatedRule);//把两个gamma合并到一个string中，方便记录
        if(graph.visitedQuestion.containsKey(plusStr)){ //如果question的答案已被记录过
            ans = graph.visitedQuestion.get(plusStr);
        } else {
            if (t1_relatedRule.equals(t2_relatedRule)) {//如果两者完全相等，则直接记为Yes, 不需要询问workers
                ans = "y";
            } else {
                Scanner sc = new Scanner(System.in);
                ans = sc.nextLine();
                while (!ans.equals("y") && !ans.equals("Y") && !ans.equals("n") && !ans.equals("N")) {
                    System.err.println("Please enter the right answer.");
                    ans = sc.nextLine();
                }
            }
            graph.visitedQuestion.put(plusStr, ans);
        }

        log.write("The answer is < " + ans + " >");

        if (ans.equals("y") || ans.equals("Y")) {
            group.color = 1; //green,相似,为它的父节点都color green
            System.out.println("Group " + gid + " is colored green");
            log.write("Group " + gid + " is colored green");
            ArrayList<Integer> parentPath = getSubPath(group.color, gid, path);
            for (Integer id : parentPath) {
                if (group_map.get(id).color == 1) continue;
                group_map.get(id).color = 1;
                System.out.println("Group " + id + " is colored green");
                log.write("Group " + id + " is colored green");
            }
            return true;
        } else {
            group.color = 2; //red,不相似,为它的子节点都color red
            System.out.println("Group " + gid + " is colored red");
            log.write("Group " + gid + " is colored red");
            ArrayList<Integer> childrenPath = getSubPath(group.color, gid, path);
            for (Integer id : childrenPath) {
                group_map.get(id).color = 2;
                System.out.println("Group " + id + " is colored red");
                log.write("Group " + id + " is colored red");
            }
            return false;
        }
    }

    public static Vector<Pair> binary_search(Graph graph, ArrayList<Integer> path, int lo, int hi, HashMap<Integer, Group> group_map, Tuple rule, Log log) {
        Vector<Pair> pair_map = new Vector<>(1000);
        if (lo <= hi) {
            int mid = (lo + hi) / 2;
            if (group_map.get(path.get(mid)).color != 0) {
                return pair_map;
            } else if (!ask_question(graph, path.get(mid), path, group_map, rule, log)) {
                //red，不相似，访问parents
                pair_map.addAll(binary_search(graph, path, mid + 1, hi, group_map, rule, log));

            } else {
                //green，相似

                Group group = group_map.get(path.get(mid));
                Vector<Pair> pairs = group.pairs;
                pair_map.addAll(pairs);
                ArrayList<Integer> parentPath = getSubPath(group.color, group.gid, path);
                for (Integer id : parentPath) {
                    pair_map.addAll(group_map.get(id).pairs);
                }
                //访问children
                pair_map.addAll(binary_search(graph, path, lo, mid - 1, group_map, rule, log));
            }
        }
        return pair_map;
    }

    public static void resetColor(HashMap<Integer, Group> group_map) {
        Iterator<Map.Entry<Integer, Group>> iter = group_map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Group> entry = iter.next();
            Group group = entry.getValue();
            group.color = 0;
        }
    }

    public static ArrayList<Vector<Pair>> question_selection(Graph graph, ArrayList<ArrayList<Integer>> disjoint_paths,
                                                             HashMap<Integer, Group> group_map, List<Tuple> ruleList, Log log) {
        ArrayList<Vector<Pair>> pair_with_rule = new ArrayList<>(ruleList.size());
        for (Tuple rule : ruleList) {
            Vector<Pair> pair_map = new Vector<>(500);
            for (ArrayList<Integer> path : disjoint_paths) {
                pair_map.addAll(binary_search(graph, path, 0, path.size() - 1, group_map, rule, log));
            }
            pair_with_rule.add(pair_map);
            resetColor(group_map);
        }
        return pair_with_rule;
    }

    public static ArrayList<ArrayList<Integer>> generate_disjoint_path(Vector<Gnode> nodes, HashMap<Integer, Vector<Integer>> fatherIDMap) {
        ArrayList<ArrayList<Integer>> disjoint_paths = new ArrayList<ArrayList<Integer>>();

        //remainList代表graph中剩余未被visited的点
        ArrayList<Integer> visitedList = new ArrayList<Integer>(nodes.size());
        //visitedList代表graph中已被visited的点
        ArrayList<Integer> remainList = new ArrayList<Integer>(nodes.size());

        for (int i = 0; i < nodes.size(); i++) {
            Gnode gnode = nodes.get(i);
            Integer gid = gnode.group.gid;
            remainList.add(gid);
        }
        for (int i = 0; i < nodes.size(); i++) {
            Gnode gnode = nodes.get(i);
            Integer gid = gnode.group.gid;
            if (remainList.contains(gid)) {
                ArrayList<Integer> path = generate_path(gnode, visitedList, fatherIDMap);
                disjoint_paths.add(path);
                visitedList.addAll(path);
                remainList.removeAll(path);
            }
        }
        return disjoint_paths;
    }


    public static ArrayList<Integer> generate_path(Gnode gnode, ArrayList<Integer> visitedList, HashMap<Integer, Vector<Integer>> fatherIDMap) {
        // 根据终点反推起点
        ArrayList<Integer> path = new ArrayList<Integer>();
        int destID = gnode.group.gid;
        path.add(destID);
        Vector<Integer> fatherList = gnode.father;//原始gnode（dest）的father list
        Vector<Integer> visitedFatherList = new Vector<Integer>(fatherList.size());
        Vector<Integer> remainFatherList = new Vector<Integer>(fatherList.size());
        Vector<Integer> fList = copy(fatherIDMap.get(destID));//current node的father list
        fList.removeAll(visitedList);//取差集，删去已访问的nodes
        for (Integer fatherID : fatherList) {
            if (fList.contains(fatherID)) {
                visitedFatherList.add(fatherID);
                path.add(fatherID);
                fList = fatherIDMap.get(fatherID);
                fList.removeAll(visitedList);
            } else {
                remainFatherList.add(fatherID);
            }
        }
        return path;
    }

    public static AVLTree<PartialNode> construct_range_search_tree0(ArrayList<Group> groupresult, int aid) {
        AVLTree<PartialNode> search_tree = new AVLTree<PartialNode>();
        int node_size = groupresult.size();

        for (int i = 0; i < node_size; i++) {
            Group group = groupresult.get(i);
            ArrayList<Group> groups = new ArrayList<Group>();
            groups.add(group);
            PartialNode pNode = new PartialNode(aid, groups, 0);
            System.out.println(pNode.groups.get(0).gid);
            if (pNode.groups.get(0).gid == 2) {
                System.out.println("debug here");
            }
            search_tree.insert(pNode);
        }
        System.out.print("\nA" + aid + " Root = ");
        printAllGroup(search_tree.root.data.groups);
        System.out.println();
        printGraph(search_tree.root);

        for (int i = 0; i < node_size; i++) {
            Group group = groupresult.get(i);
            ArrayList<Group> groups = new ArrayList<Group>();
            groups.add(group);
            PartialNode pNode = new PartialNode(aid, groups, 1);
            search_tree.insert(pNode);
        }

        System.out.print("\nA" + aid + " Root = ");
        printAllGroup(search_tree.root.data.groups);
        System.out.println();
        printGraph(search_tree.root);

        return search_tree;
    }


    public static HashSet<Integer> find_children(Group pre_node, AVLTree<PartialNode> search_tree, int aid) {

        HashSet<Integer> childrenIDs = new HashSet<Integer>(100);

        // qualifiedList代表每个node对应的qualified nodes
        ArrayList<PartialNode> qualifiedList = search_tree.find_qualified_nodes(pre_node);
        if (aid == Parameters.Anum - 1) {
            for (PartialNode q_node : qualifiedList) {
                q_node.searchTag = 1;// 表明该点用于查找
                ArrayList<PartialNode> curr_node_list = search_tree.getUnderNodes(q_node);
                q_node.searchTag = 0;
                if (curr_node_list.size() == 0) {
                    curr_node_list.add(q_node);
                    ArrayList<Group> groups = q_node.groups;
                    for (Group g : groups) {
                        childrenIDs.add(g.gid);
                    }
                } else {
                    for (PartialNode node : curr_node_list) {
                        ArrayList<Group> groups = node.groups;
                        for (Group g : groups) {
                            childrenIDs.add(g.gid);
                        }
                    }
                }
            }
            return childrenIDs;
        }

        for (PartialNode q_node : qualifiedList) {
            q_node.searchTag = 1;// 表明该点用于查找
            ArrayList<PartialNode> curr_node_list = search_tree.getUnderNodes(q_node);
            q_node.searchTag = 0;
            if (curr_node_list.size() == 0) {
                curr_node_list.add(q_node);
            }
            if (aid == 4) {
                System.out.println();
            }
            AVLTree<PartialNode> subtree = construct_range_search_tree(curr_node_list, aid + 1);
            HashSet<Integer> tmp_childrenIDs = find_children(pre_node, subtree, aid + 1);
            if (tmp_childrenIDs != null) {
                childrenIDs.addAll(tmp_childrenIDs);
            }
        }

        return childrenIDs;
    }

    public static ArrayList<Integer> find_children(PartialNode pre_node, AVLTree<PartialNode> search_tree, int aid) {

        ArrayList<Integer> childrenIDs = new ArrayList<Integer>(100);
        // 返回search_tree中的所有node
        // ArrayList<PartialNode> node_list =
        // search_tree.findAll(search_tree.root);

        // qualifiedList代表每个node对应的qualified nodes
        ArrayList<PartialNode> qualifiedList = search_tree.find_qualified_nodes(pre_node);
        if (aid == Parameters.Anum - 1) {
            for (PartialNode q_node : qualifiedList) {
                q_node.searchTag = 1;// 表明该点用于查找
                ArrayList<PartialNode> curr_node_list = search_tree.getUnderNodes(q_node);
                q_node.searchTag = 0;
                if (curr_node_list.size() == 0) {
                    curr_node_list.add(q_node);
                    ArrayList<Group> groups = q_node.groups;
                    for (Group g : groups) {
                        childrenIDs.add(g.gid);
                    }
                } else {
                    for (PartialNode node : curr_node_list) {
                        ArrayList<Group> groups = node.groups;
                        for (Group g : groups) {
                            childrenIDs.add(g.gid);
                        }
                    }
                }
            }
            return childrenIDs;
        }

        for (PartialNode q_node : qualifiedList) {
            q_node.searchTag = 1;// 表明该点用于查找
            ArrayList<PartialNode> curr_node_list = search_tree.getUnderNodes(q_node);
            q_node.searchTag = 0;
            if (curr_node_list.size() == 0) {
                curr_node_list.add(q_node);
            }
            if (aid == 4) {
                System.out.println();
            }
            AVLTree<PartialNode> subtree = construct_range_search_tree(curr_node_list, aid + 1);
            ArrayList<Integer> tmp_childrenIDs = find_children(pre_node, subtree, aid + 1);
            if (tmp_childrenIDs != null) {
                childrenIDs.addAll(tmp_childrenIDs);
            }
        }

        return childrenIDs;
    }

    public static Vector<Integer> findFather(Gnode gnode, Vector<Gnode> graph) {
        Vector<Integer> father = new Vector<Integer>();
        int myID = gnode.group.gid;
        System.out.print("\nFather of (G" + myID + ") = [");
        for (int i = 0; i < graph.size(); i++) {
            int fatherID = graph.get(i).group.gid;
            Vector<Integer> childrenID = graph.get(i).children;
            for (Integer childID : childrenID) {
                if (myID == childID) {
                    father.add(fatherID);
                    System.out.print(fatherID + ",");
                    break;
                }
            }
        }
        System.out.print("]\n");
        return father;

    }

    public static Graph construct_graph_by_index(ArrayList<Group> groupresult) {
        Graph graph = null; // 声明一个graph

        int aid = 0;
        AVLTree<PartialNode> search_tree = construct_range_search_tree0(groupresult, aid);
        System.out.println();
        ArrayList<PartialNode> node_list = search_tree.getAllNodes(search_tree.root);

        graph = new Graph(node_list.size()); // 初始化graph的大小

        for (PartialNode pnode : node_list) {
            ArrayList<Group> groups = pnode.groups;
            for (Group group : groups) {
                if (group.gid == 1) {
                    System.out.println("debug here");
                }

                HashSet<Integer> childrenIDs = find_children(group, search_tree, aid);
                childrenIDs.remove(new Integer(group.gid));
                System.out.print("\nChildren of ");
                printAllGroup(pnode.groups);
                System.out.print(" = ");
                System.out.println(childrenIDs);

                // 存放每个group以及它的C(gij)的IDs
                Gnode gnode = new Gnode(group, childrenIDs);
                graph.addNode(gnode);
            }
        }
        return graph;

    }
}
