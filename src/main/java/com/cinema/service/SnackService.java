package com.cinema.service;

import com.cinema.entity.Snack;
import com.cinema.repository.SnackRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SnackService {
    private final SnackRepository snackRepository;

    public SnackService(SnackRepository snackRepository) {
        this.snackRepository = snackRepository;
    }

    public List<Snack> getAvailableSnacks() {
        return snackRepository.findByStatus("on");
    }

    public List<Snack> getAllSnacks() {
        return snackRepository.findAll();
    }

    public Snack getById(Long id) {
        return snackRepository.findById(id).orElse(null);
    }

    public Snack save(Snack snack) {
        return snackRepository.save(snack);
    }

    public void delete(Long id) {
        snackRepository.deleteById(id);
    }
}
