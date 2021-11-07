package Model;

import java.util.Map;

public class Data {
    private String title;
    private String note;
    private Map<String,String> date;
    private String id;

    public Data(String title, String note, Map<String,String> date, String id) {
        this.title = title;
        this.note = note;
        this.date = date;
        this.id = id;
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

    public Map<String,String> getDate() {
        return date;
    }

    public void setDate( Map<String,String> date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}


