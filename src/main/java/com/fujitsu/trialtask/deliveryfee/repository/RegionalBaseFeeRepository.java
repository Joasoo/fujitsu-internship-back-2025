package com.fujitsu.trialtask.deliveryfee.repository;

import com.fujitsu.trialtask.deliveryfee.entity.RegionalBaseFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionalBaseFeeRepository extends JpaRepository<RegionalBaseFee, Long> {

}
