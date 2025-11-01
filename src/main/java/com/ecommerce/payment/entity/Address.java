package com.ecommerce.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "street_number", nullable = false)
    private String number;

    @Column(name = "province", nullable = false)
    private String province;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    public String getFullAddress() {
        return String.format("%s %s, %s, %s, %s",
                number, street, province, country, postalCode);
    }
    //number got changed to Integer
    public String getFormattedAddress() {
        return String.format("%s %s%n%s %s%n%s, %s %s",
                firstName, lastName,
                number, street,
                province, country, postalCode);
    }
}
