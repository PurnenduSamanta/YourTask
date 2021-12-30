package Model;

public class DataForDeserializing {

    private String title;
    private String note;
    private String id;
    private String imageUrl;


    public DataForDeserializing()
    {

    }

    public DataForDeserializing(String title, String note, String id, String imageUrl) {
        this.title = title;
        this.note = note;
        this.id = id;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
