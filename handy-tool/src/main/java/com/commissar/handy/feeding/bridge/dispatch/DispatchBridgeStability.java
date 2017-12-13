package com.commissar.handy.feeding.bridge.dispatch;

import com.commissar.handy.dispatch.ContainerFeature;
import com.commissar.handy.dispatch.ContainerProxy;
import com.commissar.handy.dispatch.Dispatcher;
import com.commissar.handy.feeding.exception.UnavailableResourcesException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 桥接稳定器
 * 维持Producer与Consumer之间的忠诚度,有利于Consumer缓存高代价资源
 *
 * @author babu
 */
public class DispatchBridgeStability {
    private static final Logger LOG = LoggerFactory.getLogger(DispatchBridgeStability.class);
    /**
     * 无效资源
     */
    private static final ContainerProxy INVALID_CONTAINER_PROXY = new ContainerProxy<>(null, null);
    /**
     * 拍了一个数
     * 由于Guava缓存失效,依赖于缓存对象被访问,如果一直无访问,那么将不会触发失效Listener操作,这样会导致Worker无法归还
     *
     * @// TODO: 17/12/4 后续可以考虑失效策略
     */
    private static final long EXPIRE_SECONDS = 8;
    /**
     * 缓存key -> 容器特征
     */
    private static final Cache<String, ContainerFeature> CONTAINER_FEATURE_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(EXPIRE_SECONDS, TimeUnit.SECONDS).weakKeys().weakValues().build();
    /**
     * 缓存key -> ContainerProxy
     */
    private static final LoadingCache<String, ContainerProxy> CONTAINER_PROXY_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(EXPIRE_SECONDS, TimeUnit.SECONDS)
            .removalListener(removalListener()).build(cacheLoader());

    /**
     * 获取Worker
     *
     * @param key              缓存key
     * @param containerFeature Worker对应特征
     * @param <P>              生产者参数类型
     * @param <C>              消费者参数类型
     * @return LoadingCache 不会返回null,但可能会是不可用的ContainerProxy
     * @throws ExecutionException
     */
    public static <P, C> ContainerProxy<P, C> getContainerProxy(String key, ContainerFeature containerFeature)
            throws ExecutionException, UnavailableResourcesException {
        CONTAINER_FEATURE_CACHE.put(key, containerFeature);
        ContainerProxy<P, C> containerProxy = CONTAINER_PROXY_CACHE.getIfPresent(key);
        // 没申请过,申请一个
        if (containerProxy == null) {
            LOG.debug("Alloc containerProxy:{}", key);
            containerProxy = CONTAINER_PROXY_CACHE.get(key);
        }
        /* 检查缓存的ContainerProxy可用性,不可用删除,重新申请
           如果 containerProxy 本来有,意味着失效,删除再申请
           如果 containerProxy 是 if(containerProxy == null) 申请的,重试一次申请资源
           没写在 if(containerProxy == null) 的else里面, 两个原因:
              1.重试一次申请资源;
              2.结构清晰,阿里巴巴规范,可怕*/
        if (containerProxy != null && !containerProxy.isAvailable()) {
            LOG.debug("Invalidate containerProxy:{}", containerProxy.unique());
            CONTAINER_PROXY_CACHE.invalidate(key);
            containerProxy = CONTAINER_PROXY_CACHE.get(key);
        }
        /**
         * 由于 {@link Dispatcher#alloc(ContainerFeature)} 无资源,返回一个空无效ContainerProxy
         * 所以经过资源申请后,依然不可用,意味着真的没有可用资源
         */
        if (containerProxy.isAvailable()) {
            LOG.debug("Unavailable containerProxy:{}", containerProxy.unique());
            return containerProxy;
        } else {
            throw new UnavailableResourcesException("did not get the ContainerProxy");
        }
    }

    /**
     * 申请资源
     *
     * @param key 缓存key
     * @return 可能会是不可用的ContainerProxy
     */
    private static ContainerProxy allocContainerProxy(String key) {
        ContainerFeature containerFeature = CONTAINER_FEATURE_CACHE.getIfPresent(key);
        // 这里如果取不到ContainerFeature意味着有问题, 返回null会抛CacheLoader.InvalidCacheLoadException
        if (containerFeature == null) {
            LOG.warn("Unavailable ContainerFeature:{}", key);
            return null;
        }
        ContainerProxy containerProxy = Dispatcher.alloc(containerFeature);
        return containerProxy == null ? INVALID_CONTAINER_PROXY : containerProxy;
    }

    /**
     * 缓存失效操作,执行资源归还
     *
     * @return 缓存失效函数
     */
    private static RemovalListener<String, ContainerProxy> removalListener() {
        return removal -> {
            LOG.debug("free ContainerProxy unique:{}", removal.getValue().unique());
            if (removal.getValue() != INVALID_CONTAINER_PROXY) {
                Dispatcher.free(removal.getValue());
            }
        };
    }

    /**
     * 缓存miss执行方法
     *
     * @return 缓存miss函数
     */
    private static CacheLoader<String, ContainerProxy> cacheLoader() {
        return new CacheLoader<String, ContainerProxy>() {
            public ContainerProxy load(String key) throws Exception {
                return allocContainerProxy(key);
            }
        };
    }

}
