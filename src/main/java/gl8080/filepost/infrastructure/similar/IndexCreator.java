package gl8080.filepost.infrastructure.similar;

import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

public class IndexCreator {
    
    private final ProgressListener progressListener;

    public IndexCreator(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public void createIndex(NoIndexedImages noIndexedImages) {
        GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(CEDD.class);

        try (IndexWriter writer = LuceneUtils.createIndexWriter(noIndexedImages.getIndexDirPath(), false, LuceneUtils.AnalyzerType.WhitespaceAnalyzer)) {
            int i=0;
            int total = noIndexedImages.count();
            for (File image : noIndexedImages.getImages()) {
                BufferedImage bufferedImage = ImageIO.read(image);
                Document document = globalDocumentBuilder.createDocument(bufferedImage, image.toString());
                writer.addDocument(document);
                i++;
                this.progressListener.report((double)i/(double)total);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    @FunctionalInterface
    public interface ProgressListener {
        void report(double progress);
    }
}
