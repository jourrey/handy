package com.commissar.handy.dispatch;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * 容器特征
 *
 * @param <T> 特征类类型
 * @author babu
 */
public class ContainerFeature<T> {

    private T feature;

    /**
     * 只允许包内访问
     *
     * @param feature
     */
    ContainerFeature(T feature) {
        this.feature = feature;
    }

    public T getFeature() {
        return feature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContainerFeature<T> that = (ContainerFeature<T>) o;

        return new EqualsBuilder()
                .append(feature, that.feature)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(feature)
                .toHashCode();
    }
}
