package gl8080.filepost.infrastructure.similar;

import gl8080.filepost.domain.DestinationFolder;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class IndexedSimilarImageFinder {
    private final DestinationFolder folder;

    public IndexedSimilarImageFinder(DestinationFolder folder) {
        this.folder = folder;
    }

    public NoIndexedImages findNoIndexedImages() {
        try {
            List<File> images = this.existsIndexDirectory() ? this.collectNoIndexedImages() : this.folder.collectAllImages();
            return new NoIndexedImages(this.indexDirPath(), images);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private boolean existsIndexDirectory() {
        Path indexDir = Paths.get(this.indexDirPath());
        return Files.exists(indexDir);
    }

    private List<File> collectNoIndexedImages() throws IOException {
        Path indexDir = Paths.get(this.indexDirPath());

        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(indexDir))) {
            IndexSearcher indexSearcher = new IndexSearcher(reader);
            return this.folder.collectAllImages(image -> this.notExistsIndex(image, indexSearcher));
        }
    }

    private boolean notExistsIndex(Path image, IndexSearcher indexSearcher) {
        TermQuery query = new TermQuery(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, image.toString()));
        try {
            TopDocs topDocs = indexSearcher.search(query, 1);
            return topDocs.totalHits == 0;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String indexDirPath() {
        return "./indexes/" + this.folder.getName();
    }
}
