package com.example.demo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/bene")
@CrossOrigin
public class BeneficiaryController {

	@Autowired
	BeneficiaryRepository beneficiaryRepository;

	@RequestMapping(path = "/add", consumes = "application/json", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> addBeneficiary(
			@RequestBody Beneficiary beneficiary) {
		Map<String, Object> model = new HashMap<String, Object>();
		try {

			beneficiaryRepository.save(beneficiary);
			model.put("status", "SUCCESS");
		} catch (Exception e) {
			model.put("status", "FAIL");
			e.printStackTrace();
		}
		return model;
	}

	@RequestMapping(path = "/customer")
	@ResponseBody
	public Map<String, Object> findByCusomerId(@RequestParam String customerId) {
		List<Beneficiary> benelist = null;
		Map<String, Object> model = new HashMap<String, Object>();
		try {
			benelist = beneficiaryRepository.findByCustomerId(Long
					.parseLong(customerId));
			model.put("benelist", benelist);
			model.put("status", "SUCCESS");
		} catch (Exception e) {
			e.printStackTrace();
			model.put("status", "FAIL");
		}
		return model;
	}

	@RequestMapping(path = "/all")
	@ResponseBody
	public Iterable<Beneficiary> findAll() {
		Iterable<Beneficiary> all = Collections.EMPTY_LIST;
		try {
			all = beneficiaryRepository.findAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return all;
	}

	@RequestMapping(path = "/delete")
	@ResponseBody
	public String delete() {
		try {
			beneficiaryRepository.deleteAll();
		} catch (Exception e) {
			e.printStackTrace();
			return "FAIL";
		}
		return "SUCCESS";
	}

}
