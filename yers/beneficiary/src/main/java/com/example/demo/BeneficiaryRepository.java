package com.example.demo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface BeneficiaryRepository extends CrudRepository<Beneficiary, Long>{
	List<Beneficiary> findByCustomerId(Long customerId);
}
