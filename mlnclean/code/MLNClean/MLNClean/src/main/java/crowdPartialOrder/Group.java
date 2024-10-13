package crowdPartialOrder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import info.debatty.java.stringsimilarity.Cosine;
import info.debatty.java.stringsimilarity.Jaccard;
import info.debatty.java.stringsimilarity.Levenshtein;

import java.util.Map.Entry;
import java.util.Queue;

import util.Log;
import util.Similarity;

public class Group { // 存放相似的pairs
    public int gid;
    public int f; // f的作用是标记，0，1，2，代表无相同实体group，混合group，相同实体group，先初始化为0.
    public Vector<Pair> pairs;
    int count;
    public Range[] range;    // range.length = Anum
    public float[] partition_line = new float[Parameters.Anum]; // 每一个attribute都有一个切分后的分割界限值
    public int color = 0;    //0,1,2分别表示没有color,绿色（相似），红色（不相似）

    public Group() {
    }

    public Group(Vector<Pair> pairs, Range[] range, int gid) {
        this.gid = gid;
        this.pairs = pairs;
        this.range = new Range[range.length];
        for (int i = 0; i < range.length; i++) {
            this.range[i] = new Range(range[i].up, range[i].down);
        }
    }

    public Group(int Anum) {
        this.range = new Range[Anum];

        for (int k = 0; k < Anum; k++) {// k表示第k个Attribute
            Range range = new Range(1, Parameters.threshold);
            this.range[k] = range;
        }
        this.pairs = new Vector<Pair>();
    }

    public static void write_partition_group_info(Vector<Group> groupresult, String outFile) {
        try {
            int c = 0;
            File file = new File(outFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for (int i = 0; i < groupresult.size(); i++) {
                // f的作用是标记，0，1，2，代表无相同实体group，混合group，相同实体group，先初始化为0.
                groupresult.get(i).f = 0;
                bw.write(i + " : ");
                // 组内相似度范围
                for (int j = 0; j < Parameters.Anum; j++) {
                    bw.write(groupresult.get(i).range[j].up + "———" + groupresult.get(i).range[j].down + " ");
                }
                bw.write("\n");

                // 组内所含Pair标号/代表
                if (groupresult.get(i).pairs.size() > Parameters.Anum) {
                    for (int k = 0; k < groupresult.get(i).pairs.size(); k++) {
                        bw.write(groupresult.get(i).pairs.get(k).id1 + " " + groupresult.get(i).pairs.get(k).id2);
                    }
                } else {
                    for (int k = 0; k < groupresult.get(i).pairs.size(); k++) {
                        bw.write(groupresult.get(i).pairs.get(k).id1 + " " + groupresult.get(i).pairs.get(k).id2);
                    }
                }
                bw.write("\n");

                // ************************************
                // 开始标注.f
                boolean f = false;
                for (int j = 0; j < groupresult.get(i).pairs.size(); j++) {
                    if (!(groupresult.get(i).pairs.get(j).id1 + 1 == groupresult.get(i).pairs.get(j).id2
                            && groupresult.get(i).pairs.get(j).id1 <= 222
                            && groupresult.get(i).pairs.get(j).id1 % 2 == 0))
                        f = true;
                }
                if (f == false) {
                    groupresult.get(i).f = 2;
                    c += groupresult.get(i).pairs.size();
                }
                if (f == true) {
                    for (int j = 0; j < groupresult.get(i).pairs.size(); j++) {
                        if ((groupresult.get(i).pairs.get(j).id1 + 1 == groupresult.get(i).pairs.get(j).id2
                                && groupresult.get(i).pairs.get(j).id1 <= 222
                                && groupresult.get(i).pairs.get(j).id1 % 2 == 0)) {
                            groupresult.get(i).f = 1;
                            // wr<<origin[groupresult.get(i).pair.at(j).id1]<<"
                            // "<<origin[groupresult.get(i).pairs.get(j).id2]<<endl;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void group_merge_sort(ArrayList<Group> groupList, int aid) {
        MergeSort(groupList, aid);
    }

    private static void merge(ArrayList<Group> groupList, int left, int mid, int right, int aid) {

        ArrayList<Group> tmp = new ArrayList<Group>(groupList.size());

        for (int i = 0; i < groupList.size(); i++) {
            tmp.add(new Group());
        }

        int r1 = mid + 1;
        int tIndex = left;
        int cIndex = left;
        // 逐个归并
        while (left <= mid && r1 <= right) {
            if (groupList.get(left).range[aid].down <= groupList.get(r1).range[aid].down) {
                tmp.set(tIndex++, groupList.get(left++));
                // tmp[tIndex++] = a[left++];
            } else {
                tmp.set(tIndex++, groupList.get(r1++));
                // tmp[tIndex++] = a[r1++];
            }
        }
        // 将左边剩余的归并
        while (left <= mid) {
            tmp.set(tIndex++, groupList.get(left++));
            // tmp[tIndex++] = a[left++];
        }
        // 将右边剩余的归并
        while (r1 <= right) {
            tmp.set(tIndex++, groupList.get(r1++));
            // tmp[tIndex++] = a[r1++];
        }
        // 从临时数组拷贝到原数组
        while (cIndex <= right) {
            groupList.set(cIndex, tmp.get(cIndex));
            // a[cIndex] = tmp[cIndex];
            // 输出归并排序结果
            // System.out.print(groupList.get(cIndex).range[aid].down + "\t");
            cIndex++;
        }
        // System.out.println();
    }

    private static void MergeSort(ArrayList<Group> groupList, int aid) {
        // System.out.println("开始排序");
        Sort(groupList, 0, groupList.size() - 1, aid);
    }

    private static void printArray(String pre, int[] a) {
        System.out.print(pre + "\n");
        for (int i = 0; i < a.length; i++)
            System.out.print(a[i] + "\t");
        System.out.println();
    }

    private static void Sort(ArrayList<Group> groupList, int left, int right, int aid) {
        if (left >= right)
            return;

        int mid = (left + right) / 2;
        // 二路归并排序里面有两个Sort，多路归并排序里面写多个Sort就可以了
        Sort(groupList, left, mid, aid);
        Sort(groupList, mid + 1, right, aid);
        merge(groupList, left, mid, right, aid);

    }

    public int compare(PartialNode other) {
        //如果是同一个node,则返回2
        if (this.gid == other.groups.get(0).gid && other.leaf == 0) {
            return 2;
        }
        float ans;

        if (other.searchTag == 0 && other.leaf == 1) {
            ans = this.range[other.aid].down - Parameters.minValue - other.groups.get(0).range[other.aid].up;
//			System.out.println(""+(this.group.range[this.aid].down - Parameters.minValue)+"-"+this.group.range[this.aid].up+"="+ans);
        } else {
            ans = this.range[other.aid].down - other.groups.get(0).range[other.aid].up;
//			System.out.println(""+this.group.range[this.aid].down+"-"+this.group.range[this.aid].up+"="+ans);
        }

        if (ans > 0)
            return 1;
        else if (ans == 0)
            return 0;
        else
            return -1;
    }

    public static ArrayList<Group> group_by_similarity(Queue<Group> Q, Log log) {
        int[] flags = new int[Parameters.Anum];
        Pair curr_pair;
        int group_id = 0;
        float line;
        ArrayList<Group> groupResult = new ArrayList<Group>(100);
        while (!Q.isEmpty()) {

            for (int i = 0; i < Parameters.Anum; i++) {// clear flags
                flags[i] = 0;
            }

            Group group = adjust_threshold(Q.peek());
            Q.poll();

            for (int p = 0; p < Parameters.Anum; p++) {
                // 如果条件满足，其中threshold = ε， split该group
                if (group.range[p].up - group.range[p].down > Parameters.threshold) {
                    // System.out.println("[" + group.range[p].down + ", " +
                    // group.range[p].up + "]");
                    flags[p] = 1;
                    line = (group.range[p].up + group.range[p].down) / 2;
                    // System.out.println("line = " + line);
                    group.partition_line[p] = line;
                }
            }

            // check if the group Ni is split by Attr[p]
            int p_index = byte_to_int(flags);
            if (p_index > 0) { // generate 2^t children of the group Ni
                int size = (int) Math.pow(2, Parameters.Anum);
                HashMap<Integer, Group> partition = new HashMap<Integer, Group>(size);

                while (!group.pairs.isEmpty()) { // 遍历每一个元组对，分配到不同的新集合
                    curr_pair = group.pairs.lastElement();
                    group.pairs.remove(group.pairs.size() - 1);// pop_back

                    int[] hash = new int[Parameters.Anum];

                    for (int i = 0; i < Parameters.Anum; i++) {
                        if (flags[i] == 0) {
                            hash[i] = 0;// 该Attribute没有被划分
                        } else {
                            line = group.partition_line[i];
                            if (curr_pair.similar[i] > line)
                                hash[i] = 1;
                            if (curr_pair.similar[i] <= line)
                                hash[i] = 0;
                        }
                    }
                    int index = byte_to_int(hash);
                    if (null != partition.get(index)) {
                        partition.get(index).count++;
                        partition.get(index).pairs.addElement(curr_pair);
                    } else {
                        partition.put(index, new Group(Parameters.Anum));
                        partition.get(index).count++;
                        partition.get(index).pairs.addElement(curr_pair);
                    }
                }
                // Q.addAll(partition)
                Iterator<Entry<Integer, Group>> iterator = partition.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry<Integer, Group> entry = iterator.next();
                    // Group grp = adjust_threshold(entry.getValue());
                    Q.add(entry.getValue());
                }
            } else { // Ni is a leaf and taken as a group g.
                group.gid = group_id++;
                groupResult.add(group);
            }
        }
        for (Group group : groupResult) {
            System.out.println("G" + group.gid);
            //log.write("G" + group.gid);
            for (Range range : group.range) {
                System.out.print("[" + range.down + "," + range.up + "]  ");
                //log.write("[" + range.down + "," + range.up + "]  ");
            }
            System.out.println();
            Vector<Pair> pairs = group.pairs;
            for (Pair pair : pairs) {
                System.out.println("<" + pair.id1 + ", " + pair.id2 + ">");
                //log.write("<" + pair.id1 + ", " + pair.id2 + ">");
            }
        }

        System.out.println("Group number: " + groupResult.size());
        log.write("Group number: " + groupResult.size());
        return groupResult;
    }

    public static int byte_to_int(int a[]) {
        String binaryString = "";
        for (int a_i : a) {
            binaryString += a_i;
        }
        return Integer.parseInt(binaryString, 2);
    }

    public static int transfer_to_int(int a[]) {
        int i;
        int result = 0;
        for (i = Parameters.Anum - 1; i >= 0; i--) {
            result = result + a[i] * (int) Math.pow(3, Parameters.Anum - 1 - i);
        }
        return result;
    }

    public static Group adjust_threshold(Group temp) {
        int i, j;
        float max = 0, min = 1;
        for (i = 0; i < Parameters.Anum; i++) {
            max = 0;
            min = 1;
            for (j = 0; j < temp.pairs.size(); j++) {
                if (max < temp.pairs.get(j).similar[i])
                    max = temp.pairs.get(j).similar[i];
                if (min > temp.pairs.get(j).similar[i])
                    min = temp.pairs.get(j).similar[i];
            }
            temp.range[i].up = max;
            temp.range[i].down = min;
        }
        return temp;
    }

    public static Queue<Group> read_similar_by_id(String inFile, String splitStr) {
        Queue<Group> grouptemp = new LinkedList<Group>();
        Group prigroup = new Group(Parameters.Anum);// 初始化第一个大Group
        File file = new File(inFile);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String str = null;

            while ((str = reader.readLine()) != null) {
                int tid1 = Integer.parseInt(str.substring(0, str.indexOf(" || ")));
                String tmpstr1 = str.substring(str.indexOf(" || ") + 4);
                int tid2 = Integer.parseInt(tmpstr1.substring(0, tmpstr1.indexOf(" || ")));
                String tmpstr2 = tmpstr1.substring(tmpstr1.indexOf(" || ") + 4);
                String[] similarity = tmpstr2.replaceAll(" ", "").split("\\|\\|");
                Pair pair = new Pair(tid1, tid2, Parameters.Anum);

                for (int k = 0; k < Parameters.Anum; k++) {
                    pair.setSimilar(k, Float.parseFloat(similarity[k]));
                }
                prigroup.count++;
                prigroup.pairs.addElement(pair); // 将初始化好的pair放入第一个大group中
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        grouptemp.add(prigroup);
        return grouptemp;
    }

    public static Queue<Group> read_similar(String inFile, String splitStr) {
        Queue<Group> grouptemp = new LinkedList<Group>();
        Group prigroup = new Group(Parameters.Anum);// 初始化第一个大Group
        File file = new File(inFile);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String str = null;

            while ((str = reader.readLine()) != null) {
                String t1 = str.substring(0, str.indexOf(" || "));
                String tmpstr1 = str.substring(str.indexOf(" || ") + 4);
                String t2 = tmpstr1.substring(0, tmpstr1.indexOf(" || "));
                String tmpstr2 = tmpstr1.substring(tmpstr1.indexOf(" || ") + 4);
                String[] similarity = tmpstr2.replaceAll(" ", "").split("\\|\\|");
                Pair pair = new Pair(t1.split(splitStr), t2.split(splitStr), Parameters.Anum);

                for (int k = 0; k < Parameters.Anum; k++) {
                    pair.setSimilar(k, Float.parseFloat(similarity[k]));
                }
                prigroup.count++;
                prigroup.pairs.addElement(pair); // 将初始化好的pair放入第一个大group中
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        grouptemp.add(prigroup);
        return grouptemp;
    }

    // 计算每个pair的相似度, 形成初始的第一个大的Group
    public static Group init_pairs(String outFile, HashMap<Integer, String> dataset_map, String splitstr, ArrayList<Integer> ignoredIDs, String ignoredTupleFile) {
        ArrayList<Integer> ignoredTuples = new ArrayList<>(100);//记录所有没有达到pair阈值的tupleID
        Group prigroup = new Group(Parameters.Anum);// 初始化第一个大Group
        HashSet<String> hashSet = new HashSet<String>(10000);
        try {
            File file = new File(outFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            Iterator<Entry<Integer, String>> iter1 = dataset_map.entrySet().iterator();
            while (iter1.hasNext()) {
                Entry<Integer, String> entry1 = iter1.next();
                int tid1 = entry1.getKey();
                String t1 = entry1.getValue();
                String[] split_t1 = t1.split(splitstr);
                Iterator<Entry<Integer, String>> iter2 = dataset_map.entrySet().iterator();
                boolean flag = false;
                while (iter2.hasNext()) {
                    Entry<Integer, String> entry2 = iter2.next();
                    int tid2 = entry2.getKey();
                    if (tid1 == tid2) {
                        flag = true;
                        continue;
                    }
                    String combine_id;
                    if (tid2 > tid1) {// 排序，小—>大，方便比较
                        combine_id = "" + tid1 + "," + tid2;
                    } else {
                        combine_id = "" + tid2 + "," + tid1;
                    }
                    if (hashSet.contains(combine_id)) {
                        continue;
                    }
                    hashSet.add(combine_id);
                    String t2 = entry2.getValue();

					/*
                     * if (Similarity.editSimilarity(t1, t2) <
					 * Parameters.dissimilar_tau) { continue; }
					 */

                    String[] split_t2 = t2.split(splitstr);

                    Pair pair = new Pair(tid1, tid2, split_t1, split_t2, split_t1.length);
                    float[] tmp_similarity = new float[Parameters.Anum];

                    boolean[] flags = new boolean[Parameters.Anum];
                    for (int k = 0; k < Parameters.Anum; k++) { // 为当前的Pair计算每个属性上的相似度
                        float similar = 0;
//                        similar = Similarity.editSimilarity(split_t1[k], split_t2[k]);
//                        similar = Similarity.jaccardSimilarity(split_t1[k], split_t2[k]);
                        if (split_t1[k].length() < 3) {
                            split_t1[k] += "*";
                        }
                        if (split_t2[k].length() < 3) {
                            split_t2[k] += "*";
                        }
                        similar = (float) new Cosine().similarity(split_t1[k], split_t2[k]);
//                        System.out.println(similar);
                        if (similar < Parameters.tau) {
                            similar = 0;
                        }
                        tmp_similarity[k] = similar;
                    }

                    for (int k = 0; k < Parameters.Anum; k++) {
                        //如果该属性是跟rule相关的，但相似度小于阈值tau,则标记flag=true
                        if (!ignoredIDs.contains(k) && tmp_similarity[k] == 0) {
                            flags[k] = true;
                        } else flags[k] = false;
                    }

                    int count = 0;
                    for (int k = 0; k < Parameters.Anum; k++) {
                        if (flags[k] == true)
                            count++;
                    }
                    //如果与rule相关的属性上的相似度都没有达到阈值，则不将它写入文件
                    if (count > 1) {
                        continue;
                    }
                    flag = true;
                    bw.write(tid1 + " || ");
                    bw.write(tid2 + " || ");

                    for (int k = 0; k < Parameters.Anum; k++) {
                        pair.setSimilar(k, tmp_similarity[k]);
                        DecimalFormat df = new DecimalFormat("0.00");
                        String str_similarity = df.format(tmp_similarity[k]);
                        if (k != Parameters.Anum - 1) {
                            bw.write(str_similarity + " || ");
                        } else {
                            bw.write(str_similarity + "\n");
                        }
                    }

                    prigroup.pairs.addElement(pair); // 将初始化好的pair放入第一个大group中
                }
                bw.flush();

                if (!flag) {//没有任何一个tuple与之相似，则放入ignoredTuple中
                    ignoredTuples.add(tid1);
                }
            }

            bw.close();
            fw.close();
            System.out.println("Similarity File write finished.");

            //Write ignoredTupleID into file
            writeIntegerListToFile(ignoredTuples, ignoredTupleFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return prigroup;
    }

    public static void writeIntegerListToFile(ArrayList<Integer> list, String outURL) {
        try {
            File file = new File(outURL);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for(Integer tupleID: list){
                bw.write(tupleID.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
            fw.close();
            System.out.println("Write ignored TupleIDs that are smaller than similarity threshold into File finished.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        System.out.println(Similarity.editSimilarity("2", "5"));
    }

}
