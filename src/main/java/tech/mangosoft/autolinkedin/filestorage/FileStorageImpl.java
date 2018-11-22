package tech.mangosoft.autolinkedin.filestorage;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import tech.mangosoft.autolinkedin.service.ContactService;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class FileStorageImpl implements FileStorage{

    @Autowired
    private ContactService contactService;

    private final Path rootLocation = Paths.get("data");
    private final Path rootLocationUpload = Paths.get("dataUpload");

    @Value("${storage.uploadFileName}")
    private String uploadFileName;

    @Value("${storage.filename}")
    private String filename;

    @Override
    public Resource loadFile() {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()) {
                return resource;
            }else{
                throw new RuntimeException("FAIL!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error! -> message = " + e.getMessage());
        }
    }

    @Override
    public boolean store(MultipartFile file){
        try {
            File finalFile = new File(rootLocationUpload.toString().concat(uploadFileName));
            FileUtils.writeByteArrayToFile(finalFile, file.getBytes());
            if (!contactService.readFromExcel(finalFile)) {
                return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectory(rootLocation);
            Files.createDirectory(rootLocationUpload);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage!");
        }
    }

    @Override
    public Stream<Path> loadFiles() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(this.rootLocation::relativize);
        }
        catch (IOException e) {
            throw new RuntimeException("\"Failed to read stored file");
        }
    }
}