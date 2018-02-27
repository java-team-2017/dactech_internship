package com.dactech.requestoff.model.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
public class Request {
	public static final long REQUEST_STATUS_SAVED = 1;
	public static final long REQUEST_STATUS_APPROVED = 2;
	public static final long REQUEST_STATUS_DENIED = 3;
	public static final long REQUEST_STATUS_RESPONDED = 4;
	public static final long REQUEST_STATUS_WAITING = 5;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	private Employee employee;
	private String fromTime;
	private String toTime;
	private long totalTime;
	private String reason;
	private long status;
	private String responseMessage;
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "day_off_type_id")
	private DayOffType dayOffType;
	private long recipientId;
	private int validFlag;
	private String insertDate;
	private long insertOperator;
	private String updateDate;
	private long updateOperator;

	@Transient
	private String recipientName;
	@Transient
	private long forwardId;
	@Transient
	private String forwardName;

	public Request() {
		super();
	}

	public Request(long id, Employee employee, String fromTime, String toTime, long totalTime, String reason,
			long status, String responseMessage, DayOffType dayOffType, long recipientId, int validFlag,
			String insertDate, long insertOperator, String updateDate, long updateOperator, String recipientName,
			long forwardId, String forwardName) {
		this.id = id;
		this.employee = employee;
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.totalTime = totalTime;
		this.reason = reason;
		this.status = status;
		this.responseMessage = responseMessage;
		this.dayOffType = dayOffType;
		this.recipientId = recipientId;
		this.validFlag = validFlag;
		this.insertDate = insertDate;
		this.insertOperator = insertOperator;
		this.updateDate = updateDate;
		this.updateOperator = updateOperator;
		this.recipientName = recipientName;
		this.forwardId = forwardId;
		this.forwardName = forwardName;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Employee getEmployee() {
		return employee;
	}

	public void setEmployee(Employee employee) {
		this.employee = employee;
	}

	public String getFromTime() {
		return fromTime;
	}

	public void setFromTime(String fromTime) {
		this.fromTime = fromTime;
	}

	public String getToTime() {
		return toTime;
	}

	public void setToTime(String toTime) {
		this.toTime = toTime;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime = totalTime;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public long getStatus() {
		return status;
	}

	public void setStatus(long status) {
		this.status = status;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public DayOffType getDayOffType() {
		return dayOffType;
	}

	public void setDayOffType(DayOffType dayOffType) {
		this.dayOffType = dayOffType;
	}

	public long getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(long recipientId) {
		this.recipientId = recipientId;
	}

	public int getValidFlag() {
		return validFlag;
	}

	public void setValidFlag(int validFlag) {
		this.validFlag = validFlag;
	}

	public String getInsertDate() {
		return insertDate;
	}

	public void setInsertDate(String insertDate) {
		this.insertDate = insertDate;
	}

	public long getInsertOperator() {
		return insertOperator;
	}

	public void setInsertOperator(long insertOperator) {
		this.insertOperator = insertOperator;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public long getUpdateOperator() {
		return updateOperator;
	}

	public void setUpdateOperator(long updateOperator) {
		this.updateOperator = updateOperator;
	}

	public String getRecipientName() {
		return recipientName;
	}

	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}

	public long getForwardId() {
		return forwardId;
	}

	public void setForwardId(long forwardId) {
		this.forwardId = forwardId;
	}

	public String getForwardName() {
		return forwardName;
	}

	public void setForwardName(String forwardName) {
		this.forwardName = forwardName;
	}

	public static long getRequestStatusSaved() {
		return REQUEST_STATUS_SAVED;
	}

	public static long getRequestStatusApproved() {
		return REQUEST_STATUS_APPROVED;
	}

	public static long getRequestStatusDenied() {
		return REQUEST_STATUS_DENIED;
	}

	public static long getRequestStatusResponded() {
		return REQUEST_STATUS_RESPONDED;
	}

	public static long getRequestStatusWaiting() {
		return REQUEST_STATUS_WAITING;
	}

}
