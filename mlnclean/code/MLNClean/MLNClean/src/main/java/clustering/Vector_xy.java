package clustering;

import java.util.HashMap;

/**
 * Created by gcc on 19-2-25.
 */
public class Vector_xy {
    int id;
    double x;
    double y;

    boolean isVisited;  //访问标记
    int cluster;        //所属簇编号
    boolean isNoised;   //是否为噪声数据

    HashMap<Integer, Vector_xy> adjacentPoints = new HashMap<>();  //该点的邻域

    Vector_xy(int id, double x, double y){
     this.id = id;
     this.x = x;
     this.y = y;
    }
}
