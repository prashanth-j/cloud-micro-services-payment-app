package com.cts.cloudpoc;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/account")
@CrossOrigin
public class AccountController {

	@Autowired
	AccountRepository accountRepository;
	
	@PostMapping(path="/add",consumes="application/json")
	@ResponseBody
	public Map<String, Object> addAccounts(@RequestBody Account account) {
		Map<String, Object> model = new HashMap<>();
		model.put("status", "SUCCESS");
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			if(account.getAccountNumber() == null ){
				String accountNumber = String.format("%10s", cal.getTimeInMillis()+"").replace(' ', '0');
				account.setAccountNumber(Long.valueOf(accountNumber));
			}
			account.setStatus("ACTIVE");
		accountRepository.save(account);
		}
		catch(Exception e) {
			model.put("status", "FAIL");
			e.printStackTrace();
		}
		return model;
	}
	
	@GetMapping(path="/all")
	@ResponseBody
	public Iterable<Account> getAll() {
		return accountRepository.findAll();
	}
	
	@GetMapping(path="/number")
	@ResponseBody
	public Account findByAccountNumber(@RequestParam Long accountNumber, 
			@RequestParam String ifscCode) {
		System.out.println("inside findByAccountNumber");
		Account account = accountRepository.findByAccountNumberAndIfscCode(accountNumber, ifscCode);
		System.out.println(account);
		return account;
	}
	
	@GetMapping(path="/customer")
	@ResponseBody
	public Account findByCusomerId(@RequestParam String customerId) {
		Account account = null;
		try{
			account = accountRepository.findByCustomerId(Long.valueOf(customerId));
			System.out.println(account);
		}catch(Exception e){
			e.printStackTrace();
		}
		return account;
	}
	
	@GetMapping(path="/delete")
	@ResponseBody
	public String delete() {
		try {
			accountRepository.deleteAll();
		}
		catch(Exception e) {
			return "FAIL";
		}
		return "SUCCESS";
	}
}
