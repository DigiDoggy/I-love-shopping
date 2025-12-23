package shop.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "addresses")
@Data
public class Address {
    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "street", nullable = false)
    private String street;
    @Column(name = "city", nullable = false)
    private String city;
    @Column(name = "state_province", nullable = false)
    private String stateProvince;
    @Column(name = "zip_code", nullable = false)
    private String zipCode;
    @Column(name = "country", nullable = false)
    private String country;
    @Column(name = "default_address",nullable = false)
    private boolean defaultAddress;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;


}
