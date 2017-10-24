package gl8080.filepost.domain;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class SimilarImageFinder {

    List<File> findSimilarImages(File targetImageFile, String dirName) {
        try (DirectoryReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("./indexes/" + dirName)))) {
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
}
