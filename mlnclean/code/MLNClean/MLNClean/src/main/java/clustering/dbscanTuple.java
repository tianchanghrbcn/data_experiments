package clustering;

import data.Tuple;

import java.util.HashMap;

/**
 * Created by gcc on 19-2-22.
 */
public class dbscanTuple {
    Tuple tuple;
    boolean isVisited;  //访问标记
    int cluster;        //所属簇编号
    boolean isNoised;   //是否为噪声数据
    HashMap<Integer, dbscanTuple> adjacentPoints = new HashMap<>();  //该点的邻域

    public dbscanTuple(Tuple t) {
        this.tuple = t;
        this.isVisited = false;
        this.isNoised = false;
        this.cluster = -1;
    }

}
