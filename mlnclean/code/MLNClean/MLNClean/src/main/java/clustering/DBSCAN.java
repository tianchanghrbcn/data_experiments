package clustering;

import data.Tuple;
import spellchecker.SpellChecker;

import java.io.*;
import java.util.*;

/**
 * Created by gcc on 19-2-19.
 */
public class DBSCAN {

    public static String baseURL = "/data/nw/DC_ED/References_inner_and_outer/mlnclean/dataset";

    public static double eps = 1; //区域半径
    public static int MinPts = 2; //最小包含样本数, 密度


    public static double getDistance(Tuple t1, Tuple t2) {
        String s1 = Arrays.toString(t1.getContext());
        String s2 = Arrays.toString(t2.getContext());
        return SpellChecker.distance(s1, s2);
    }

    //for test
    public static double getDistance_test(Vector_xy t1, Vector_xy t2) {
        double x2 = Math.pow(t1.x - t2.x, 2);
        double y2 = Math.pow(t1.y - t2.y, 2);
        return Math.sqrt(x2 + y2);
    }

    //for test
    public static HashMap<Integer, Vector_xy> initAdjacentPoints_test(Vector_xy centroid, HashMap<Integer, Vector_xy> domain) {

        HashMap<Integer, Vector_xy> adjacentPoints = new HashMap<>();

        Iterator<Map.Entry<Integer, Vector_xy>> iter = domain.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Vector_xy> entry = iter.next();
            Vector_xy t = entry.getValue();

            double dist = getDistance_test(t, centroid);
            if (dist <= eps) {
                adjacentPoints.put(t.id, t);
            }
        }
        return adjacentPoints;
    }

    public static HashMap<Integer, dbscanTuple> initAdjacentPoints(Tuple centroid, HashMap<Integer, Tuple> domain) {

        HashMap<Integer, dbscanTuple> adjacentPoints = new HashMap<>();

        Iterator<Map.Entry<Integer, Tuple>> iter = domain.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Tuple> entry = iter.next();
            Tuple t = entry.getValue();
            double dist = getDistance(t, centroid);
            if (dist <= eps) {
                adjacentPoints.put(t.tupleID, new dbscanTuple(t));
            }
        }
        return adjacentPoints;
    }

    //for test
    public static ArrayList<Vector_xy> initCorePoints_test(HashMap<Integer, Vector_xy> dataset_map, int MinPts) {  //初始化核心对象集合
        ArrayList<Vector_xy> corePoints = new ArrayList<>();
        Iterator<Map.Entry<Integer, Vector_xy>> iter = dataset_map.entrySet().iterator();
        System.out.print("\n Core point List: ");
        while (iter.hasNext()) {
            Map.Entry<Integer, Vector_xy> entry = iter.next();
            Vector_xy d_t = entry.getValue();

            if (d_t.adjacentPoints.size() >= MinPts) {   //添加核心对象
                corePoints.add(d_t);
                System.out.print("x" + d_t.id);
            }
        }
        System.out.print("\n");
        return corePoints;
    }

    public static ArrayList<dbscanTuple> initCorePoints(HashMap<String, dbscanTuple> dataset_map, int MinPts) {  //初始化核心对象集合
        ArrayList<dbscanTuple> corePoints = new ArrayList<>();

        Iterator<Map.Entry<String, dbscanTuple>> iter = dataset_map.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<String, dbscanTuple> entry = iter.next();
            dbscanTuple d_t = entry.getValue();

            if (d_t.adjacentPoints.size() >= MinPts) {   //添加核心对象
                corePoints.add(d_t);
            }
        }
        return corePoints;
    }

    //for test
    public static HashMap<Integer, Vector_xy> intersection_test(HashMap<Integer, Vector_xy> c1, HashMap<Integer, Vector_xy> c2) {
        HashMap<Integer, Vector_xy> c3 = new HashMap<>();
        Iterator<Map.Entry<Integer, Vector_xy>> iter = c1.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Vector_xy> entry = iter.next();
            int tupleID = entry.getKey();
            if (c2.containsKey(tupleID)) {
                c3.put(tupleID, entry.getValue());
            }
        }
        return c3;
    }

    public static HashMap<Integer, dbscanTuple> intersection(HashMap<Integer, dbscanTuple> c1, HashMap<Integer, dbscanTuple> c2) {
        HashMap<Integer, dbscanTuple> c3 = new HashMap<>();
        Iterator<Map.Entry<Integer, dbscanTuple>> iter = c1.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, dbscanTuple> entry = iter.next();
            int tupleID = entry.getKey();
            if (c2.containsKey(tupleID)) {
                c3.put(tupleID, entry.getValue());
            }
        }
        return c3;
    }

    //for test
    public static HashMap<Integer, Vector_xy> intersection_test(ArrayList<Vector_xy> corePoints, HashMap<Integer, Vector_xy> c2) {
        HashMap<Integer, Vector_xy> c3 = new HashMap<>();
        for (Vector_xy corePoint : corePoints) {
            int tupleID = corePoint.id;
            if (c2.containsKey(tupleID)) {
                c3.put(tupleID, corePoint);
            }
        }
        return c3;
    }

    public static HashMap<Integer, dbscanTuple> intersection(ArrayList<dbscanTuple> corePoints, HashMap<Integer, dbscanTuple> c2) {
        HashMap<Integer, dbscanTuple> c3 = new HashMap<>();
        for (dbscanTuple corePoint : corePoints) {
            int tupleID = corePoint.tuple.tupleID;
            if (c2.containsKey(tupleID)) {
                c3.put(tupleID, corePoint);
            }
        }
        return c3;
    }

    //for test
    public static HashMap<Integer, Vector_xy> minus_test(HashMap<Integer, Vector_xy> c1, HashMap<Integer, Vector_xy> c2, int clusterID) {
        HashMap<Integer, Vector_xy> c3 = new HashMap<>();
        Iterator<Map.Entry<Integer, Vector_xy>> iter = c1.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Vector_xy> entry = iter.next();
            int tupleID = entry.getKey();
            if (!c2.containsKey(tupleID)) {
                Vector_xy dt = entry.getValue();
                dt.cluster = clusterID;
                System.out.print("x" + tupleID + " ");
                c3.put(tupleID, dt);
            }
        }
        System.out.print("}\n");
        return c3;
    }

    public static HashMap<Integer, dbscanTuple> minus(HashMap<Integer, dbscanTuple> c1, HashMap<Integer, dbscanTuple> c2, int clusterID) {
        HashMap<Integer, dbscanTuple> c3 = new HashMap<>();
        Iterator<Map.Entry<Integer, dbscanTuple>> iter = c1.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, dbscanTuple> entry = iter.next();
            int tupleID = entry.getKey();
            if (!c2.containsKey(tupleID)) {
                dbscanTuple dt = entry.getValue();
                dt.cluster = clusterID;
                System.out.print("x" + tupleID + " ");
                c3.put(tupleID, dt);
            }
        }
        System.out.print("}\n");
        return c3;
    }

    public HashMap<Integer, dbscanTuple> minus(HashMap<Integer, dbscanTuple> c1, HashMap<Integer, dbscanTuple> c2) {
        HashMap<Integer, dbscanTuple> c3 = new HashMap<>();
        Iterator<Map.Entry<Integer, dbscanTuple>> iter = c1.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, dbscanTuple> entry = iter.next();
            int tupleID = entry.getKey();
            if (!c2.containsKey(tupleID)) {
                dbscanTuple dt = entry.getValue();
                c3.put(tupleID, dt);
            }
        }
        return c3;
    }

    //for test
    public static ArrayList<Vector_xy> minus_test(ArrayList<Vector_xy> corePoints, HashMap<Integer, Vector_xy> c2) {
        ArrayList<Vector_xy> c3 = new ArrayList<>();

        for (Vector_xy corePoint : corePoints) {
            int tupleID = corePoint.id;
            if (!c2.containsKey(tupleID)) {
                c3.add(corePoint);
            }
        }
        return c3;
    }

    public static ArrayList<dbscanTuple> minus(ArrayList<dbscanTuple> corePoints, HashMap<Integer, dbscanTuple> c2) {
        ArrayList<dbscanTuple> c3 = new ArrayList<>();

        for (dbscanTuple corePoint : corePoints) {
            int tupleID = corePoint.tuple.tupleID;
            if (!c2.containsKey(tupleID)) {
                c3.add(corePoint);
            }
        }
        return c3;
    }

    //for test
    public static ArrayList<Vector_xy> CorePointMinus_test(ArrayList<Vector_xy> corePoints, HashMap<Integer, Vector_xy> c2) {

        //先做交集c1^c2
        HashMap<Integer, Vector_xy> intersection = intersection_test(corePoints, c2);

        ArrayList<Vector_xy> result = minus_test(corePoints, intersection);

        return result;
    }

    //核心对象删减
    public static ArrayList<dbscanTuple> CorePointMinus(ArrayList<dbscanTuple> corePoints, HashMap<Integer, dbscanTuple> c2) {

        //先做交集c1^c2
        HashMap<Integer, dbscanTuple> intersection = intersection(corePoints, c2);

        ArrayList<dbscanTuple> result = minus(corePoints, intersection);

        return result;
    }

    public static void test(String[] args) {

        HashMap<Integer, Vector_xy> domain = new HashMap<>();

        Vector_xy x1 = new Vector_xy(1, 0.697, 0.460);
        Vector_xy x2 = new Vector_xy(2, 0.774, 0.376);
        Vector_xy x3 = new Vector_xy(3, 0.634, 0.264);
        Vector_xy x4 = new Vector_xy(4, 0.608, 0.318);
        Vector_xy x5 = new Vector_xy(5, 0.556, 0.215);
        Vector_xy x6 = new Vector_xy(6, 0.403, 0.237);
        Vector_xy x7 = new Vector_xy(7, 0.481, 0.149);
        Vector_xy x8 = new Vector_xy(8, 0.437, 0.211);
        Vector_xy x9 = new Vector_xy(9, 0.666, 0.091);
        Vector_xy x10 = new Vector_xy(10, 0.243, 0.267);
        Vector_xy x11 = new Vector_xy(11, 0.245, 0.057);
        Vector_xy x12 = new Vector_xy(12, 0.343, 0.099);
        Vector_xy x13 = new Vector_xy(13, 0.639, 0.161);
        Vector_xy x14 = new Vector_xy(14, 0.657, 0.198);
        Vector_xy x15 = new Vector_xy(15, 0.360, 0.370);
        Vector_xy x16 = new Vector_xy(16, 0.593, 0.042);
        Vector_xy x17 = new Vector_xy(17, 0.719, 0.103);
        Vector_xy x18 = new Vector_xy(18, 0.359, 0.188);
        Vector_xy x19 = new Vector_xy(19, 0.339, 0.241);
        Vector_xy x20 = new Vector_xy(20, 0.282, 0.257);
        Vector_xy x21 = new Vector_xy(21, 0.748, 0.232);
        Vector_xy x22 = new Vector_xy(22, 0.714, 0.346);
        Vector_xy x23 = new Vector_xy(23, 0.483, 0.312);
        Vector_xy x24 = new Vector_xy(24, 0.478, 0.437);
        Vector_xy x25 = new Vector_xy(25, 0.525, 0.369);
        Vector_xy x26 = new Vector_xy(26, 0.751, 0.489);
        Vector_xy x27 = new Vector_xy(27, 0.532, 0.472);
        Vector_xy x28 = new Vector_xy(28, 0.473, 0.376);
        Vector_xy x29 = new Vector_xy(29, 0.725, 0.445);
        Vector_xy x30 = new Vector_xy(30, 0.446, 0.459);

        domain.put(1, x1);
        domain.put(2, x2);
        domain.put(3, x3);
        domain.put(4, x4);
        domain.put(5, x5);
        domain.put(6, x6);
        domain.put(7, x7);
        domain.put(8, x8);
        domain.put(9, x9);
        domain.put(10, x10);
        domain.put(11, x11);
        domain.put(12, x12);
        domain.put(13, x13);
        domain.put(14, x14);
        domain.put(15, x15);
        domain.put(16, x16);
        domain.put(17, x17);
        domain.put(18, x18);
        domain.put(19, x19);
        domain.put(20, x20);
        domain.put(21, x21);
        domain.put(22, x22);
        domain.put(23, x23);
        domain.put(24, x24);
        domain.put(25, x25);
        domain.put(26, x26);
        domain.put(27, x27);
        domain.put(28, x28);
        domain.put(29, x29);
        domain.put(30, x30);

        run_test(domain, eps, MinPts);
    }

    public static void main(String[] args) {

        String fileURL = "/data/nw/DC_ED/References_inner_and_outer/mlnclean/dataset/HAI/rawData/test/0.01_0.05/domainList.txt";

        List<HashMap<Integer, Tuple>> domainList = readDomainFile(fileURL);
        List<List<HashMap<Integer, dbscanTuple>>> domain_to_clusters = new ArrayList<>();

        int i = 0;
        for (HashMap<Integer, Tuple> domain : domainList) {
            if (i == 1) break;
            System.out.println("generate clusters for domain...");
            ArrayList<HashMap<Integer, dbscanTuple>> clusterList = run(domain, eps, MinPts);
            domain_to_clusters.add(clusterList);
            i++;
        }

        try {
            writeClustersToFile(domain_to_clusters, "/data/nw/DC_ED/References_inner_and_outer/mlnclean/dataset/HAI/rawData/test/0.01_0.05/domain_to_clusters.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void writeClustersToFile(List<List<HashMap<Integer, dbscanTuple>>> domain_to_clusters, String filename) throws IOException {
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


            for (int i = 0; i < domain_to_clusters.size(); i++) {
                List<HashMap<Integer, dbscanTuple>> groups = domain_to_clusters.get(i);

                if (groups.size() != 0) {
                    writer.write("domain" + i + "\n");
                }
                int group_index = 0;
                for (HashMap<Integer, dbscanTuple> group : groups) {
                    writer.write("group" + group_index + "\n");
                    Iterator<Map.Entry<Integer, dbscanTuple>> iter = group.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<Integer, dbscanTuple> entry = iter.next();
                        Tuple t = entry.getValue().tuple;
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

    //for test
    public static HashMap<Integer, Vector_xy> copy_test(HashMap<Integer, Vector_xy> oldMap) {

        HashMap<Integer, Vector_xy> newMap = new HashMap<>();

        Iterator<Map.Entry<Integer, Vector_xy>> iter0 = oldMap.entrySet().iterator();
        while (iter0.hasNext()) {
            Map.Entry<Integer, Vector_xy> entry = iter0.next();
            Vector_xy d_t = entry.getValue();
            newMap.put(d_t.id, d_t);
        }
        return newMap;
    }

    public static HashMap<Integer, dbscanTuple> copy(HashMap<Integer, dbscanTuple> oldMap) {

        HashMap<Integer, dbscanTuple> newMap = new HashMap<>();

        Iterator<Map.Entry<Integer, dbscanTuple>> iter0 = oldMap.entrySet().iterator();
        while (iter0.hasNext()) {
            Map.Entry<Integer, dbscanTuple> entry = iter0.next();
            dbscanTuple d_t = entry.getValue();
            newMap.put(d_t.tuple.tupleID, d_t);
        }
        return newMap;
    }

    //for test
    public static void run_test(HashMap<Integer, Vector_xy> domain, double eps, int MinPts) {
        int size = domain.size();
        int clusterID = 0;  //初始化聚类簇数
        int idx;

        ArrayList<HashMap<Integer, Vector_xy>> clusterList = new ArrayList<>();

        //确定每个样本的邻域
        HashMap<Integer, Vector_xy> dataset_map = new HashMap<>();
        Iterator<Map.Entry<Integer, Vector_xy>> iter0 = domain.entrySet().iterator();
        while (iter0.hasNext()) {
            Map.Entry<Integer, Vector_xy> entry = iter0.next();
            Vector_xy d_t = entry.getValue();
            d_t.adjacentPoints = initAdjacentPoints_test(d_t, domain);
            dataset_map.put(d_t.id, d_t);
        }

        ArrayList<Vector_xy> corePoints = initCorePoints_test(dataset_map, MinPts); //初始化核心对象
        HashMap<Integer, Vector_xy> notVisitedPoints = new HashMap<>();   //初始化未访问队列
        Iterator<Map.Entry<Integer, Vector_xy>> iter = domain.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Vector_xy> entry = iter.next();
            Vector_xy t = entry.getValue();
            notVisitedPoints.put(t.id, t);
        }

        HashMap<Integer, Vector_xy> visitedPoints = new HashMap<>();      //初始化已访问队列

        while (corePoints.size() > 0) {

            HashMap<Integer, Vector_xy> old_notVisitedPoints = copy_test(notVisitedPoints);//记录当前未访问样本集合
            Vector_xy o = corePoints.get((int) Math.round(Math.random() * (corePoints.size() - 1)));  //随机选取一个核心对象

            Queue<Vector_xy> Q = new LinkedList<>();
            Q.offer(o);//初始化队列Q
            notVisitedPoints.remove(o.id);

            while (Q.size() > 0) {
                Vector_xy q = Q.poll();    //取出队列首个样本q
                HashMap<Integer, Vector_xy> adjacentPoints = dataset_map.get(q.id).adjacentPoints;
                if (adjacentPoints.size() >= MinPts) {
                    HashMap<Integer, Vector_xy> inter = intersection_test(adjacentPoints, notVisitedPoints); //△

                    Iterator<Map.Entry<Integer, Vector_xy>> it = inter.entrySet().iterator();

                    while (it.hasNext()) {    //将△中的样本加入队列Q
                        Map.Entry<Integer, Vector_xy> entry = it.next();
                        int tupleID = entry.getKey();
                        Vector_xy dt = entry.getValue();
                        Q.offer(dt);
                        notVisitedPoints.remove(dt.id);
                    }
                }
            }
            clusterID++;
            System.out.print("Cluster " + clusterID + "={");
            HashMap<Integer, Vector_xy> cluster = minus_test(old_notVisitedPoints, notVisitedPoints, clusterID);
            clusterList.add(cluster);
            corePoints = CorePointMinus_test(corePoints, cluster);
        }
    }

    public static ArrayList<HashMap<Integer, dbscanTuple>> run(HashMap<Integer, Tuple> domain, double eps, int MinPts) {
        int size = domain.size();
        int clusterID = 0;  //初始化聚类簇数
        int idx;

        ArrayList<HashMap<Integer, dbscanTuple>> clusterList = new ArrayList<>();

        //确定每个样本的邻域
        HashMap<String, dbscanTuple> dataset_map = new HashMap<>();
        Iterator<Map.Entry<Integer, Tuple>> iter0 = domain.entrySet().iterator();
        while (iter0.hasNext()) {
            Map.Entry<Integer, Tuple> entry = iter0.next();
            Tuple t = entry.getValue();
            dbscanTuple d_t = new dbscanTuple(t);
            d_t.adjacentPoints = initAdjacentPoints(t, domain);
            dataset_map.put(Arrays.toString(t.getContext()), d_t);
        }

        System.out.println("init corePoints");
        ArrayList<dbscanTuple> corePoints = initCorePoints(dataset_map, MinPts); //初始化核心对象
        HashMap<Integer, dbscanTuple> notVisitedPoints = new HashMap<>();   //初始化未访问队列
        Iterator<Map.Entry<Integer, Tuple>> iter = domain.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Tuple> entry = iter.next();
            Tuple t = entry.getValue();
            notVisitedPoints.put(t.tupleID, new dbscanTuple(t));
        }

        HashMap<Integer, dbscanTuple> visitedPoints = new HashMap<>();      //初始化已访问队列

        while (corePoints.size() > 0) {

            HashMap<Integer, dbscanTuple> old_notVisitedPoints = copy(notVisitedPoints);//记录当前未访问样本集合
            dbscanTuple o = corePoints.get(0);  //随机选取一个核心对象

            Queue<dbscanTuple> Q = new LinkedList<>();
            Q.offer(o);//初始化队列Q
            notVisitedPoints.remove(o.tuple.tupleID);

            while (Q.size() > 0) {
                dbscanTuple q = Q.poll();    //取出队列首个样本q
                HashMap<Integer, dbscanTuple> adjacentPoints = dataset_map.get(Arrays.toString(q.tuple.getContext())).adjacentPoints;
                if (adjacentPoints.size() >= MinPts) {
                    HashMap<Integer, dbscanTuple> inter = intersection(adjacentPoints, notVisitedPoints); //△

                    Iterator<Map.Entry<Integer, dbscanTuple>> it = inter.entrySet().iterator();

                    while (it.hasNext()) {    //将△中的样本加入队列Q
                        Map.Entry<Integer, dbscanTuple> entry = it.next();
                        int tupleID = entry.getKey();
                        dbscanTuple dt = entry.getValue();
                        Q.offer(dt);
                        notVisitedPoints.remove(dt.tuple.tupleID);
                    }
                }
            }
            clusterID++;
            System.out.print("Cluster" + clusterID + "={");
            HashMap<Integer, dbscanTuple> cluster = minus(old_notVisitedPoints, notVisitedPoints, clusterID);
            clusterList.add(cluster);
            corePoints = CorePointMinus(corePoints, cluster);
        }
        return clusterList;
    }

    public static List<HashMap<Integer, Tuple>> readDomainFile(String fileURL) {
        FileReader reader;
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
                    String[] ids = str.replaceAll("AttrID:", "").replaceAll(", ", ",").split(",");
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
                    Tuple t = new Tuple();
                    t.tupleID = key;
                    domainList.get(domain_index).put(key, t);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return domainList;
    }


}
