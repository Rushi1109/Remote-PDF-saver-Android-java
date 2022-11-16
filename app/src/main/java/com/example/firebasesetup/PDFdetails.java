package com.example.firebasesetup;

public class PDFdetails {

    private String name;
    private String url;

    public PDFdetails() {
    }

    public PDFdetails(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
