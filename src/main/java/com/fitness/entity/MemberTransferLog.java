package com.fitness.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_transfer_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberTransferLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long transferId;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne
	@JoinColumn(name = "from_branch_id", nullable = false)
	private Branch fromBranch;

	@ManyToOne
	@JoinColumn(name = "to_branch_id", nullable = false)
	private Branch toBranch;

	@Column(name = "reason", length = 255)
	private String reason;

	@Column(name = "transferred_by", nullable = false)
	private String transferredBy;

	@Column(name = "transfer_date", nullable = false)
	private LocalDateTime transferDate;
}
