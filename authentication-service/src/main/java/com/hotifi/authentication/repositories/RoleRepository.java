package com.hotifi.authentication.repositories;

import com.hotifi.authentication.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    @Query(value = "SELECT * FROM role WHERE name = ?1", nativeQuery = true)
    Role findByRoleName(String name);

}
