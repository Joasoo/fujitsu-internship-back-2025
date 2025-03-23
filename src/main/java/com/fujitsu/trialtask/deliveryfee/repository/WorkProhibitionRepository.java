package com.fujitsu.trialtask.deliveryfee.repository;

import com.fujitsu.trialtask.deliveryfee.entity.WorkProhibition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkProhibitionRepository extends JpaRepository<WorkProhibition, Long> {
    Optional<WorkProhibition> findByVehicleIdAndCodeItemCodeIn(Long vehicleId, List<String> codes);
}
