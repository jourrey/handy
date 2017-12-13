package com.commissar.handy.batch;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

/**
 * 分页批处理辅助类
 * 主要解决的场景是,一些任务处理的数据比较多,内存不可能一次性放进来,这时候需要分批处理
 * 限制条件是,待处理的数据,必须是可排序、可比较的
 *
 * @param <T> 处理数据类型
 * @author babu
 */
public abstract class AbstractPagingBatchHandler<T> {

    /**
     * 批处理上限
     */
    private int batchLimit = 100;

    protected int getBatchLimit() {
        return batchLimit;
    }

    protected void setBatchLimit(int batchLimit) {
        this.batchLimit = batchLimit;
    }

    /**
     * 执行分页批处理任务
     *
     * @param params 一些需要透传的参数
     * @throws Exception 抛出异常,退出整个Handler
     */
    public void execute(Object... params) throws Exception {
        int count = count(params);
        List<T> list;
        T lastT = null;
        int returnOffset;
        int limit;
        int offset = 0;
        while (count > 0) {
            limit = count > batchLimit ? batchLimit : count;
            count -= limit;
            list = find(offset, limit, params);
            list = list == null ? Collections.EMPTY_LIST : list;
            for (T t : list) {
                if (lastT == null || (compareTo(lastT, t, params) < 0)) {
                    lastT = t;
                    returnOffset = handleAndReturnOffset(t, params);
                    offset += returnOffset < 1 ? 1 : returnOffset;
                } else {
                    offset++;
                }
            }
        }
    }

    /**
     * 需处理的数据数量
     *
     * @param params 一些需要透传的参数
     * @return 数量
     */
    public abstract int count(Object... params);

    /**
     * 查询从offset开始共limit条记录
     * 确保是全局有序的,否则会导致部分数据被跳过处理
     *
     * @param offset 起始位置
     * @param limit  偏移量
     * @param params 一些需要透传的参数
     * @return 有序的待处理数据
     */
    public abstract List<T> find(int offset, int limit, Object... params);

    /**
     * 比较find返回的数据大小,目的是为了防止数据被重复处理
     * 期望逻辑:待数据 > 已处理的最大数据,才会被处理
     * 使用前提:
     * 1.find方法返回的结果必须是全局经过排序的
     * 2.数据对象必须实现Comparable接口
     * 3.find排序规则和Comparable需要一致,默认是递增,如不同,请重写
     *
     * @param baseline 已处理的最大数据
     * @param current  当前数据
     * @param params   一些需要透传的参数
     * @return -1,0,1
     * @throws Exception 抛出异常,退出整个Handler
     */
    public int compareTo(T baseline, T current, Object... params) throws Exception {
        if (baseline instanceof Comparable) {
            return ((Comparable) baseline).compareTo(current);
        }
        throw new NoSuchMethodException(MessageFormat.format("{0} not implements Comparable"
                , baseline.getClass()));
    }

    /**
     * 处理find返回的数据,且经过compareTo过滤
     *
     * @param t
     * @param params 一些需要透传的参数
     * @return 返回需要跳过的记录数, 从当前记录开始, 包含当前记录,
     * 最小值是1
     * 例如update一个用户的订单, 这时候一次执行了N条, 那么直接跳过, 提升效率,
     */
    public abstract int handleAndReturnOffset(T t, Object... params);

}
