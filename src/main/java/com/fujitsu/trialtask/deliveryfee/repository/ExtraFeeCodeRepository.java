package com.fujitsu.trialtask.deliveryfee.repository;

import com.fujitsu.trialtask.deliveryfee.entity.ExtraFeeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExtraFeeCodeRepository extends JpaRepository<ExtraFeeCode, String> {
    Optional<ExtraFeeCode> findByCode(String code);
}
