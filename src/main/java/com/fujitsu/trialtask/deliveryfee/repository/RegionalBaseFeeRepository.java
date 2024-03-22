package com.fujitsu.trialtask.deliveryfee.repository;

import com.fujitsu.trialtask.deliveryfee.entity.RegionalBaseFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionalBaseFeeRepository extends JpaRepository<RegionalBaseFee, Long> {
    Optional<RegionalBaseFee> findByCityIdAndVehicleId(Long cityId, Long vehicleId);
}
