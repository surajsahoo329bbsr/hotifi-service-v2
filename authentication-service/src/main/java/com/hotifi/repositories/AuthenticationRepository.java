package com.hotifi.repositories;

import com.hotifi.entities.Authentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthenticationRepository extends JpaRepository<Authentication, Long> {

    Authentication findByEmail(String email);

    Boolean existsByPhone(String phone);

    Boolean existsByEmail(String email);

}
