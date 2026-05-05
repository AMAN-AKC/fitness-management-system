package com.fitness.repository;

import com.fitness.entity.AddOn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AddOnRepository extends JpaRepository<AddOn, Long> {
	List<AddOn> findByIsActiveTrue();
}
