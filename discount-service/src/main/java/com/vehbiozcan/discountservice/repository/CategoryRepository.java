package com.vehbiozcan.discountservice.repository;

import com.vehbiozcan.discountservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findByExternalId(String externalId);

}
