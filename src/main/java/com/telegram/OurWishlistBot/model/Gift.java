package com.telegram.OurWishlistBot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "gifts")
public class Gift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private String url;
    private Long userId;
    private Long reservationBy;
    private GiftStatus giftStatus = GiftStatus.AVAILABLE;

    public Gift (String description, Long userId) {
        this.description = description;
        this.userId = userId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getReservationBy() {
        return reservationBy;
    }

    public void setReservationBy(Long reservationByUser) {
        this.reservationBy = reservationByUser;
    }

    public GiftStatus getGiftStatus() {
        return giftStatus;
    }

    public void setGiftStatus(GiftStatus giftStatus) {
        this.giftStatus = giftStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}