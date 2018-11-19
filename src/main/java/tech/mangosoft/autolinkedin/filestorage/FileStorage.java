package tech.mangosoft.autolinkedin.filestorage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileStorage {
     boolean store(MultipartFile file);
     Resource loadFile();
     void deleteAll();
     void init();
     Stream<Path> loadFiles();
}
