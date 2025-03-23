package com.fujitsu.trialtask.deliveryfee.repository;

import com.fujitsu.trialtask.deliveryfee.entity.WeatherCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WeatherConditionRepository extends JpaRepository<WeatherCondition, Long> {

    /**
     * Returns all WeatherConditions that match given CodeClass (e.g AT for air temperature) and measurement.
     * If the rule defines both min & max, they are inclusive.
     * If the rule defines only min OR max, it is exclusive.
     */
    @Query(value = """
                SELECT wc FROM WeatherCondition wc
                WHERE wc.codeItem.codeClass = :codeClass
                AND (:measurement BETWEEN wc.minMeasurement AND wc.maxMeasurement
                OR wc.minMeasurement IS NULL AND :measurement < wc.maxMeasurement
                OR wc.maxMeasurement IS NULL AND :measurement > wc.minMeasurement)
            """)
    List<WeatherCondition> findAllByCodeItemCodeClassAndMeasurement(
            @Param("codeClass") String codeClass,
            @Param("measurement") Float measurement
    );

    @Query("""
            SELECT wc FROM WeatherCondition wc
            JOIN FETCH wc.phenomenons
            WHERE wc.codeItem.codeClass = :codeClass
            """)
    List<WeatherCondition> findAllByCodeItemCodeClass(@Param("codeClass") String codeClass);
}
