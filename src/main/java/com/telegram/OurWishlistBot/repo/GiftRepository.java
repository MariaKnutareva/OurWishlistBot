package com.telegram.OurWishlistBot.repo;

import com.telegram.OurWishlistBot.model.Gift;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GiftRepository extends CrudRepository<Gift, Long> {
    @Query(value = "SELECT g FROM gifts g WHERE userId = :UID")
    List<Gift> getGiftsByUserID(@Param("UID") Long UID);
}
