package ec.com.nttdata.accounts_movements_service.model;

import ec.com.nttdata.accounts_movements_service.enums.MovementTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movements")
@EqualsAndHashCode(of = "id")
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementTypeEnum movementType;

    @Column(nullable = false)
    private BigDecimal amount; //valor

    @Column(nullable = false)
    private BigDecimal balance; //saldo
    @ToString.Exclude()
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist()
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate()
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}