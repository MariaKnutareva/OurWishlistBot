package com.telegram.OurWishlistBot;

import com.telegram.OurWishlistBot.gifts.Gift;
import com.telegram.OurWishlistBot.gifts.GiftException;

import java.util.List;

public class User {
    private String name;
    private List<Gift> wishlist;

    private Integer id;

    private Integer chatId;

    public User(String name, Integer id, Integer chatId) {
        this.name = name;
        this.id = id;
        this.chatId = chatId;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public List<Gift> getWishlist() {
        return wishlist;
    }

    public void addGift (Gift gift) {
        wishlist.add(gift);
    }
    public void deleteGiftByDescription (String description) {
        wishlist.removeIf(gift -> gift.getDescription().equals(description));
    }

    public Gift getGiftByDescription (String description) throws GiftException {
        for (Gift gift : wishlist) {
            if (description.equals(gift.getDescription())) return gift;
        }
        throw new GiftException();
    }
}
