package pl.ing.housingmarket.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@EqualsAndHashCode(of = {"type", "price", "description", "area", "rooms", "region"})
@SQLDelete(sql = "update house set is_deleted = true where id=?")
@SQLRestriction("is_deleted = false")
@ToString
public class House {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String type;
    private BigDecimal price;
    private String description;
    private BigDecimal area;
    private int rooms;
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private Region region;
    @JsonIgnore
    @Column(columnDefinition = "boolean default false")
    private boolean isDeleted = false;
    @JsonIgnore
    private LocalDate creationDate;

    public House(String type, BigDecimal price, String description, BigDecimal area, int rooms, Region region, LocalDate creationDate) {
        this.type = type;
        this.price = price;
        this.description = description;
        this.area = area;
        this.rooms = rooms;
        this.region = region;
        this.creationDate = creationDate;
        isDeleted = false;
    }
}
