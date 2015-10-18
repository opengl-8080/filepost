package gl8080.filepost.infrastructure.store;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="filepost")
public class StoreXmlRoot {
    
    private List<DestinationFolderTag> destinationFolders = new ArrayList<>();

    @XmlElementWrapper(name="destination-folders")
    @XmlElement(name="destination-folder")
    public List<DestinationFolderTag> getDestinationFolders() {
        return destinationFolders;
    }

    public void setDestinationFolders(List<DestinationFolderTag> destinationFolders) {
        this.destinationFolders = destinationFolders;
    }

    @Override
    public String toString() {
        return "StoreXmlRoot [destinationFolders=" + destinationFolders + "]";
    }
    
}
