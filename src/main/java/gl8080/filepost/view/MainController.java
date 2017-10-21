package gl8080.filepost.view;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import gl8080.filepost.domain.DestinationFolder;
import gl8080.filepost.domain.DestinationFolderRepository;
import gl8080.filepost.infrastructure.DestinationFolderRepositoryImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

public class MainController implements Initializable {
    
    @FXML
    private Label targetFilesLabel;
    @FXML
    private Button clearButton;
    @FXML
    private TextField filterTextField;
    @FXML
    private ListView<ListItem> destDirectoryListView;
    
    private List<File> targetFiles;
    private List<DestinationFolder> destinationFolders;
    
    private DestinationFolderRepository repository = new DestinationFolderRepositoryImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.destinationFolders = repository.findAll();
        this.refreshDestFolderListView();
        this.clear();
    }
    
    @FXML
    public void selectDestFolderByMouse(MouseEvent e) {
        if (!this.targetFiles.isEmpty() && this.isDoubleClick(e)) {
            this.selectDestFolder();
        }
    }
    
    @FXML
    public void selectDestFolderByKeyboad(KeyEvent e) {
        if (!this.targetFiles.isEmpty() && (" ".equals(e.getCharacter()) || "\r".equals(e.getCharacter()))) {
            this.selectDestFolder();
        }
    }
    
    private void selectDestFolder() {
        ListItem item = destDirectoryListView.getSelectionModel().getSelectedItem();
        
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("ファイル移動");
        confirm.setHeaderText(item.folder.getDestPath() + " に移動します。");
        confirm.setOnCloseRequest(e -> {
            if (confirm.getResult() == ButtonType.OK) {
                try {
                    item.folder.moveInto(this.targetFiles);
                    Alert complete = new Alert(AlertType.INFORMATION);
                    complete.setTitle("移動完了");
                    complete.setHeaderText(this.targetFiles.size() + " 件のファイルを移動しました。");
                    complete.setContentText("移動先：" + item.folder.getDestPath());
                    complete.show();
                    this.clear();
                } catch (UncheckedIOException ex) {
                    Alert error = new Alert(AlertType.ERROR);
                    
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    TextArea textArea = new TextArea(sw.toString());
                    
                    error.getDialogPane().setExpandableContent(textArea);
                    error.setContentText("ファイル保存中にエラーが発生しました。");
                    error.show();
                }
            }
        });
        confirm.show();
    }
    
    @FXML
    public void refreshDestFolderListView() {
        ObservableList<ListItem> list = FXCollections.observableArrayList();

        String filterText = this.filterTextField.getText().toLowerCase();
        String[] filterTexts = filterText.split("[ 　]");

        this.destinationFolders
            .stream()
            .filter(f -> Stream.of(filterTexts).allMatch(text -> f.getName().toLowerCase().contains(text)))
            .forEach(f -> list.add(new ListItem(f)));
        
        this.destDirectoryListView.setItems(list.sorted());
    }
    
    private boolean isDoubleClick(MouseEvent e) {
        return e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2;
    }
    
    @FXML
    public void onDragOverFile(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        
        if (dragboard.hasFiles()) {
            boolean allFile = dragboard.getFiles().stream().allMatch(File::isFile);
            
            if (allFile) {
                event.acceptTransferModes(TransferMode.COPY);
            }
        }
        
        event.consume();
    }
    
    @FXML
    public void onDragDroppedFile(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        
        this.targetFiles = dragboard.getFiles();
        this.targetFilesLabel.setText(this.targetFiles.size() + " 件選択されています");
        this.targetFilesLabel.setDisable(true);
        this.filterTextField.setDisable(false);
        this.filterTextField.requestFocus();
        
        event.setDropCompleted(true);
        event.consume();
    }
    
    @FXML
    public void clear() {
        this.targetFiles = new ArrayList<>();
        this.targetFilesLabel.setText("ここにファイルをドロップ");
        this.targetFilesLabel.setDisable(false);
        this.filterTextField.setDisable(true);
    }
    
    @FXML
    public void onDragOverFolder(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        
        if (dragboard.hasFiles()
                && dragboard.getFiles().size() == 1
                && dragboard.getFiles().get(0).isDirectory()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        
        event.consume();
    }
    
    @FXML
    public void onDragDroppedFolder(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        
        File folder = dragboard.getFiles().get(0);
    
        DestinationFolder dest = new DestinationFolder(folder);
        
        if (this.repository.existsSameFolder(dest)) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("エラー");
            alert.setHeaderText("既にフォルダが登録されています。");
            alert.getDialogPane().setContentText(dest.getDestPath());
            alert.show();
        } else {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("フォルダ追加");
            dialog.setContentText("フォルダ名");
            dialog.setHeaderText(dest.getDestPath());
            dialog.getEditor().setText(dest.getName());
            
            dialog.setOnCloseRequest(e -> {
                TextInputDialog d = (TextInputDialog)e.getTarget();
                String result = d.getResult();
                
                if (result != null) {
                    if (result.isEmpty()) {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("エラー");
                        alert.setHeaderText("フォルダ名に空は指定できません。");
                        alert.show();
                    } else {
                        dest.setName(result);
                        this.repository.registerFolder(dest);
                        this.destinationFolders = repository.findAll();
                        this.refreshDestFolderListView();
                    }
                }
            });
            
            dialog.show();
        }
        
        event.setDropCompleted(true);
        event.consume();
    }
    
    public static class ListItem {
        private final DestinationFolder folder;
        
        public ListItem(DestinationFolder folder) {
            this.folder = folder;
        }

        @Override
        public String toString() {
            return this.folder.getName() + "\n  > " + this.folder.getDestPath();
        }
    }
}
