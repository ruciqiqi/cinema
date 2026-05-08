package com.cinema.repository;

import com.cinema.entity.Snack;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SnackRepository extends JpaRepository<Snack, Long> {
    List<Snack> findByStatus(String status);
    List<Snack> findByCategory(String category);
}
