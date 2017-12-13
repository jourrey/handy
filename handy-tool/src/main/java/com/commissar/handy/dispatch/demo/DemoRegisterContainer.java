package com.commissar.handy.dispatch.demo;


import com.commissar.handy.dispatch.ContainerFeature;
import com.commissar.handy.dispatch.ContainerProxy;
import com.commissar.handy.dispatch.Dispatcher;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class DemoRegisterContainer {

    public static void main(String[] args) {
        Region shanghai = new Region("shanghai");
        ContainerFeature shanghaiFeature = Dispatcher.containerFeature(shanghai);
        DemoContainerCurator demoContainerCurator = new DemoContainerCurator("shanghai");
        // 注册容器管理器
        Dispatcher.register(shanghaiFeature, demoContainerCurator);
        // 添加容器代理
        Dispatcher.addContainerProxy(demoContainerCurator.unique(), "001");
        Dispatcher.addContainerProxy(demoContainerCurator.unique(), "002");
        Dispatcher.addContainerProxy(demoContainerCurator.unique(), "003");
        // 申请容器代理
        ContainerProxy<Integer, String> containerProxy = Dispatcher.alloc(shanghaiFeature);
        // 容器代理是否可用
        containerProxy.isAvailable();
        // 投放数据
        containerProxy.process(1);
        // 使用完成,归还资源
        Dispatcher.free(containerProxy);
    }

    static class Region {
        private String name;

        public Region(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Region region = (Region) o;

            return new EqualsBuilder()
                    .append(name, region.name)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(name)
                    .toHashCode();
        }
    }

}
