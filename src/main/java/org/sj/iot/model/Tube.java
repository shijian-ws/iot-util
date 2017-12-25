package org.sj.iot.model;

/**
 * 单一映射
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2017-12-12
 */
public final class Tube<K, V> {
    public K k;
    public V v;

    private Tube() {
    }

    public static <K, V> Tube<K, V> of(K k) {
        return of(k, null);
    }

    public static <K, V> Tube<K, V> of(K k, V v) {
        Tube tube = new Tube();
        tube.k = k;
        tube.v = v;
        return tube;
    }
}
