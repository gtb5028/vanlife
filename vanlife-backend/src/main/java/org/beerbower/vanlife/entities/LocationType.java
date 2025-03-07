package org.beerbower.vanlife.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity
@Table(name = "location_type")
public class LocationType {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_type_id_seq")
    @SequenceGenerator(name = "location_type_seq", sequenceName = "public.location_type_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @ElementCollection
    @CollectionTable(name = "location_type_overpass_tags",
            joinColumns = @JoinColumn(name = "location_type_id"))
    @MapKeyColumn(name = "tag_key")
    @Column(name = "tag_value")
    private Map<String, String> overpassTags = new HashMap<>();
}
