package in.gov.indiapost.philately.controller;

import in.gov.indiapost.philately.dto.ApiResponse;
import in.gov.indiapost.philately.entity.PhilatelicItem;
import in.gov.indiapost.philately.entity.PostalCircle;
import in.gov.indiapost.philately.repository.PostalCircleRepository;
import in.gov.indiapost.philately.service.PhilatelicItemService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PhilatelicItemController {

    private final PhilatelicItemService itemService;
    private final PostalCircleRepository postalCircleRepository;

    public PhilatelicItemController(PhilatelicItemService itemService,
                                   PostalCircleRepository postalCircleRepository) {
        this.itemService = itemService;
        this.postalCircleRepository = postalCircleRepository;
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<Page<PhilatelicItem>>> getItems(
            @RequestParam(required = false) Long circleId,
            @RequestParam(required = false) String materialType,
            @RequestParam(required = false) Integer releaseYear,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "most-recent") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Page<PhilatelicItem> items = itemService.getItems(circleId, materialType, releaseYear, theme, query, sortBy, page, size);
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<ApiResponse<PhilatelicItem>> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.getItemById(id)));
    }

    @GetMapping("/items/featured")
    public ResponseEntity<ApiResponse<List<PhilatelicItem>>> getFeaturedItems() {
        return ResponseEntity.ok(ApiResponse.ok(itemService.getFeaturedItems()));
    }

    @GetMapping("/items/popular")
    public ResponseEntity<ApiResponse<List<PhilatelicItem>>> getPopularItems() {
        return ResponseEntity.ok(ApiResponse.ok(itemService.getPopularItems()));
    }

    @GetMapping("/items/{id}/related")
    public ResponseEntity<ApiResponse<List<PhilatelicItem>>> getRelatedItems(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.getRelatedItems(id)));
    }

    @GetMapping("/circles")
    public ResponseEntity<ApiResponse<List<PostalCircle>>> getPostalCircles() {
        return ResponseEntity.ok(ApiResponse.ok(postalCircleRepository.findAll()));
    }
}

