package in.gov.indiapost.philately.controller;

import in.gov.indiapost.philately.dto.ApiResponse;
import in.gov.indiapost.philately.entity.WishlistItem;
import in.gov.indiapost.philately.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistItem>>> getWishlist(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok(wishlistService.getUserWishlist(authentication.getName())));
    }

    @PostMapping("/{itemId}")
    public ResponseEntity<ApiResponse<WishlistItem>> addToWishlist(
            Authentication authentication,
            @PathVariable Long itemId) {
        WishlistItem item = wishlistService.addToWishlist(authentication.getName(), itemId);
        return ResponseEntity.ok(ApiResponse.ok("Item added to wishlist", item));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            Authentication authentication,
            @PathVariable Long itemId) {
        wishlistService.removeFromWishlist(authentication.getName(), itemId);
        return ResponseEntity.ok(ApiResponse.ok("Item removed from wishlist", null));
    }

    @GetMapping("/check/{itemId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkWishlist(
            Authentication authentication,
            @PathVariable Long itemId) {
        boolean inWishlist = wishlistService.isInWishlist(authentication.getName(), itemId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("inWishlist", inWishlist)));
    }
}

