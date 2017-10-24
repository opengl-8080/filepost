package gl8080.filepost.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NoIndexedImages {
    final String indexDirPath;
    final List<File> images;

    NoIndexedImages(String indexDirPath, List<File> images) {
        this.indexDirPath = indexDirPath;
        this.images = new ArrayList<>(images);
    }
    
    public boolean isNotEmpty() {
        return !this.images.isEmpty();
    }

    int count() {
        return this.images.size();
    }
}
