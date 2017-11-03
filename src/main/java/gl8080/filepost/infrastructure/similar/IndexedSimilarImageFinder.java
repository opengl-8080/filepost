package gl8080.filepost.infrastructure.similar;

import gl8080.filepost.domain.DestinationFolder;
import gl8080.filepost.domain.SimilarImageFinder;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IndexedSimilarImageFinder implements SimilarImageFinder {
    private final DestinationFolder folder;

    public IndexedSimilarImageFinder(DestinationFolder folder) {
        this.folder = folder;
    }

    @Override
    public List<File> findSimilarImages(DestinationFolder destinationFolder, File targetImageFile) {
        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(this.indexDirPath(destinationFolder))))) {
            GenericFastImageSearcher searcher = new GenericFastImageSearcher(5, CEDD.class);
            BufferedImage image = ImageIO.read(targetImageFile);
            ImageSearchHits hits = searcher.search(image, reader);

            List<File> images = new ArrayList<>();

            for (int i=0; i<hits.length(); i++) {
                double score = hits.score(i);

                if (score < 5.0) {
                    Document document = reader.document(hits.documentID(i));
                    String[] values = document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER);
                    String filePath = values[0];
                    images.add(Paths.get(filePath).toFile());
                }
            }

            return images;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
            return this.folder.collectImages(image -> this.notExistsIndex(image, indexSearcher));
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
        return this.indexDirPath(this.folder);
    }

    private String indexDirPath(DestinationFolder destinationFolder) {
        return "./indexes/" + destinationFolder.getName();
    }
}
