package com.coreledger.entity;

import com.coreledger.config.converter.StatusConverter;
import com.coreledger.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base Entity with Common Fields
 * All entities should extend this class
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key (Auto Increment)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /**
     * Memo/Description (Nullable)
     */
    @Column(name = "memo", length = 255)
    private String memo;

    /**
     * Status (1=有效, 0=无效)
     */
    @Column(name = "status", nullable = false)
    @Convert(converter = StatusConverter.class)
    private Status status = Status.ACTIVE;

    /**
     * Created Timestamp (Auto-set on insert)
     */
    @CreatedDate
    @Column(name = "create_instant", nullable = false, updatable = false)
    private LocalDateTime createInstant;

    /**
     * Modified Timestamp (Auto-set on update)
     */
    @LastModifiedDate
    @Column(name = "modify_instant", nullable = false)
    private LocalDateTime modifyInstant;

    /**
     * Version for Optimistic Locking
     */
    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;
}
