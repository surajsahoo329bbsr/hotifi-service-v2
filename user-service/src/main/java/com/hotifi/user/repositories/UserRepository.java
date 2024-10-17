package com.hotifi.user.repositories;

import com.hotifi.user.entitiies.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByAuthenticationId(Long authenticationId);

    User findByFacebookId(String facebookId);

    User findByGoogleId(String facebookId);

    User findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByFacebookId(String facebookId);

    Boolean existsByGoogleId(String googleId);

    @Query(value = "SELECT * FROM user WHERE username IN :usernames", nativeQuery = true)
    List<User> findAllUsersByUsernames(@Param("usernames") Set<String> usernames);

}
