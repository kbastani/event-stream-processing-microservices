package demo.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class AbstractEntity<T extends Serializable> extends Aggregate<T> implements Serializable {

    private T identity;

    @CreatedDate
    private Long createdAt;

    @LastModifiedDate
    private Long lastModified;

    public AbstractEntity() {
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public T getIdentity() {
        return identity;
    }

    public void setIdentity(T id) {
        this.identity = id;
    }

    @Override
    public String toString() {
        return "BaseEntity{" +
                "createdAt=" + createdAt +
                ", lastModified=" + lastModified +
                '}';
    }
}
