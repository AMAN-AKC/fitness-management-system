package com.fitness.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "branch_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchInventory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long inventoryId;

	@ManyToOne
	@JoinColumn(name = "branch_id", nullable = false)
	private Branch branch;

	@Column(name = "item_name", nullable = false, length = 100)
	private String itemName;

	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	@Column(name = "last_updated", nullable = false)
	private LocalDateTime lastUpdated;

	@PrePersist
	@PreUpdate
	protected void onUpdate() {
		lastUpdated = LocalDateTime.now();
	}
}
