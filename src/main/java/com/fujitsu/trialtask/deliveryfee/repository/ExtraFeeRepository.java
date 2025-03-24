package com.fujitsu.trialtask.deliveryfee.repository;

import com.fujitsu.trialtask.deliveryfee.entity.CodeItem;
import com.fujitsu.trialtask.deliveryfee.entity.ExtraFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExtraFeeRepository extends JpaRepository<ExtraFee, Long> {
    List<ExtraFee> findAllByVehicleIdAndCodeItemIn(Long vehicleId, List<CodeItem> codes);
}
