package gl8080.filepost.domain;

import java.io.File;
import java.util.List;

public interface SimilarImageFinder {

    List<File> findSimilarImages(File targetImageFile);
}
