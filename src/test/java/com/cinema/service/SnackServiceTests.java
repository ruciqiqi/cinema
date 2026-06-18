package com.cinema.service;

import com.cinema.entity.Snack;
import com.cinema.repository.SnackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnackServiceTests {

    @Mock private SnackRepository snackRepository;

    @InjectMocks
    private SnackService snackService;

    private Snack popcorn;
    private Snack coke;
    private Snack combo;

    @BeforeEach
    void setUp() {
        popcorn = new Snack();
        popcorn.setId(1L);
        popcorn.setName("爆米花");
        popcorn.setCategory("零食");
        popcorn.setPrice(20.0);
        popcorn.setStatus("on");

        coke = new Snack();
        coke.setId(2L);
        coke.setName("可乐");
        coke.setCategory("饮料");
        coke.setPrice(10.0);
        coke.setStatus("on");

        combo = new Snack();
        combo.setId(3L);
        combo.setName("双人套餐");
        combo.setCategory("套餐");
        combo.setPrice(45.0);
        combo.setStatus("on");
    }

    // ==================== SN-01 获取所有小食 ====================
    @Test
    void testGetAllSnacks() {
        when(snackRepository.findAll()).thenReturn(Arrays.asList(popcorn, coke, combo));

        List<Snack> snacks = snackService.getAllSnacks();

        assertEquals(3, snacks.size());
        verify(snackRepository, times(1)).findAll();
    }

    // ==================== SN-02 仅上架小食 ====================
    @Test
    void testGetAvailableSnacks() {
        when(snackRepository.findByStatus("on")).thenReturn(Arrays.asList(popcorn, coke, combo));

        List<Snack> result = snackService.getAvailableSnacks();

        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(s -> "on".equals(s.getStatus())));
    }

    // ==================== SN-03 根据ID查询 ====================
    @Test
    void testGetByIdExists() {
        when(snackRepository.findById(1L)).thenReturn(Optional.of(popcorn));

        Snack result = snackService.getById(1L);

        assertNotNull(result);
        assertEquals("爆米花", result.getName());
        assertEquals(20.0, result.getPrice(), 0.01);
    }

    // ==================== SN-04 根据ID查询不存在 ====================
    @Test
    void testGetByIdNotFound() {
        when(snackRepository.findById(999L)).thenReturn(Optional.empty());

        Snack result = snackService.getById(999L);

        assertNull(result);
    }

    // ==================== SN-05 保存小食 ====================
    @Test
    void testSaveSnack() {
        Snack newSnack = new Snack();
        newSnack.setName("薯条");
        newSnack.setPrice(15.0);
        newSnack.setStatus("on");

        when(snackRepository.save(newSnack)).thenAnswer(inv -> {
            Snack saved = inv.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        Snack result = snackService.save(newSnack);

        assertNotNull(result);
        assertEquals(99L, result.getId());
        assertEquals("薯条", result.getName());
        verify(snackRepository, times(1)).save(newSnack);
    }

    // ==================== SN-06 删除小食 ====================
    @Test
    void testDeleteSnack() {
        doNothing().when(snackRepository).deleteById(1L);

        snackService.delete(1L);

        verify(snackRepository, times(1)).deleteById(1L);
    }

    // ==================== SN-07 空库 ====================
    @Test
    void testGetSnacksEmpty() {
        when(snackRepository.findAll()).thenReturn(new ArrayList<>());

        List<Snack> snacks = snackService.getAllSnacks();

        assertTrue(snacks.isEmpty());
    }
}
