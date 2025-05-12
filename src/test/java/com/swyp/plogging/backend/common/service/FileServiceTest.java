package com.swyp.plogging.backend.common.service;

import com.swyp.plogging.backend.common.exception.FileDeleteException;
import com.swyp.plogging.backend.common.exception.FileUploadException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
    "file.upload-dir=${java.io.tmpdir}/ploggo-service-uploads"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled
class FileServiceTest {

    @Autowired
    private FileService fileService;
    @Autowired
    private Environment env;

    private Path uploadDir;

    @BeforeAll
    void beforeAll() throws IOException {
        uploadDir = Paths.get(env.getProperty("file.upload-dir"));
        if (Files.exists(uploadDir)) {
            delete(uploadDir);
        }
        Files.createDirectories(uploadDir);
    }

    @BeforeEach
    void cleanup() throws IOException {
        delete(uploadDir);
        Files.createDirectories(uploadDir);
    }

    @AfterAll
    void afterAll() throws IOException {
        delete(uploadDir);
    }

    private void delete(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
            for (Path child : ds) {
                if (Files.isDirectory(child)) {
                    delete(child);
                } else {
                    Files.delete(child);
                }
            }
        }
        Files.delete(path);
    }

    @Test
    void uploadImageAndGetFileName_성공() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "profileImage.png",
            MediaType.IMAGE_PNG_VALUE,
            "testFileData".getBytes()
        );

        String fileName = fileService.uploadImageAndGetFileName(file);
        Path saved = uploadDir.resolve(fileName);

        assertThat(Files.exists(saved)).isTrue();
        byte[] actual = Files.readAllBytes(saved);
        assertThat(new String(actual)).isEqualTo("testFileData");
    }

    @Test
    void uploadImageAndGetFileName_빈파일() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.png",
            MediaType.IMAGE_PNG_VALUE,
            new byte[0]
        );

        assertThatThrownBy(() -> fileService.uploadImageAndGetFileName(file))
            .isInstanceOf(FileUploadException.class)
            .hasMessage("File is empty.");
    }

    @Test
    void uploadImageAndGetFileName_크기초과파일() {
        byte[] sixMb = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "big.png",
            MediaType.IMAGE_PNG_VALUE,
            sixMb
        );

        assertThatThrownBy(() -> fileService.uploadImageAndGetFileName(file))
            .isInstanceOf(FileUploadException.class)
            .hasMessage("File too large.");
    }

    @Test
    void uploadImageAndGetFileName_불허확장자() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "invalid.zip",
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            "zipType".getBytes()
        );

        assertThatThrownBy(() -> fileService.uploadImageAndGetFileName(file))
            .isInstanceOf(FileUploadException.class)
            .hasMessage("Unsupported file type. Only JPG, JPEG, PNG are allowed.");
    }

    @Test
    void deleteFileWithPath_성공() throws Exception {
        // 파일 생성
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profileImage.png",
                MediaType.IMAGE_PNG_VALUE,
                "testFileData".getBytes()
        );

        String fileName = fileService.uploadImageAndGetFileName(file);


        // 삭제
        String imageUrl = "/images/"+fileName;
        fileService.deleteSavedFileWithUrl(imageUrl);

        Path deleted = uploadDir.resolve(fileName);

        assertThat(Files.exists(deleted)).isFalse();
    }

    @Test
    void deleteFileWithPath_없는파일() throws Exception {
        String fileName = "profileImage.png";

        // 삭제
        String imageUrl = "/images/"+fileName;
        assertThatThrownBy(() -> fileService.deleteSavedFileWithUrl(imageUrl))
                .isInstanceOf(FileDeleteException.class);
    }
}