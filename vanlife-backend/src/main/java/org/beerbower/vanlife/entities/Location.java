package org.beerbower.vanlife.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder({"id", "externalId", "source"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "location")
public class Location {

    public enum Source {
        LOC, // local
        OSM  // open street map
    }

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_id_seq")
    @SequenceGenerator(name = "location_id_seq", sequenceName = "public.location_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long internalId;

    @JsonIgnore
    @Column(name = "external_id", unique = true)
    private Long externalId;

    @JsonIgnore
    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Source source = Source.LOC;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @ManyToOne
    @JoinColumn(name = "location_type_id", nullable = false)
    private LocationType type;

    @Column(length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ElementCollection
    @CollectionTable(name = "location_metadata",
            joinColumns = @JoinColumn(name = "location_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    private Map<String, String> metadata = new HashMap<>();

    @Transient
    public String getId() {
        return String.format("%s-%d", source, source == Source.LOC ? internalId : externalId);
    }
}
