package com.hotifi.speedtest.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hotifi.user.entitiies.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
public class SpeedTest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String networkProvider;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt = new Timestamp(System.currentTimeMillis());

    @Column(columnDefinition = "DECIMAL(10,3)", nullable = false)
    private double uploadSpeed;

    @Column(columnDefinition = "DECIMAL(10,3)", nullable = false)
    private double downloadSpeed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(length = 12, nullable = false)
    private String pinCode;

    //@JsonIgnore
    //private List<Long> sessionIds;

}
