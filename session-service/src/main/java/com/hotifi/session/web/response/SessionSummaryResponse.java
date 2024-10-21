package com.hotifi.session.web.response;

import com.hotifi.session.models.Buyer;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class SessionSummaryResponse {

    public BigDecimal totalEarnings;

    public BigDecimal netEarnings;

    public BigDecimal unitSellingPrice;

    public String networkProvider;

    public double totalDataSold;

    public double totalData;

    public Date sessionCreatedAt;

    public Date sessionModifiedAt;

    public Date sessionFinishedAt;

    public List<Buyer> buyers;

}
