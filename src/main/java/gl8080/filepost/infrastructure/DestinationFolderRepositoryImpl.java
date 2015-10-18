package gl8080.filepost.infrastructure;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import gl8080.filepost.domain.DestinationFolder;
import gl8080.filepost.domain.DestinationFolderRepository;
import gl8080.filepost.infrastructure.store.StoreXmlRoot;

public class DestinationFolderRepositoryImpl implements DestinationFolderRepository {
    
    private File file = new File("./filepost.xml");
    
    @Override
    public List<DestinationFolder> findAll() {
        if (!this.file.exists()) {
            StoreXmlRoot empty = new StoreXmlRoot();
            JAXB.marshal(empty, this.file);
        }
        
        try {
            StoreXmlRoot hoge = JAXB.unmarshal(Files.newBufferedReader(this.file.toPath(), StandardCharsets.UTF_8), StoreXmlRoot.class);
            
            return hoge.getDestinationFolders().stream().map(tag -> {
                return new DestinationFolder(new File(tag.getPath()), tag.getName());
            }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
