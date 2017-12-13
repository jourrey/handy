package com.commissar.handy.dispatch;

import com.commissar.handy.dispatch.demo.DemoContainerCurator;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@FixMethodOrder()
public class DispatcherTest {

    private ContainerFeature beijingFeature = Dispatcher.containerFeature("beijing");
    private ContainerFeature shanghaiFeature = Dispatcher.containerFeature("shanghai");
    private ContainerCurator bigPrinceCurator;
    private ContainerCurator smallPrinceCurator;

    @Before
    public void initCondition() {
        bigPrinceCurator = new DemoContainerCurator("大王子");
        smallPrinceCurator = new DemoContainerCurator("小王子");
    }

    @After
    public void cleanCondition() {
        Dispatcher.unRegister(beijingFeature, bigPrinceCurator);
        Dispatcher.unRegister(shanghaiFeature, smallPrinceCurator);
    }

    @Test
    public void testContainerFeature() {
        ContainerFeature shanghaiFeature2 = Dispatcher.containerFeature("shanghai");

        assertThat(beijingFeature, not(shanghaiFeature));
        assertThat(shanghaiFeature, is(shanghaiFeature2));
    }

    @Test
    public void testRegister() {
        assertThat(Dispatcher.register(beijingFeature, bigPrinceCurator), is(true));
        assertThat(Dispatcher.register(shanghaiFeature, smallPrinceCurator), is(true));
        assertThat(Dispatcher.register(beijingFeature, bigPrinceCurator), is(false));
        assertThat(Dispatcher.register(shanghaiFeature, smallPrinceCurator), is(false));
    }

    @Test
    public void testUnRegister() {
        Dispatcher.register(beijingFeature, bigPrinceCurator);
        Dispatcher.register(shanghaiFeature, smallPrinceCurator);

        assertThat(Dispatcher.unRegister(beijingFeature, bigPrinceCurator), is(true));
        assertThat(Dispatcher.unRegister(shanghaiFeature, smallPrinceCurator), is(true));
        assertThat(Dispatcher.unRegister(beijingFeature, bigPrinceCurator), is(false));
        assertThat(Dispatcher.unRegister(shanghaiFeature, smallPrinceCurator), is(false));
    }

    @Test
    public void testAddContainerProxy() {
        Dispatcher.register(beijingFeature, bigPrinceCurator);
        Dispatcher.register(shanghaiFeature, smallPrinceCurator);

        String miniPrince = "Why don't you eat it";
        assertThat(Dispatcher.addContainerProxy(smallPrinceCurator.unique(), miniPrince), is(true));
        assertThat(Dispatcher.addContainerProxy(smallPrinceCurator.unique(), miniPrince), is(false));
        assertThat(Dispatcher.addContainerProxy(bigPrinceCurator.unique(), miniPrince), is(false));
    }

    @Test
    public void testDelContainerProxy() {
        Dispatcher.register(beijingFeature, bigPrinceCurator);
        Dispatcher.register(shanghaiFeature, smallPrinceCurator);
        String miniPrince = "Why don't you eat it";
        Dispatcher.addContainerProxy(smallPrinceCurator.unique(), miniPrince);
        ContainerProxy smallPrinceProxy = Dispatcher.alloc(shanghaiFeature);

        assertThat(Dispatcher.delContainerProxy(smallPrinceProxy), is(true));
    }

    @Test(expected = NullPointerException.class)
    public void testDelContainerProxy_Ex() {
        Dispatcher.register(beijingFeature, bigPrinceCurator);
        Dispatcher.register(shanghaiFeature, smallPrinceCurator);
        ContainerProxy bigPrinceProxy = Dispatcher.alloc(beijingFeature);

        assertThat(Dispatcher.delContainerProxy(bigPrinceProxy), is(false));
    }

    @Test
    public void testAlloc() {
        Dispatcher.register(beijingFeature, bigPrinceCurator);
        Dispatcher.register(shanghaiFeature, smallPrinceCurator);
        String miniPrince = "Why don't you eat it";
        Dispatcher.addContainerProxy(smallPrinceCurator.unique(), miniPrince);

        // 不可用
        ContainerProxy bigPrinceProxy = Dispatcher.alloc(beijingFeature);
        assertNull(bigPrinceProxy);
        // 可用
        ContainerProxy smallPrinceProxy = Dispatcher.alloc(shanghaiFeature);
        assertNotNull(smallPrinceProxy);
        assertThat(smallPrinceProxy.isAvailable(), is(true));
        assertThat(smallPrinceProxy.containerCuratorUnique(), is(smallPrinceCurator.unique()));
        assertThat(smallPrinceProxy.unique(), is(miniPrince));
    }

    @Test
    public void testFree() {
        Dispatcher.register(beijingFeature, bigPrinceCurator);
        Dispatcher.register(shanghaiFeature, smallPrinceCurator);
        String miniPrince = "Why don't you eat it";
        Dispatcher.addContainerProxy(smallPrinceCurator.unique(), miniPrince);
        ContainerProxy smallPrinceProxy = Dispatcher.alloc(shanghaiFeature);

        assertThat(Dispatcher.free(smallPrinceProxy), is(true));
    }

    @Test(expected = NullPointerException.class)
    public void testFree_Ex() {
        Dispatcher.register(beijingFeature, bigPrinceCurator);
        Dispatcher.register(shanghaiFeature, smallPrinceCurator);
        ContainerProxy bigPrinceProxy = Dispatcher.alloc(beijingFeature);

        assertThat(Dispatcher.free(bigPrinceProxy), is(false));
    }

}
