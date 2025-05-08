package com.swyp.plogging.backend.common.service;

import com.swyp.plogging.backend.common.domain.ImageExtension;
import com.swyp.plogging.backend.common.exception.FileUploadException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String uploadImageFile(MultipartFile file) {
        validateFile(file);

        String original = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(original);
        String filename = UUID.randomUUID() + "." + extension;

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path target = uploadPath.resolve(filename).normalize();

            if (!target.startsWith(uploadPath)) {
                throw new FileUploadException("Invalid file path");
            }

            Files.createDirectories(target.getParent());
            file.transferTo(target.toFile());

            return filename;
        } catch (IOException e) {
            throw new FileUploadException();
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File too large");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null || !ImageExtension.isAllowed(extension)) {
            throw new FileUploadException("Unsupported file type. Only JPG, JPEG, PNG are allowed.");
        }
    }
}
