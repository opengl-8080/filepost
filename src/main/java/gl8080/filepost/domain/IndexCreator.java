package gl8080.filepost.domain;

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
import java.util.function.Consumer;

public class IndexCreator {
    
    private final Consumer<Double> listener;

    public IndexCreator(Consumer<Double> listener) {
        this.listener = listener;
    }

    public void createIndex(NoIndexedImages noIndexedImages) {
        GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(CEDD.class);

        try (IndexWriter writer = LuceneUtils.createIndexWriter(noIndexedImages.indexDirPath, false, LuceneUtils.AnalyzerType.WhitespaceAnalyzer)) {
            int i=0;
            int total = noIndexedImages.count();
            for (File image : noIndexedImages.images) {
                BufferedImage bufferedImage = ImageIO.read(image);
                Document document = globalDocumentBuilder.createDocument(bufferedImage, image.toString());
                writer.addDocument(document);
                i++;
                this.listener.accept((double)i/(double)total);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
