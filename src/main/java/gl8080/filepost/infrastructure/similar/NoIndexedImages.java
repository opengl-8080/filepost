package gl8080.filepost.infrastructure.similar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NoIndexedImages {
    private final String indexDirPath;
    private final List<File> images;

    NoIndexedImages(String indexDirPath, List<File> images) {
        this.indexDirPath = indexDirPath;
        this.images = new ArrayList<>(images);
    }

    String getIndexDirPath() {
        return indexDirPath;
    }
    
    List<File> getImages() {
        return this.images;
    }

    public boolean isNotEmpty() {
        return !this.images.isEmpty();
    }

    int count() {
        return this.images.size();
    }
}
