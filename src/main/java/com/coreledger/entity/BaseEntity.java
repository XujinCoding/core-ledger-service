package com.coreledger.entity;

import jakarta.persistence.*;
import lombok.Data;
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
@Data
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
     * Status (1=Enabled, 0=Disabled, Default=1)
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * Created Timestamp (Auto-set on insert)
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Updated Timestamp (Auto-set on update)
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
