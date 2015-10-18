package gl8080.filepost.domain;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class DestinationFolderTest {
    
    private File destDir;
    private DestinationFolder dest;

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();
    
    @Before
    public void setup() {
        destDir = new File(tmpDir.getRoot(), "dest");
        destDir.mkdirs();
        
        dest = new DestinationFolder(destDir);
    }
    
    @Test
    public void ディレクトリ名がデフォルトの名前として設定されていること() {
        // verify
        assertThat(dest.getName(), is(destDir.getName()));
    }
    
    @Test
    public void 名前を任意に変更できること() throws Exception {
        // exercise
        dest.setName("override");
        
        // verify
        assertThat(dest.getName(), is("override"));
    }
    
    @Test
    public void 指定したファイル一覧を_このインスタンスが指すディレクトリに移動できること() throws Exception {
        // setup
        File file1 = createFile("file1.txt");
        File file2 = createFile("file2.txt");
        File file3 = createFile("file3.txt");
        
        List<File> files = Arrays.asList(file1, file2, file3);
        
        // exercise
        dest.moveInto(files);
        
        // verify
        boolean deleted = files.stream().allMatch(f -> !f.exists());
        assertThat(deleted, is(true));
        
        String content1 = content(new File(destDir, "file1.txt"));
        String content2 = content(new File(destDir, "file2.txt"));
        String content3 = content(new File(destDir, "file3.txt"));
        
        assertThat(content1, is("FILE1.TXT"));
        assertThat(content2, is("FILE2.TXT"));
        assertThat(content3, is("FILE3.TXT"));
    }
    
    private File createFile(String fileName) throws Exception {
        File file = new File(tmpDir.getRoot(), fileName);
        Files.write(file.toPath(), fileName.toUpperCase().getBytes(), StandardOpenOption.CREATE);
        return file;
    }
    
    private String content(File file) throws Exception {
        return new String(Files.readAllBytes(file.toPath()));
    }
}
