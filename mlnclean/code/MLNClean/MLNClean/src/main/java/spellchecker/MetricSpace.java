package spellchecker;

/**
 * 度量空间
 */
public interface MetricSpace<T> {

    double distance(T a, T b);

}