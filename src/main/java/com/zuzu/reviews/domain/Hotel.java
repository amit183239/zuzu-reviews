package com.zuzu.reviews.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "hotels")
@Getter @Setter @NoArgsConstructor
public class Hotel {
    @Id
    private Long id;

    @Column(length = 512)
    private String name;

    public String getCountry() {
        return country;
    }

    public String getName() {
        return name;
    }

    @Column(length = 128)
    private String country;

    public void setCountry(String country) {
        this.country = country;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
