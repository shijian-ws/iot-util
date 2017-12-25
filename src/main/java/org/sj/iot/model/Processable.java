package org.sj.iot.model;

/**
 * 任务处理接口
 *
 * @author shijian
 * @email shijianws@163.com
 * @date 2017-09-10
 */
public interface Processable<T> {
    void process(T t);
}
