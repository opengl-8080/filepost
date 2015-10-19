package gl8080.filepost.domain;

import java.util.List;

public interface DestinationFolderRepository {
    
    List<DestinationFolder> findAll();

    void registerFolder(DestinationFolder folder);
    
    boolean existsSameFolder(DestinationFolder folder);
}
