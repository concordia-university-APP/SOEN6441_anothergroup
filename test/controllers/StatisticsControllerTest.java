package controllers;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import play.mvc.Result;
import services.StatisticsService;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Author : Tanveer Reza
 * Version : 1
 * Tests for statistic controlelr class
 */
public class StatisticsControllerTest {
    private StatisticsController statisticsController;
    private StatisticsService statisticsService;

    @BeforeEach
    public void setUp() {
        statisticsService = Mockito.mock(StatisticsService.class);
        statisticsController = new StatisticsController(statisticsService);
    }

    /**
     * Test whether the controller is functional and returns 200 on a successful call
     */
    @Test
    public void testGetStatistics() {
        // Arrange
        String query = "Java";
        Map<String, Long> mockWordFrequency = new LinkedHashMap<>();
        mockWordFrequency.put("java", 3L);
        mockWordFrequency.put("programming", 2L);
        mockWordFrequency.put("tutorial", 1L);

        // Mock the behavior of statisticsService
        when(statisticsService.getWordFrequency(anyString())).thenReturn(mockWordFrequency);

        Result result = statisticsController.getStatistics(query);

        assertEquals(200, result.status());
    }
}