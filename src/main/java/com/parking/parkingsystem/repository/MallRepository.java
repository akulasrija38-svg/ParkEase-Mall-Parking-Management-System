package com.parking.parkingsystem.repository;

import com.parking.parkingsystem.model.Mall;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MallRepository extends MongoRepository<Mall, String> {
}