package com.space.hitCount.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hitCount")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HitCount {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // 방문자수 id

	@Column(length = 500)
	private String date; // 조회한 날짜

	@Column(length = 500)
	private String spaceId; // 조회된 사용자 ID

	public void save(String spaceId, String formatedNow) {
		this.spaceId = spaceId;
		this.date = formatedNow;
	}
}
