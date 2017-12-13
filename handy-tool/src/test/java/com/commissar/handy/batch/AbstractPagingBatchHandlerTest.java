package com.commissar.handy.batch;

import com.google.common.collect.Lists;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@FixMethodOrder()
public class AbstractPagingBatchHandlerTest {

    @Test
    public void testExecute() throws Exception {
        Integer[] handleCount = new Integer[]{0};
        List<String> data = Lists.newArrayList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
        AbstractPagingBatchHandler<String> pagingBatchHandler = new AbstractPagingBatchHandler<String>() {
            @Override
            public int count(Object... params) {
                return data.size();
            }

            @Override
            public List<String> find(int offset, int limit, Object... params) {
                return data.subList(offset, offset + limit);
            }

            @Override
            public int handleAndReturnOffset(String s, Object... params) {
                handleCount[0] = handleCount[0] + 1;
                // 测试最小是1的控制
                return 0;
            }
        };
        pagingBatchHandler.setBatchLimit(3);
        pagingBatchHandler.execute("hello", "xiaomangzi");
        // 10 11 12 大于1 小于2 不会被执行
        assertThat(handleCount[0], is(10));
    }

    @Test
    public void testExecute_Override() throws Exception {
        Integer[] handleCount = new Integer[]{0};
        List<String> data = Lists.newArrayList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12");
        AbstractPagingBatchHandler<String> pagingBatchHandler = new AbstractPagingBatchHandler<String>() {
            @Override
            public int count(Object... params) {
                return data.size();
            }

            @Override
            public List<String> find(int offset, int limit, Object... params) {
                return data.subList(offset, offset + limit);
            }

            /**
             * 默认不符合要求,按数字大小排序
             */
            @Override
            public int compareTo(String t1, String t2, Object... params) throws Exception {
                return Integer.valueOf(t1).compareTo(Integer.valueOf(t2));
            }

            @Override
            public int handleAndReturnOffset(String s, Object... params) {
                handleCount[0] = handleCount[0] + 1;
                // 测试最小是1的控制
                return 0;
            }
        };
        pagingBatchHandler.setBatchLimit(3);
        pagingBatchHandler.execute("hello", "xiaomangzi");
        assertThat(handleCount[0], is(13));
    }

}
