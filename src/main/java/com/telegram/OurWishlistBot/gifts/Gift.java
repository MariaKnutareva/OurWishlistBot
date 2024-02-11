package com.telegram.OurWishlistBot.gifts;

import com.telegram.OurWishlistBot.gifts.Status;

public class Gift {
    private String description;
    private String url;
    private Status status = Status.AVAILABLE;

    public Gift (String description) {
        this.description = description;
    }
    public Gift (String description, String url) {
        this.description = description;
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
