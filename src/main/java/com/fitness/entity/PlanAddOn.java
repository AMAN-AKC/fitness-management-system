package com.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "plan_addon")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanAddOn {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "plan_id", nullable = false)
	private Plan plan;

	@ManyToOne
	@JoinColumn(name = "addon_id", nullable = false)
	private AddOn addOn;

	@Column(nullable = false)
	@Builder.Default
	private Boolean isIncluded = false; // true if addon is included with plan

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}
}
