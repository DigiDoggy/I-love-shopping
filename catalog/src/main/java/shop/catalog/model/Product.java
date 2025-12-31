package shop.catalog.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    private String name;
    @Column(unique = true, nullable = false)
    private String slug;
    private String description;
    private double price;
    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;
    @Column(name = "rating_average", nullable = false)
    private int ratingAverage;
    @Column(name = "rating_count", nullable = false)
    private int ratingCount;
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at")
    private Instant updatedAt;
    @Column(name = "weight_kg")
    private double weightKg;
    @Column(name = "height_cm")
    private double heightCm;
    @Column(name = "width_cm")
    private double widthCm;
    @Column(name = "length_cm")
    private double lengthCm;

}
