package com.dactech.requestoff.service;

import com.dactech.requestoff.model.request.EmployeeOffStatusDetailsRequest;
import com.dactech.requestoff.model.request.EmployeeOffStatusRegistRequest;
import com.dactech.requestoff.model.request.EmployeeOffStatusSearchRequest;
import com.dactech.requestoff.model.response.EmployeeOffStatusDetailsResponse;
import com.dactech.requestoff.model.response.EmployeeOffStatusRegistResponse;
import com.dactech.requestoff.model.response.EmployeeOffStatusSearchResponse;

public interface EmployeeOffStatusService {
	EmployeeOffStatusRegistResponse employeeOffStatusRegist(EmployeeOffStatusRegistRequest employeeOffStatusRegistRequest) throws Exception;

	public EmployeeOffStatusSearchResponse search(EmployeeOffStatusSearchRequest employeeOffStatusSearchRequest);

	public EmployeeOffStatusDetailsResponse details(EmployeeOffStatusDetailsRequest employeeOffStatusDetailsRequest);
}