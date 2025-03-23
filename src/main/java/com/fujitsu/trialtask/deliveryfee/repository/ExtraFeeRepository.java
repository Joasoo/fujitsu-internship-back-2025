package com.fujitsu.trialtask.deliveryfee.repository;

import com.fujitsu.trialtask.deliveryfee.entity.CodeItem;
import com.fujitsu.trialtask.deliveryfee.entity.ExtraFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExtraFeeRepository extends JpaRepository<ExtraFee, Long> {
    Optional<ExtraFee> findByVehicleIdAndCodeItemCode(Long vehicleId, String code);

    Optional<ExtraFee> findByVehicleIdAndCodeItemCodeIn(Long vehicleId, List<String> codes);
    List<ExtraFee> findAllByVehicleIdAndCodeItemCodeIn(Long vehicleId, List<String> codes);
}
