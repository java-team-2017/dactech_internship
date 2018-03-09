package com.dactech.requestoff.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.dactech.requestoff.model.entity.DayOffType;
import com.dactech.requestoff.model.entity.Employee;
import com.dactech.requestoff.model.entity.EmployeeOffStatus;
import com.dactech.requestoff.model.entity.Request;
import com.dactech.requestoff.model.entity.SlackRequest;
import com.dactech.requestoff.model.request.SlackRequestRegistRequest;
import com.dactech.requestoff.model.request.SlackRequestSearchRequest;
import com.dactech.requestoff.model.response.SlackRequestGetResponse;
import com.dactech.requestoff.model.response.SlackRequestGetResponse.SlackMsg;
import com.dactech.requestoff.model.response.SlackRequestRegistResponse;
import com.dactech.requestoff.model.response.SlackRequestSearchResponse;
import com.dactech.requestoff.repository.DayOffTypeRepository;
import com.dactech.requestoff.repository.EmployeeOffStatusRepository;
import com.dactech.requestoff.repository.EmployeeRepository;
import com.dactech.requestoff.repository.RequestRepository;
import com.dactech.requestoff.repository.SlackRequestRepository;
import com.dactech.requestoff.service.SlackRequestService;
import com.dactech.requestoff.util.SlackUtil;
import com.dactech.requestoff.util.StringUtil;

@Service
public class SlackRequestServiceImpl implements SlackRequestService {
	@Autowired
	SlackRequestRepository slackRequestRepository;
	@Autowired
	EmployeeRepository employeeRepository;
	@Autowired
	DayOffTypeRepository dayOffTypeRepository;
	@Autowired
	RequestRepository requestRepository;
	@Autowired
	EmployeeOffStatusRepository employeeOffStatusRepository;

	@Override
	public long slackRequestRegist(SlackRequest slackRequest) {
		slackRequestRepository.save(slackRequest);
		return slackRequest.getId();
	}
	
	@Override
	public List<SlackRequest> getSlackRequest(String token, String channel, Date latest, Date oldest) throws Exception {
		List<SlackRequest> slackRequests = new ArrayList<SlackRequest>();
		RestTemplate restTemplate = new RestTemplate();
		String url = "https://slack.com/api/channels.history";
		String oldestParam = "", lastestParam = "";
		long lastestTime = 0, oldestTime = 0;

		if (StringUtil.isEmpty(token)) {
			throw new Exception("Token is empty");
		} else if (StringUtil.isEmpty(channel)) {
			throw new Exception("Chanel is empty");
		}

		url = url + "?token=" + token + "&channel=" + channel;

		if (latest != null) {
			lastestTime = (long) latest.getTime() / 1000;
			lastestParam = "&latest=" + (lastestTime);
		}
		if (oldest != null) {
			oldestTime = (long) oldest.getTime() / 1000;
			oldestParam = "&oldest=" + (oldestTime);
		}

		ResponseEntity<SlackRequestGetResponse> responseEntity = restTemplate
				.getForEntity(url + lastestParam + oldestParam, SlackRequestGetResponse.class);
		SlackRequestGetResponse response = responseEntity.getBody();

		if (response.getOk().equals("true")) {
			for (SlackMsg slackMsg : response.getMessages()) {
				SlackRequest slackRequest = SlackUtil.processSlackMessage(slackMsg);
				slackRequests.add(slackRequest);
			}

			while (response.getHasMore().equals("true")) {
				lastestTime = (long) Double
						.parseDouble(response.getMessages().get(response.getMessages().size() - 1).getTs());
				lastestTime--;
				lastestParam = "&latest=" + (lastestTime);

				responseEntity = restTemplate.getForEntity(url + lastestParam + oldestParam,
						SlackRequestGetResponse.class);
				response = responseEntity.getBody();

				for (SlackMsg slackMsg : response.getMessages()) {
					SlackRequest slackRequest = SlackUtil.processSlackMessage(slackMsg);
					slackRequests.add(slackRequest);
				}
			}
		} else {
			throw new Exception(response.getError());
		}

		return slackRequests;
	}

	@Override
	public Request convertSlackRequest(SlackRequest slackRequest) throws Exception {
		if (slackRequest.getIsValidMsg() != 1) {
			throw new Exception("The message is not a Request");
		}
		Request request = new Request();
		List<Employee> employees = employeeRepository.findByNameLike(StringUtil.standardizeName(slackRequest.getName()));
		if (employees == null || employees.size() == 0) {
			throw new Exception("Employee not found");
		}
		if (employees.size() > 1) {
			StringBuilder errMsg = new StringBuilder("Found more than 1 employee with name : " + slackRequest.getName());
			throw new Exception(errMsg.toString());
		}
		Employee employee = employees.get(0);
		request.setEmployee(employee);

		request.setFromTime(slackRequest.getDayOffFrom());
		request.setToTime(slackRequest.getDayOffTo());
		request.setTotalTime(slackRequest.getTotalHours());
		request.setReason(slackRequest.getReason());
		request.setStatus(Request.REQUEST_STATUS_APPROVED);
		request.setResponseMessage("");
		DayOffType dayOffType = dayOffTypeRepository.findById(16);
		request.setDayOffType(dayOffType);
		request.setRecipientId(employee.getId());
		request.setValidFlag(1);

		return request;
	}

	@Override
	public SlackRequestSearchResponse search(SlackRequestSearchRequest request) {
		SlackRequestSearchResponse response = new SlackRequestSearchResponse();
		List<SlackRequest> slackRequests = slackRequestRepository.search(request);
		response.setSlackRequests(slackRequests);
		return response;
	}

	@Override
	public SlackRequestRegistResponse regist(SlackRequestRegistRequest request) throws Exception {
		SlackRequest slackRequest;
		if (StringUtil.isEmpty(request.getId())) { // create new slack request
			slackRequest = new SlackRequest();

			slackRequest.setMsgUserId(request.getMsgUserId());
			slackRequest.setName(request.getName());
			slackRequest.setMsgContent(request.getMsgContent());
			slackRequest.setMsgTime(Long.parseLong(request.getMsgTime()));
			slackRequest.setDayOffFrom(request.getDayOffFrom());
			slackRequest.setDayOffTo(request.getDayOffTo());
			slackRequest.setTotalHours(Double.parseDouble(request.getTotalHours()));
			slackRequest.setReason(request.getReason());
			slackRequest.setProcessFlag(Integer.parseInt(request.getProcessFlag()));
			slackRequest.setIsValidMsg(Integer.parseInt(request.getIsValidMsg()));
			slackRequest.setErrMsg(request.getErrMsg());
			slackRequest.setValidFlag(Integer.parseInt(request.getValidFlag()));
		} else { // update slackRequest
			long slackRequestId = Long.parseLong(request.getId());
			slackRequest = slackRequestRepository.findById(slackRequestId);
			if (slackRequest == null) {
				throw new Exception("cannot find the slack request with id : " + slackRequestId);
			}

			if (!slackRequest.getUpdateDate().equals(request.getUpdateDate())) {
				throw new Exception("Someone updated slack request with id " + slackRequest.getId() + " at "
						+ slackRequest.getUpdateDate());
			}

			if (StringUtil.isNotEmpty(request.getMsgUserId())) {
				slackRequest.setMsgUserId(request.getMsgUserId());
			}
			if (StringUtil.isNotEmpty(request.getName())) {
				slackRequest.setName(request.getName());
			}
			if (StringUtil.isNotEmpty(request.getMsgContent())) {
				slackRequest.setMsgContent(request.getMsgContent());
			}
			if (StringUtil.isNotEmpty(request.getMsgTime())) {
				slackRequest.setMsgTime(Long.parseLong(request.getMsgTime()));
			}
			if (StringUtil.isNotEmpty(request.getDayOffFrom())) {
				slackRequest.setDayOffFrom(request.getDayOffFrom());
			}
			if (StringUtil.isNotEmpty(request.getDayOffTo())) {
				slackRequest.setDayOffTo(request.getDayOffTo());
			}
			if (StringUtil.isNotEmpty(request.getTotalHours())) {
				slackRequest.setTotalHours(Double.parseDouble(request.getTotalHours()));
			}
			if (StringUtil.isNotEmpty(request.getReason())) {
				slackRequest.setReason(request.getReason());
			}
			if (StringUtil.isNotEmpty(request.getIsValidMsg())) {
				slackRequest.setIsValidMsg(Integer.parseInt(request.getIsValidMsg()));
			}
			if (StringUtil.isNotEmpty(request.getErrMsg())) {
				slackRequest.setErrMsg(request.getErrMsg());
			}
			if (StringUtil.isNotEmpty(request.getValidFlag())) {
				slackRequest.setValidFlag(Integer.parseInt(request.getValidFlag()));
			}
			if (StringUtil.isNotEmpty(request.getProcessFlag())) {
				int processFlag = Integer.parseInt(request.getProcessFlag());
				if (processFlag == SlackRequest.PROCESSED) {
					if (slackRequest.getIsValidMsg() == SlackRequest.VALID_REQUEST_MSG) {
						try {
							Request requestOff = convertSlackRequest(slackRequest);
							
							int year = 0;
							int currentYear = Calendar.getInstance().get(Calendar.YEAR);
							try {
								String from = request.getDayOffFrom();
								year = Integer.parseInt(from.substring(0, 4)); // yyyy-MM-dd
							} catch (Exception ex) {
								System.err.println("Warning: do not update remain hours");
							}
							
							if (year != currentYear) {
								System.err.println("Warning: do not update remain hours");
							} else {
								double newRemainHours;
								Employee e = requestOff.getEmployee();
								EmployeeOffStatus eos = employeeOffStatusRepository.findById(year, e.getId());
								newRemainHours = eos.getRemainHours() - requestOff.getTotalTime();
								if (newRemainHours < 0) {
									throw new Exception("Time off (" + requestOff.getTotalTime() + ") Greater than remain hours (" + eos.getRemainHours() + ")");
								}
								eos.setRemainHours(newRemainHours);
								employeeOffStatusRepository.save(eos);
							}
							
							requestRepository.save(requestOff);
							slackRequest.setProcessFlag(SlackRequest.PROCESSED);
						} catch (Exception e) {
							slackRequest.setProcessFlag(SlackRequest.UNPROCESSED);
							slackRequest.setIsValidMsg(SlackRequest.INVALID_REQUEST_MSG);
							slackRequest.setErrMsg(e.getMessage());
							slackRequestRepository.save(slackRequest);
							throw new Exception(e.getMessage());
						}
					} else {
						slackRequest.setProcessFlag(SlackRequest.PROCESSED);
					}
				} else {
					slackRequest.setProcessFlag(SlackRequest.UNPROCESSED);
				}
				
			}
		}
		slackRequestRepository.save(slackRequest);
		SlackRequestRegistResponse response = new SlackRequestRegistResponse();
		response.setId(slackRequest.getId());
		return response;
	}

	@Override
	public long importSlackRequest(String token, String channel, Date latest, Date oldest) throws Exception {
		List<SlackRequest> slackRequests = getSlackRequest(token, channel, latest, oldest);
		//test
		int i = 0;
		for (SlackRequest slackRequest : slackRequests) {
			try {
				if (slackRequest.getIsValidMsg() == SlackRequest.VALID_REQUEST_MSG) {
					Request request = convertSlackRequest(slackRequest);
					
					// update remain time
					int year = 0;
					int currentYear = Calendar.getInstance().get(Calendar.YEAR);
					try {
						String from = request.getFromTime();
						year = Integer.parseInt(from.substring(0, 4)); // yyyy-MM-dd
					} catch (Exception ex) {
						System.err.println("Warning: do not update remain hours");
					}
					
					if (year != currentYear) {
						System.err.println("Warning: do not update remain hours");
					} else {
						double newRemainHours;
						Employee e = request.getEmployee();
						EmployeeOffStatus eos = employeeOffStatusRepository.findById(year, e.getId());
						newRemainHours = eos.getRemainHours() - request.getTotalTime();
						if (newRemainHours < 0) {
							throw new Exception("Time off (" + request.getTotalTime() + ") Greater than remain hours (" + eos.getRemainHours() + ")");
						}
						eos.setRemainHours(newRemainHours);
						employeeOffStatusRepository.save(eos);
					}
					
					requestRepository.save(request);
					slackRequest.setProcessFlag(SlackRequest.PROCESSED);
				}
			} catch (Exception e) {
				slackRequest.setErrMsg(e.getMessage());
				slackRequest.setIsValidMsg(SlackRequest.INVALID_REQUEST_MSG);
			}
			System.out.println(++i);
			slackRequestRepository.save(slackRequest);
		}
		return 0;
	}

}