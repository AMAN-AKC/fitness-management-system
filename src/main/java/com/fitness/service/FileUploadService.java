package com.fitness.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fitness.exception.BusinessRuleException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {

	@Value("${file.upload.directory:uploads}")
	private String uploadDirectory;

	@Value("${file.upload.max-size:5242880}")
	private long maxFileSize;

	@Value("${file.allowed-types:jpg,png,pdf}")
	private String allowedTypes;

	private static final Set<String> DEFAULT_ALLOWED_TYPES = new HashSet<>(
			Arrays.asList("jpg", "jpeg", "png", "pdf"));

	/**
	 * Upload a file and return the file path
	 * 
	 * @param file     MultipartFile to upload
	 * @param memberId Member ID for audit purposes
	 * @return File path relative to upload directory
	 */
	public String uploadPhoto(MultipartFile file, Long memberId) {
		validateFile(file);

		try {
			// Create upload directory if it doesn't exist
			Path uploadPath = Paths.get(uploadDirectory);
			Files.createDirectories(uploadPath);

			// Generate unique filename
			String originalFileName = file.getOriginalFilename();
			String fileExtension = getFileExtension(originalFileName);
			String uniqueFileName = "member_" + memberId + "_" + UUID.randomUUID() + "." + fileExtension;

			// Save file
			Path filePath = uploadPath.resolve(uniqueFileName);
			Files.write(filePath, file.getBytes());

			// Return relative path
			return "uploads/" + uniqueFileName;

		} catch (IOException e) {
			throw new BusinessRuleException("Failed to upload file: " + e.getMessage());
		}
	}

	/**
	 * Validate file type, size, and name
	 * 
	 * @param file MultipartFile to validate
	 */
	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessRuleException("File cannot be empty");
		}

		// Validate file size
		if (file.getSize() > maxFileSize) {
			throw new BusinessRuleException(
					"File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + "MB");
		}

		// Validate file type
		String fileExtension = getFileExtension(file.getOriginalFilename());
		Set<String> allowedExtensions = parseAllowedTypes();

		if (!allowedExtensions.contains(fileExtension.toLowerCase())) {
			throw new BusinessRuleException(
					"File type '" + fileExtension + "' is not allowed. Allowed types: " + allowedExtensions);
		}

		// Validate filename is not null/empty
		if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
			throw new BusinessRuleException("File must have a valid name");
		}
	}

	/**
	 * Extract file extension from filename
	 * 
	 * @param filename Original filename
	 * @return File extension (lowercase)
	 */
	private String getFileExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			return "";
		}
		return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
	}

	/**
	 * Parse allowed file types from configuration
	 * 
	 * @return Set of allowed file extensions
	 */
	private Set<String> parseAllowedTypes() {
		Set<String> types = new HashSet<>(DEFAULT_ALLOWED_TYPES);

		if (allowedTypes != null && !allowedTypes.isEmpty()) {
			String[] configuredTypes = allowedTypes.split(",");
			types.clear();
			for (String type : configuredTypes) {
				types.add(type.trim().toLowerCase());
			}
		}

		return types;
	}

	/**
	 * Delete a file (if needed for updating photo)
	 * 
	 * @param filePath File path to delete
	 */
	public void deletePhoto(String filePath) {
		try {
			Path file = Paths.get(filePath);
			Files.deleteIfExists(file);
		} catch (IOException e) {
			// Log but don't fail - file may have already been deleted
			System.err.println("Warning: Could not delete file: " + filePath);
		}
	}

	/**
	 * Get policy description for allowed file types
	 * 
	 * @return Human-readable policy string
	 */
	public String getUploadPolicy() {
		return String.format(
				"Maximum file size: %dMB. Allowed types: %s",
				maxFileSize / 1024 / 1024,
				parseAllowedTypes());
	}
}
