package tech.mangosoft.autolinkedin.filestorage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileStorage {
    public void store(MultipartFile file);
    public Resource loadFile();
    public void deleteAll();
    public void init();
    public Stream<Path> loadFiles();
}
