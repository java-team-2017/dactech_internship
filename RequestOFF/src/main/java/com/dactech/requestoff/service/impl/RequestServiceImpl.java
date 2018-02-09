package com.dactech.requestoff.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dactech.requestoff.model.entity.DayOffType;
import com.dactech.requestoff.model.entity.Employee;
import com.dactech.requestoff.model.entity.Request;
import com.dactech.requestoff.model.request.RequestCalculateHoursRequest;
import com.dactech.requestoff.model.request.RequestDetailsRequest;
import com.dactech.requestoff.model.request.RequestRegistRequest;
import com.dactech.requestoff.model.request.RequestSearchRequest;
import com.dactech.requestoff.model.response.RequestCalculateHoursResponse;
import com.dactech.requestoff.model.response.RequestDetailsResponse;
import com.dactech.requestoff.model.response.RequestRegistResponse;
import com.dactech.requestoff.model.response.RequestSearchResponse;
import com.dactech.requestoff.repository.RequestRepository;
import com.dactech.requestoff.service.RequestService;
import com.dactech.requestoff.util.StringUtil;
import com.dactech.requestoff.util.DateUtils;

@Service
public class RequestServiceImpl implements RequestService{
	@Autowired
	RequestRepository requestRepository;
	
	@Override
	public RequestRegistResponse regist(RequestRegistRequest requestRegistRequest) throws Exception{
		Request request;
		
		if(StringUtil.isEmpty(requestRegistRequest.getId())) {	//create new request
			request = new Request();
			
			Employee employee = new Employee();
			employee.setId(Long.parseLong(requestRegistRequest.getEmployeeId()));
			request.setEmployee(employee);
			
			request.setFromTime(requestRegistRequest.getFromTime());
			request.setToTime(requestRegistRequest.getToTime());
			request.setReason(requestRegistRequest.getReason());
			request.setStatus(Integer.parseInt(requestRegistRequest.getStatus()));
			request.setResponseMessage(requestRegistRequest.getResponseMessage());
			
			DayOffType dayOffType = new DayOffType();
			dayOffType.setId(Long.parseLong(requestRegistRequest.getDayOffTypeId()));
			request.setDayOffType(dayOffType);
			
			request.setRecipientId(Long.parseLong(requestRegistRequest.getRecipientId()));
			
			request.setValidFlag(Integer.parseInt(requestRegistRequest.getValidFlag()));
		}
		else {	//update request
			long id = Long.parseLong(requestRegistRequest.getId());
			request = requestRepository.findById(id);
			if(request == null) {
				throw new Exception("Can not find request with id = " + id);
			}
			else if(requestRegistRequest.getUpdateDate().equals(request.getUpdateDate()) == false) {
				throw new Exception("Someone updated request with id " + requestRegistRequest.getId() + " at " + request.getUpdateDate());
			}
			else {
				if(StringUtil.isNotEmpty(requestRegistRequest.getEmployeeId())) {
					Employee employee = new Employee();
					employee.setId(Long.parseLong(requestRegistRequest.getEmployeeId()));
					request.setEmployee(employee);
				}
				if(StringUtil.isNotEmpty(requestRegistRequest.getFromTime())) {
					request.setFromTime(requestRegistRequest.getFromTime());
				}
				if(StringUtil.isNotEmpty(requestRegistRequest.getToTime())) {
					request.setToTime(requestRegistRequest.getToTime());
				}
				if(StringUtil.isNotEmpty(requestRegistRequest.getReason())) {
					request.setReason(requestRegistRequest.getReason());
				}
				if(StringUtil.isNotEmpty(requestRegistRequest.getStatus())) {
					request.setStatus(Integer.parseInt(requestRegistRequest.getStatus()));
				}
				if(StringUtil.isNotEmpty(requestRegistRequest.getResponseMessage())) {
					request.setResponseMessage(requestRegistRequest.getResponseMessage());
				}
				if(StringUtil.isNotEmpty(requestRegistRequest.getDayOffTypeId())) {
					DayOffType dayOffType = new DayOffType();
					dayOffType.setId(Long.parseLong(requestRegistRequest.getDayOffTypeId()));
					request.setDayOffType(dayOffType);
				}
				if(StringUtil.isNotEmpty(requestRegistRequest.getRecipientId())) {
					request.setRecipientId(Long.parseLong(requestRegistRequest.getRecipientId()));
				}
				if(StringUtil.isNotEmpty(requestRegistRequest.getValidFlag())) {
					request.setValidFlag(Integer.parseInt(requestRegistRequest.getValidFlag()));
				}
			}
		}
		
		requestRepository.save(request);
		RequestRegistResponse requestRegistResponse = new RequestRegistResponse();
		requestRegistResponse.setId(request.getId());
		return requestRegistResponse;
	}

	@Override
	public RequestSearchResponse requestSearch(RequestSearchRequest requestSearchRequest) {
		if (StringUtil.isNotEmpty(requestSearchRequest.getFromTime())) {
			requestSearchRequest.setFromTime(requestSearchRequest.getFromTime() + " 00:00:00");
		}
		if (StringUtil.isNotEmpty(requestSearchRequest.getToTime())) {
			requestSearchRequest.setToTime(requestSearchRequest.getToTime() + " 23:59:59");
		}
		List<Request> requests = requestRepository.searchRequest(requestSearchRequest);
		return new RequestSearchResponse(requests);
	}

	@Override
	public RequestDetailsResponse details(RequestDetailsRequest requestDetailsRequest) {
		Request request = requestRepository.findById(Long.parseLong(requestDetailsRequest.getId()));
		RequestDetailsResponse requestDetailsResponse = new RequestDetailsResponse(request);
		return requestDetailsResponse;
	}
	
	@Override
	public RequestCalculateHoursResponse calculateHours(RequestCalculateHoursRequest calculateHoursRequest) throws Exception {
//		System.out.println(calculateHoursRequest.getFromTime());
//		System.out.println(calculateHoursRequest.getToTime());
		Date fromTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(calculateHoursRequest.getFromTime());
		Date toTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(calculateHoursRequest.getToTime());
		int hours = DateUtils.diffHours(fromTime, toTime);
		RequestCalculateHoursResponse calculateHoursResponse = new RequestCalculateHoursResponse(hours + "");
		return calculateHoursResponse;
	}
	
}
