package com.fitness.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long templateId;

	@Column(name = "template_name", nullable = false, unique = true, length = 100)
	private String templateName;

	@Column(name = "subject", nullable = false, length = 200)
	private String subject;

	@Column(name = "body_html", nullable = false, columnDefinition = "TEXT")
	private String bodyHtml;

	@Column(name = "variables", length = 500)
	private String variables; // Comma separated list of variables, e.g. "memberName,planName"

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}
