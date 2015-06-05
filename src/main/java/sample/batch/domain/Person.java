package sample.batch.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Table(name = "PERSONS")
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor
public class Person {
    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 50, nullable = false)
    @NotNull
    @Size(min = 1, max = 50)
    private String firstName;

    @Column(length = 50, nullable = false)
    @NotNull
    @Size(min = 1, max = 50)
    private String lastName;

    @Column(length = 320, nullable = false)
    @NotNull
    @Size(min = 6, max = 320)
    private String mail;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @Column(nullable = false)
    @Version
    @NotNull
    private int version;

    @PrePersist
    protected void onCreate() {
        updated = created = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updated = new Date();
    }
}
