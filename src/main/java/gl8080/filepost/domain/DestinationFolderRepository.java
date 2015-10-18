package gl8080.filepost.domain;

import java.util.List;

public interface DestinationFolderRepository {
    
    List<DestinationFolder> findAll();
}
