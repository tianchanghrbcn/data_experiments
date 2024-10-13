package clustering;

import data.Tuple;
import spellchecker.SpellChecker;

import java.util.*;

/**
 * Created by gcc on 18-9-29.
 */
public class Kmeans {

    public static int k = 3;
    public static int maxIterNum = 10;

    class DisTuple {
        Tuple tuple;
        int dist;

        DisTuple(Tuple t) {
            this.tuple = t;
            dist = 0;
        }

        DisTuple(Tuple t, int dist) {
            this.tuple = t;
            this.dist = dist;
        }
    }

    public ArrayList<DisTuple> initCenters(HashMap<Integer, Tuple> domain, int k) {
        ArrayList<DisTuple> centroids = new ArrayList<>(k);

        //copy domain tupleIDs
        List<Integer> tupleIDs = new ArrayList<>(domain.size());
        Iterator<Map.Entry<Integer, Tuple>> iter = domain.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Tuple> entry = iter.next();
            int tupleID = entry.getKey();
            tupleIDs.add(tupleID);
        }

        Random random = new Random();
        for (int i = 0; i < k; i++) {
            int randKey = random.nextInt(k);
            int centroidID = tupleIDs.get(randKey);
            centroids.add(new DisTuple(domain.get(centroidID)));
        }

        return centroids;
    }

    public void train(HashMap<Integer, Tuple> domain, int k) {

        int iterNum = 0;

        ArrayList<ArrayList<DisTuple>> clusterings = new ArrayList<>(k);
        ArrayList<DisTuple> dataList = new ArrayList<>(domain.size());
        for (int i = 0; i < k; i++) {
            clusterings.set(i, new ArrayList<>());
        }

        ArrayList<DisTuple> first_centroids = initCenters(domain, k);//初始化中心点
        ArrayList<Double> centroids = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            centroids.add(0d);
        }

        //第一次聚类
        Iterator<Map.Entry<Integer, Tuple>> iter = domain.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Tuple> entry = iter.next();
            int tupleID = entry.getKey();
            Tuple t = entry.getValue();
            String content = Arrays.toString(t.getContext());

            int minDis = 99;
            int match_centroIndex = 0;
            for (int i = 0; i < first_centroids.size(); i++) {
                DisTuple centroid = first_centroids.get(i);
                String centroContent = Arrays.toString(centroid.tuple.getContext());
                int dist = SpellChecker.distance(content, centroContent);
                if (dist < minDis) {
                    minDis = dist;
                    match_centroIndex = i;//找到匹配的第i个中心点
                }
            }
            DisTuple disTuple = new DisTuple(t, minDis);
            clusterings.get(match_centroIndex).add(disTuple);//将样本t加入对应的clustering中
            dataList.add(disTuple);
        }

        while (iterNum++ < maxIterNum) {

            boolean updataFlag = false;
            int count_updata_num = 0;

            //计算新的中心点
            for (int m = 0; m < k; m++) {
                ArrayList<DisTuple> cluster = clusterings.get(m);

                int totalDist = 0;
                for (DisTuple disTuple : cluster) {
                    int dist = disTuple.dist;
                    totalDist += dist;
                }
                double centro_dist = totalDist / cluster.size();
                double old_centro_dist = centroids.get(m);
                double threshold = Math.abs(old_centro_dist - centro_dist);
                if (threshold != 0) {
                    centroids.set(m, centro_dist);
                    count_updata_num++;
                }
            }

            if (count_updata_num != 0) {
                for (int m = 0; m < k; m++) {
                    ArrayList<DisTuple> cluster = clusterings.get(m);
                    cluster.clear();//重置每个clustering
                }
            } else {
                break;//当前中心点均未更新
            }

            double minDis = 99;
            int match_centroIndex = 0;
            for (DisTuple disTuple : dataList) {
                int dist = disTuple.dist;
                for (int i = 0; i < centroids.size(); i++) {
                    double centroid_dist = centroids.get(i);
                    double curr_dist = Math.abs(dist - centroid_dist);
                    if (curr_dist < minDis) {
                        minDis = curr_dist;
                        match_centroIndex = i;//找到匹配的第i个中心点
                    }
                }
                clusterings.get(match_centroIndex).add(disTuple);//将样本t加入对应的clustering中
            }
        }

        //print
        int cluster_i = 0;
        for (ArrayList<DisTuple> cluster : clusterings) {
            System.out.println("\nclustering " + cluster_i);
            for (DisTuple dt : cluster) {
                Tuple tuple = dt.tuple;
                System.out.println("Tuple ID = " + tuple.tupleID + "\t" + Arrays.toString(tuple.getContext()));
            }
            cluster_i++;
        }

    }

    public static void main(String[] args) {

    }
}
