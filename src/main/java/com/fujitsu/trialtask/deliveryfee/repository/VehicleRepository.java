package com.fujitsu.trialtask.deliveryfee.repository;

import com.fujitsu.trialtask.deliveryfee.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

}
