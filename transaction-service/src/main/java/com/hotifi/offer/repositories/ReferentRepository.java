package com.hotifi.offer.repositories;

import com.hotifi.offer.entities.Referent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferentRepository extends JpaRepository<Referent, Long> {
}
