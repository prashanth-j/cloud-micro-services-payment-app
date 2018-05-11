package com.cts.cloudpoc;

import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, Long>{
	Account findByAccountNumberAndIfscCode(Long accountNumber, String ifscCode);
	Account findByCustomerId(Long customerId);
}
