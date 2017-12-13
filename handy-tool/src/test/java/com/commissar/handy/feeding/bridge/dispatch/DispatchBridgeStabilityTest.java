package com.commissar.handy.feeding.bridge.dispatch;

import com.commissar.handy.dispatch.ContainerCurator;
import com.commissar.handy.dispatch.ContainerFeature;
import com.commissar.handy.dispatch.ContainerProxy;
import com.commissar.handy.dispatch.Dispatcher;
import com.commissar.handy.dispatch.demo.DemoContainerCurator;
import com.commissar.handy.feeding.exception.UnavailableResourcesException;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@FixMethodOrder()
public class DispatchBridgeStabilityTest {

    private ContainerFeature shanghaiFeature = Dispatcher.containerFeature("shanghai");
    private ContainerCurator smallPrinceCurator;

    @Before
    public void initCondition() {
        smallPrinceCurator = new DemoContainerCurator("小王子");
        Dispatcher.register(shanghaiFeature, smallPrinceCurator);
    }

    @After
    public void cleanCondition() {
        Dispatcher.unRegister(shanghaiFeature, smallPrinceCurator);
    }

    @Test
    public void testGetContainerProxy() throws ExecutionException, UnavailableResourcesException, InterruptedException {
        String miniPrince0 = "Why don't you eat it";
        String miniPrince1 = "Oh my God, Don't say that";
        Dispatcher.addContainerProxy(smallPrinceCurator.unique(), miniPrince0);
        Dispatcher.addContainerProxy(smallPrinceCurator.unique(), miniPrince1);

        ContainerProxy containerProxy0 = DispatchBridgeStability.getContainerProxy("0", shanghaiFeature);
        assertThat(containerProxy0.isAvailable(), is(true));

        // 结论1:由于测试使用的DemoContainerCurator基于IsomorphicEngine,IsomorphicEngine是FIFO实现,第二次申请一定是另一个
        // Stability验证, 如果依然是containerProxy0, 表明缓存生效
        assertThat(DispatchBridgeStability.getContainerProxy("0", shanghaiFeature), is(containerProxy0));
        // 验证结论1
        ContainerProxy containerProxy1 = DispatchBridgeStability.getContainerProxy("1", shanghaiFeature);
        assertThat(containerProxy1.isAvailable(), is(true));
        assertThat(containerProxy1, not(containerProxy0));
        assertTrue(Dispatcher.free(containerProxy1));

        // EXPIRE_SECONDS验证, 与DispatchBridgeStability的EXPIRE_SECONDS一致
        long EXPIRE_SECONDS = 8;
        Thread.sleep(EXPIRE_SECONDS * 1000);
        ContainerProxy containerProxy2 = DispatchBridgeStability.getContainerProxy("0", shanghaiFeature);
        assertThat(containerProxy2.isAvailable(), is(true));
        assertThat(containerProxy2, not(containerProxy0));
        assertThat(containerProxy2, is(containerProxy1));
    }

    @Test(expected = UnavailableResourcesException.class)
    public void testGetContainerProxy_Ex() throws ExecutionException, UnavailableResourcesException, InterruptedException {
        DispatchBridgeStability.getContainerProxy("0", shanghaiFeature);
    }

}
