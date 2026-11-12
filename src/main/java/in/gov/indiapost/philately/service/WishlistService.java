package in.gov.indiapost.philately.service;

import in.gov.indiapost.philately.entity.PhilatelicItem;
import in.gov.indiapost.philately.entity.User;
import in.gov.indiapost.philately.entity.WishlistItem;
import in.gov.indiapost.philately.exception.BadRequestException;
import in.gov.indiapost.philately.exception.ResourceNotFoundException;
import in.gov.indiapost.philately.repository.PhilatelicItemRepository;
import in.gov.indiapost.philately.repository.UserRepository;
import in.gov.indiapost.philately.repository.WishlistItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistItemRepository wishlistRepo;
    private final UserRepository userRepo;
    private final PhilatelicItemRepository itemRepo;

    public WishlistService(WishlistItemRepository wishlistRepo,
                           UserRepository userRepo,
                           PhilatelicItemRepository itemRepo) {
        this.wishlistRepo = wishlistRepo;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
    }

    public List<WishlistItem> getUserWishlist(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return wishlistRepo.findByUserOrderByAddedAtDesc(user);
    }

    @Transactional
    public WishlistItem addToWishlist(String username, Long itemId) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        PhilatelicItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Philatelic item not found: " + itemId));

        if (wishlistRepo.existsByUserAndItem(user, item)) {
            throw new BadRequestException("Item is already in your wishlist");
        }

        WishlistItem wishlistItem = new WishlistItem(user, item);
        return wishlistRepo.save(wishlistItem);
    }

    @Transactional
    public void removeFromWishlist(String username, Long itemId) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        PhilatelicItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Philatelic item not found: " + itemId));

        wishlistRepo.findByUserAndItem(user, item).ifPresent(wishlistRepo::delete);
    }

    public boolean isInWishlist(String username, Long itemId) {
        return userRepo.findByUsername(username)
                .flatMap(user -> itemRepo.findById(itemId).map(item -> wishlistRepo.existsByUserAndItem(user, item)))
                .orElse(false);
    }
}

