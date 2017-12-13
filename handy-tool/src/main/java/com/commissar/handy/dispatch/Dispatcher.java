package com.commissar.handy.dispatch;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 派发器
 *
 * @author babu
 */
public class Dispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(Dispatcher.class);
    /**
     * ContainerFeature缓存,提供唯一性保证
     * 根据feature维护对应的ContainerFeature
     * Key理论上应该设置成ContainerFeature的feature,但是这里没这么做:
     * 1.是觉得丑,Dispatcher只认识ContainerFeature,ContainerCurator和ContainerProxy;
     * 2.为了扩展,对ContainerFeature透明;
     */
    private static final Map<ContainerFeature, ContainerFeature> FEATURE_POOL = Maps.newConcurrentMap();
    /**
     * ContainerCurator缓存,以ContainerFeature为粒度的聚合
     */
    private static final Multimap<ContainerFeature, ContainerCurator> CURATOR_POOL = LinkedHashMultimap.create();
    /**
     * ContainerCurator Unique -> 容器管理者
     * 确保 ContainerCuratorUnique 唯一性
     * value是容器管理者,因为ContainerCurator已经存在,不会占用更多资源;后期添加监控和维护功能更方便,留个入口
     * 要求对外不可见
     */
    private static final Map<String, ContainerCurator> CURATOR_CACHE = Maps.newConcurrentMap();
    /**
     * ContainerProxy Unique -> ContainerProxy Unique
     * 确保 ContainerProxyUnique 唯一性
     * 要求对外不可见
     */
    private static final Map<String, String> PROXY_UNIQUE_CACHE = Maps.newConcurrentMap();
    /**
     * ContainerCurator Unique -> ContainerProxy Unique
     * 用于 {@link Dispatcher#unRegister(ContainerFeature, ContainerCurator)} 清理 PROXY_UNIQUE_CACHE
     * 要求对外不可见
     */
    private static final Multimap<String, String> CURATOR_PROXY_UNIQUE_MAPPING_CACHE = LinkedHashMultimap.create();

    /**
     * 根据特征获取ContainerFeature
     *
     * @param feature 容器特征
     * @param <F>     容器特征数据类型
     * @return 容器特征, 保证唯一
     */
    public static <F> ContainerFeature<F> containerFeature(F feature) {
        checkNotNull(feature, "feature can not be null");

        ContainerFeature containerFeature = new ContainerFeature(feature);
        ContainerFeature existsFeature = FEATURE_POOL.putIfAbsent(containerFeature, containerFeature);
        if (existsFeature != null) {
            containerFeature = existsFeature;
        }

        return containerFeature;
    }

    /**
     * 注册容器管理者
     *
     * @param containerFeature 容器特征
     * @param containerCurator 容器管理者
     * @param <F>              容器特征数据类型
     * @param <P>              生产者参数类型
     * @param <C>              消费者参数类型
     * @return 是否成功
     */
    public static <F, P, C> boolean register(ContainerFeature<F> containerFeature
            , ContainerCurator<P, C> containerCurator) {
        checkNotNull(containerFeature, "containerFeature can not be null");
        checkNotNull(containerCurator, "containerCurator can not be null");
        checkNotNull(containerCurator.unique(), "containerCurator unique can not be null");
        LOG.debug("register containerCurator:{}", containerCurator.unique());

        // 防止伪造
        ContainerFeature<F> finalContainerFeature = containerFeature(containerFeature.getFeature());

        final boolean[] operation = {false};
        CURATOR_CACHE.computeIfAbsent(containerCurator.unique(), s -> {
            if (CURATOR_POOL.put(finalContainerFeature, containerCurator)) {
                operation[0] = true;
                return containerCurator;
            }
            return null;
        });

        return operation[0];
    }

    /**
     * 注销容器管理者
     *
     * @param containerFeature 容器特征
     * @param containerCurator 容器管理者
     * @param <F>              容器特征数据类型
     * @param <P>              生产者参数类型
     * @param <C>              消费者参数类型
     * @return 是否成功
     */
    public static <F, P, C> boolean unRegister(ContainerFeature<F> containerFeature
            , ContainerCurator<P, C> containerCurator) {
        checkNotNull(containerFeature, "containerFeature can not be null");
        checkNotNull(containerCurator, "containerCurator can not be null");
        checkNotNull(containerCurator.unique(), "containerCurator unique can not be null");
        LOG.debug("unRegister containerCurator:{}", containerCurator.unique());

        // 防止伪造
        ContainerFeature<F> finalContainerFeature = containerFeature(containerFeature.getFeature());

        final boolean[] operation = {false};
        CURATOR_CACHE.computeIfPresent(containerCurator.unique(), (s, containerCurator1) -> {
            // 容器管理者必须是原对象
            if (containerCurator == containerCurator1
                    && CURATOR_POOL.remove(finalContainerFeature, containerCurator1)) {
                operation[0] = containerCurator1.shutdown();
                CURATOR_PROXY_UNIQUE_MAPPING_CACHE.get(containerCurator1.unique())
                        .forEach(s1 -> PROXY_UNIQUE_CACHE.remove(s1));
                CURATOR_PROXY_UNIQUE_MAPPING_CACHE.removeAll(containerCurator1.unique());
                return null;
            }
            return containerCurator1;
        });

        return operation[0];
    }

    /**
     * 增加容器代理
     *
     * @param containerCuratorUnique 容器管理者唯一标记
     * @param containerProxyUnique   容器代理唯一标记
     * @return 是否成功
     */
    public static boolean addContainerProxy(String containerCuratorUnique, String containerProxyUnique) {
        checkNotNull(containerCuratorUnique, "containerCuratorUnique can not be null");
        checkNotNull(containerProxyUnique, "containerProxyUnique can not be null");

        final boolean[] operation = {false};
        CURATOR_CACHE.computeIfPresent(containerCuratorUnique, (s, containerCurator) -> {
            PROXY_UNIQUE_CACHE.computeIfAbsent(containerProxyUnique, s1 -> {
                if (containerCurator.add(containerProxyUnique)) {
                    operation[0] = true;
                    CURATOR_PROXY_UNIQUE_MAPPING_CACHE.put(containerCuratorUnique, containerProxyUnique);
                    return containerProxyUnique;
                }
                return null;
            });
            return containerCurator;
        });

        return operation[0];
    }

    /**
     * 删除容器代理
     *
     * @param containerProxy 容器代理
     * @param <P>            生产者参数类型
     * @param <C>            消费者参数类型
     * @return 是否成功
     */
    public static <P, C> boolean delContainerProxy(ContainerProxy<P, C> containerProxy) {
        checkNotNull(containerProxy, "containerProxy can not be null");
        checkNotNull(containerProxy.unique(), "containerProxy unique can not be null");
        checkNotNull(containerProxy.containerCuratorUnique(), "containerProxy containerCuratorUnique can not be null");

        final boolean[] operation = {false};
        CURATOR_CACHE.computeIfPresent(containerProxy.containerCuratorUnique(), (s, containerCurator) -> {
            PROXY_UNIQUE_CACHE.computeIfPresent(containerProxy.unique(), (s1, s2) -> {
                if (containerCurator.del(containerProxy)) {
                    operation[0] = true;
                    CURATOR_PROXY_UNIQUE_MAPPING_CACHE.remove(containerProxy.containerCuratorUnique()
                            , containerProxy.unique());
                    return null;
                }
                return containerProxy.unique();
            });
            return containerCurator;
        });

        return operation[0];
    }

    /**
     * 申请容器代理
     *
     * @param containerFeature 容器特征,根据容器特征,分配对应引擎的Worker
     * @param <F>              容器特征数据类型
     * @param <P>              生产者参数类型
     * @param <C>              消费者参数类型
     * @return 容器代理 可能返回null
     * @// TODO: 17/12/1 分配时当前如果没有资源直接抛弃,后续可以新增策略,对于压测来说,不影响
     */
    public static <F, P, C> ContainerProxy<P, C> alloc(ContainerFeature<F> containerFeature) {
        checkNotNull(containerFeature, "containerFeature can not be null");
        Optional<ContainerCurator> curatorOptional = CURATOR_POOL.get(containerFeature).stream()
                .filter(containerCurator -> containerCurator.remaining() > 0).findAny();
        return curatorOptional.isPresent() ? curatorOptional.get().alloc() : null;
    }

    /**
     * 归还容器代理
     *
     * @param containerProxy 容器代理
     * @param <P>            生产者参数类型
     * @param <C>            消费者参数类型
     * @return 是否成功
     */
    public static <P, C> boolean free(ContainerProxy<P, C> containerProxy) {
        checkNotNull(containerProxy, "containerProxy can not be null");
        checkNotNull(containerProxy.unique(), "containerProxy unique can not be null");
        checkNotNull(containerProxy.containerCuratorUnique(), "containerProxy containerCuratorUnique can not be null");

        final boolean[] operation = {false};
        CURATOR_CACHE.computeIfPresent(containerProxy.containerCuratorUnique(), (s, containerCurator) -> {
            operation[0] = containerCurator.free(containerProxy);
            return containerCurator;
        });

        return operation[0];
    }

}
