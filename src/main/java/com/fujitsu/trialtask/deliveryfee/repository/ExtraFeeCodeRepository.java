package com.fujitsu.trialtask.deliveryfee.repository;

import com.fujitsu.trialtask.deliveryfee.util.enums.ExtraFeeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtraFeeCodeRepository extends JpaRepository<ExtraFeeCode, String> {

}
