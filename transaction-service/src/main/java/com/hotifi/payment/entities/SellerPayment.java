package com.hotifi.payment.entities;

import com.hotifi.user.entitiies.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
public class SellerPayment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", unique = true, nullable = false)
    private User seller;

    @Column(nullable = false)
    private BigDecimal amountEarned; // Total amount earned by the seller

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amountPaid; // Total amount to be paid to the seller

    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private boolean isWithdrawalClaimNotified;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt = new Date(System.currentTimeMillis()); // Time at which first transfer was made

    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedAt; // Time at which money started to accumulate

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastPaidAt; // Time at which money started to accumulate

    private String transferErrorDescription;

    @OneToMany(mappedBy = "sellerPayment", fetch = FetchType.EAGER)
    private List<SellerReceipt> sellerReceipts;
}
