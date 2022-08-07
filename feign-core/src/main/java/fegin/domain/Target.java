package fegin.domain;

import java.util.Objects;

/**
 * @author liuyang
 * 创建时间: 2022-07-23 21:11
 */
public class Target {

    private final Class<?> targetType;

    private final String url;

    public Target(Class<?> targetType, String url) {
        this.targetType = targetType;
        this.url = url;
    }

    public Class<?> getTargetType() {
        return targetType;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Target target = (Target) o;
        return targetType.equals(target.targetType) && url.equals(target.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetType, url);
    }
}
