package com.cts.cloudpoc;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface PaymentRepository extends CrudRepository<Payment, Long> {
	List<Payment>  findByAccountNumberAndIfscCodeAndDateOfTransactionBetween(Long accountNumber, String ifscCode, Date from, Date to);
	List<Payment> findByCustomerId(Long customerId);
}