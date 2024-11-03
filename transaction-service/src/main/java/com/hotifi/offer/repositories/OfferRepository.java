package com.hotifi.offer.repositories;

import com.hotifi.offer.entities.Offer;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferRepository extends PagingAndSortingRepository<Offer, Long> {
}
