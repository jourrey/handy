package com.commissar.handy.feeding.bridge.dispatch;

import com.commissar.handy.dispatch.ContainerFeature;
import com.commissar.handy.dispatch.ContainerProxy;
import com.commissar.handy.feeding.bridge.DeliveryConsumerBridge;
import com.commissar.handy.feeding.exception.UnavailableResourcesException;
import com.google.common.cache.CacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Dispatch数据桥接器
 *
 * @param <T> 任务参数类型
 * @param <P> 生产者参数类型
 * @param <C> 消费者参数类型
 * @author babu
 */
public abstract class DispatchDeliveryConsumerBridge<T, P, C> implements DeliveryConsumerBridge<T, P> {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private ContainerFeature containerFeature;
    private DispatchBridgeStabilityVoucher dispatchBridgeStabilityVoucher;

    public DispatchDeliveryConsumerBridge(ContainerFeature containerFeature
            , DispatchBridgeStabilityVoucher dispatchBridgeStabilityVoucher) {
        this.containerFeature = containerFeature;
        this.dispatchBridgeStabilityVoucher = dispatchBridgeStabilityVoucher;
    }

    @Override
    public void produce(List<P> segmentList) {
        ContainerProxy<P, C> containerProxy;
        P segment;
        for (int i = 0; i < segmentList.size(); i++) {
            segment = segmentList.get(i);
            try {
                containerProxy = DispatchBridgeStability.getContainerProxy(
                        dispatchBridgeStabilityVoucher.voucher() + i, containerFeature);
                if (!containerProxy.process(segment)) {
                    LOG.error("ContainerProxy {} process segment {} failure", containerProxy.unique(), segment);
                }
            } catch (UnavailableResourcesException e) {
                LOG.error("Unavailable resources process segment {} ", segment, e);
            } catch (ExecutionException e) {
                LOG.error("Segment {} execution failure", segment, e);
            } catch (CacheLoader.InvalidCacheLoadException e) {
                LOG.error("Segment {} did not get the resource", segment, e);
            }
        }
    }

}
