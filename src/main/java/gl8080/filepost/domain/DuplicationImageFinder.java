package gl8080.filepost.domain;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
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
import java.util.Arrays;
import java.util.List;

class DuplicationImageFinder {
    
    private final File destDir;
    private final String destDirName;
    private final File targetImageFile;

    DuplicationImageFinder(File destDir, String destDirName, File targetImageFile) {
        this.destDir = destDir;
        this.destDirName = destDirName;
        this.targetImageFile = targetImageFile;
    }

    List<File> find() {
        if (this.existsIndexDirectory()) {
            this.updateIndex();
        } else {
            this.createIndexDirectory();
            this.createNewIndex();
        }
        
        return this.findSimilarImages();
    }
    
    private List<File> findSimilarImages() {
        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(this.indexDirPath())))) {
            GenericFastImageSearcher searcher = new GenericFastImageSearcher(5, CEDD.class);
            BufferedImage image = ImageIO.read(this.targetImageFile);
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
    
    private boolean existsIndexDirectory() {
        Path indexDir = Paths.get(this.indexDirPath());
        return Files.exists(indexDir);
    }
    
    private void createIndexDirectory() {
        Path indexDir = Paths.get(this.indexDirPath());

        try {
            Files.createDirectories(indexDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private void createNewIndex() {
        GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(CEDD.class);

        try (IndexWriter writer = LuceneUtils.createIndexWriter(this.indexDirPath(), true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer)) {
            Files.list(this.destDir.toPath())
                    .filter(this::isImageFile)
                    .forEach(image -> {
                        try {
                            BufferedImage bufferedImage = ImageIO.read(image.toFile());
                            Document document = globalDocumentBuilder.createDocument(bufferedImage, image.toString());
                            writer.addDocument(document);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void updateIndex() {
        Path indexDir = Paths.get(this.indexDirPath());
        GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(CEDD.class);

        try (
            DirectoryReader reader = DirectoryReader.open(FSDirectory.open(indexDir));
            IndexWriter writer = LuceneUtils.createIndexWriter(this.indexDirPath(), false, LuceneUtils.AnalyzerType.WhitespaceAnalyzer)
        ) {
            IndexSearcher indexSearcher = new IndexSearcher(reader);
            
            Files.list(this.destDir.toPath())
                    .filter(this::isImageFile)
                    .filter(image -> this.notExistsIndex(image, indexSearcher))
                    .forEach(image -> {
                        try {
                            BufferedImage bufferedImage = ImageIO.read(image.toFile());
                            Document document = globalDocumentBuilder.createDocument(bufferedImage, image.toString());
                            writer.addDocument(document);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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
    
    private static final List<String> IMAGE_FILE_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif");
    
    private boolean isImageFile(Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        }

        String name = path.getFileName().toString().toLowerCase();
        return IMAGE_FILE_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
    
    private String indexDirPath() {
        return "./indexes/" + this.destDirName;
    }
}
