package com.niladri.inventory_service.model;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@RequiredArgsConstructor
@MappedSuperclass
public class BaseModel {
	private Instant createdAt;
	private Instant updatedAt;
	private String createdBy;
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
