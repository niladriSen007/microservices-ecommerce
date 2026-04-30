package com.niladri.inventory_service.model;

import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.validator.internal.constraintvalidators.bv.time.futureorpresent.FutureOrPresentValidatorForInstant;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@MappedSuperclass
public class BaseModel {
	private Instant createdAt;
	private Instant updatedAt;
	private String ceatedBy;
	private String updatedBy;
	
	@PrePersist
	public void createdAt() {
		Instant instant = Instant.now();
		this.createdAt=instant;
		this.updatedAt=instant;
	}
	
	@PreUpdate
	public void updateAt() {
		this.updatedAt=Instant.now();
	}
}
