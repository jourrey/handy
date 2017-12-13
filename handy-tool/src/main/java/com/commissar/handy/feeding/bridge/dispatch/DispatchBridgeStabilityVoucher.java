package com.commissar.handy.feeding.bridge.dispatch;

/**
 * 稳定性凭证
 *
 * @author babu
 */
public interface DispatchBridgeStabilityVoucher {

    /**
     * 凭证
     * 稳定器将以凭证为粒度,一个凭证是相对稳定的
     * 使用时减少一个凭证存在竞争,因为一个凭证只维护一个资源,竞争不保证所有竞争者都能获得资源
     *
     * @return
     */
    String voucher();

}
