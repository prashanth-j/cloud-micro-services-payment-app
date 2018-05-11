package com.cts.cloudpoc;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Controller
@CrossOrigin
@RequestMapping(path="/payment")
public class PaymentController {

	@Autowired
	PaymentRepository paymentRepository;

	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	RestTemplate restTemplate;

	@PostMapping(path="/add")
	@ResponseBody
	public String add(@RequestBody Payment payment) {
		try{
			paymentRepository.save(payment);
		}
		catch(Exception e) {
			return "FAIL";
		}
		return "SUCCESS";
	}

	@RequestMapping(path="/transfer",method=RequestMethod.POST)
	@ResponseBody
	public  Map<String, Object> transfer(@RequestBody Map<String,Object> requestBody ) {
		
		Map<String, Object> model = new HashMap<>();
		model.put("status", "SUCCESS");
		List<ServiceInstance> instances = discoveryClient.getInstances("account");
		ServiceInstance serviceInstance = instances.get(0);
		String baseUrl = serviceInstance.getUri().toString();

		Long drAccountNumber = Long.parseLong((String) requestBody.get("fromAccount"));
		String drIfscCode = String.valueOf(requestBody.get("drifscCode"));
		Double amount = Double.parseDouble((String) requestBody.get("amount"));
		Long crAccountNumber = Long.parseLong((String) requestBody.get("toAccount"));
		String crIfscCode = String.valueOf(requestBody.get("crifscCode"));
		
		try{
			String acctNumberUrl = baseUrl +"/account/number";
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(acctNumberUrl).
					queryParam("accountNumber", drAccountNumber).
					queryParam("ifscCode", drIfscCode);
			Account drAccount=restTemplate.getForObject(builder.toUriString(), Account.class);
			if(drAccount == null) {
				model.put("status", "FAIL");
				return model;
			}
			
			if(drAccount.getBalance() < amount) {
				model.put("status", "FAIL");
				return model;
			}
			builder = UriComponentsBuilder.fromUriString(acctNumberUrl).
					queryParam("accountNumber", crAccountNumber).
					queryParam("ifscCode", crIfscCode);
			Account crAccount=restTemplate.getForObject(builder.toUriString(), Account.class);
			if(crAccount == null) {
				model.put("status", "FAIL");
				return model;
			}
			drAccount.setBalance(drAccount.getBalance()-amount);
			crAccount.setBalance(crAccount.getBalance()+amount);
			String status = restTemplate.postForObject(baseUrl+"/account/add", drAccount, String.class);
			if("FAIL".equals(status)) {
				model.put("status", "FAIL");
				return model;
			}
			status = restTemplate.postForObject(baseUrl+"/account/add", crAccount, String.class);
			if("FAIL".equals(status)) {
				model.put("status", "FAIL");
				return model;
			}
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			String transactionRef = cal.getWeekYear() + "" + String.format("%15s", cal.getTimeInMillis()+"").replace(' ', '0');
			Date d = cal.getTime();
			Payment payment = new Payment();
			payment.setAccountNumber(drAccountNumber);
			payment.setAmount(amount);
			payment.setDateOfTransaction(d);
			payment.setDebitOrCredit("DR");
			payment.setIfscCode(drIfscCode);
			payment.setStatus("COMPLETED");
			payment.setTransactionRef(transactionRef);
			payment.setCustomerId(drAccount.getCustomerId());
			payment = paymentRepository.save(payment);
			payment = new Payment();
			payment.setAccountNumber(crAccountNumber);
			payment.setAmount(amount);
			payment.setDateOfTransaction(d);
			payment.setDebitOrCredit("CR");
			payment.setIfscCode(crIfscCode);
			payment.setStatus("COMPLETED");
			payment.setTransactionRef(transactionRef);
			payment.setCustomerId(crAccount.getCustomerId());
			payment = paymentRepository.save(payment);
			model.put("transactionRef", transactionRef);
			model.put("status", "SUCCESS");
		}catch (Exception ex)
		{
			model.put("status", "FAIL");
			ex.printStackTrace();
		}
		return model;
	}


	@RequestMapping(path="/transactions",method=RequestMethod.POST)
	@ResponseBody
	public List<Payment> getPaymentsByAcctNo(@RequestParam Long accountNumber, @RequestParam String ifscCode,
			@RequestParam String from, @RequestParam String to){
		SimpleDateFormat sd= new SimpleDateFormat("dd-MMM-yyyy");
		System.out.println("from "+from);
		System.out.println("to "+to);

		Date fromDate =null;
		Date toDate = null;
		try {
			fromDate = sd.parse(from);
			toDate = sd.parse(to);
		}
		catch(Exception e) {
			System.out.println(e);
		}

		System.out.println("from Date"+fromDate);
		System.out.println("to Date"+toDate);

		return paymentRepository.findByAccountNumberAndIfscCodeAndDateOfTransactionBetween(accountNumber, ifscCode, fromDate, toDate);
	}

	@RequestMapping(path="/customer")
	@ResponseBody
	public Map<String,Object> findByCusomerId(@RequestParam String customerId) {
		List<Payment> paymentlist = null;
		 Map<String,Object> model  = new HashMap<String, Object>();
		try {
			paymentlist = paymentRepository.findByCustomerId(Long
					.parseLong(customerId));
			model.put("paymentList", paymentlist);
			model.put("status", "SUCCESS");
		}
		catch(Exception e) {
			e.printStackTrace();
			model.put("status", "FAIL");
		}
		return model;
	}
	
	@RequestMapping(path="/all")
	@ResponseBody
	public Iterable<Payment> findAll(){
		return paymentRepository.findAll();
	}
	
	@RequestMapping(path="/delete")
	@ResponseBody
	public String delete() {
		try {
			paymentRepository.deleteAll();
		}
		catch(Exception e) {
			return "FAIL";
		}
		return "SUCCESS";
	}

}
