package com.swyp.plogging.backend.common.service;

import com.swyp.plogging.backend.common.domain.ImageExtension;
import com.swyp.plogging.backend.common.exception.FileDeleteException;
import com.swyp.plogging.backend.common.exception.FileUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String uploadImageAndGetFileName(MultipartFile file) {
        validateFile(file);

        String original = getOriginal(file);
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

    private static String getOriginal(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (!StringUtils.hasText(original)) {
            throw new FileUploadException("Filename is invalid");
        }

        return original;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileUploadException("File is empty.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File too large.");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null || !ImageExtension.isAllowed(extension)) {
            throw new FileUploadException("Unsupported file type. Only JPG, JPEG, PNG are allowed.");
        }
    }

    @Transactional
    public void deleteSavedFileWithPath(String filePath) {
        String filename = filePath.replace("/images/", "");
        try{
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path target = uploadPath.resolve(filename).normalize();

            if(!target.startsWith(uploadPath)){
                throw new FileUploadException("Invalid file path");
            }

            File file = target.toFile();
            if(file.exists()){
                if(!file.delete()){
                    throw new FileDeleteException("Fail to delete file: "+filePath);
                }
            }else {
                throw new FileDeleteException();
            }
        }catch(Exception e){
            throw new FileDeleteException();
        }
    }
}
