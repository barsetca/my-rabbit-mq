package ru.cherniak.produser;

import java.io.Serializable;
import java.util.List;

public class MyMessage implements Serializable {
    private static final long serialVersionUID = -1650136059587331366L;

    private List<String> articleContent;

    public MyMessage(List<String> articleContent) {
        this.articleContent = articleContent;
    }

      public List<String> getArticleContent() {
        return articleContent;
    }

    public void setArticleContent(List<String> articleContent) {
        this.articleContent = articleContent;
    }
}
