package in.gov.indiapost.philately.service;

import in.gov.indiapost.philately.dto.ItemDto;
import in.gov.indiapost.philately.entity.MaterialType;
import in.gov.indiapost.philately.entity.PhilatelicItem;
import in.gov.indiapost.philately.entity.PostalCircle;
import in.gov.indiapost.philately.exception.ResourceNotFoundException;
import in.gov.indiapost.philately.repository.PhilatelicItemRepository;
import in.gov.indiapost.philately.repository.PostalCircleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PhilatelicItemService {

    private final PhilatelicItemRepository itemRepository;
    private final PostalCircleRepository postalCircleRepository;

    public PhilatelicItemService(PhilatelicItemRepository itemRepository,
                                 PostalCircleRepository postalCircleRepository) {
        this.itemRepository = itemRepository;
        this.postalCircleRepository = postalCircleRepository;
    }

    public Page<PhilatelicItem> getItems(Long circleId,
                                         String materialTypeStr,
                                         Integer releaseYear,
                                         String theme,
                                         String query,
                                         String sortBy,
                                         int page,
                                         int size) {
        MaterialType materialType = null;
        if (materialTypeStr != null && !materialTypeStr.isBlank() && !materialTypeStr.equalsIgnoreCase("all")) {
            try {
                materialType = MaterialType.valueOf(materialTypeStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("price-asc".equalsIgnoreCase(sortBy) || "price".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else if ("price-desc".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "price");
        } else if ("most-popular".equalsIgnoreCase(sortBy) || "popular".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "popular").and(Sort.by(Sort.Direction.DESC, "stockQuantity"));
        } else if ("most-recent".equalsIgnoreCase(sortBy) || "recent".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "releaseDate");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        String cleanedTheme = (theme != null && !theme.isBlank() && !theme.equalsIgnoreCase("all")) ? theme : null;
        String cleanedQuery = (query != null && !query.isBlank()) ? query.trim() : null;

        return itemRepository.findWithFilters(circleId, materialType, releaseYear, cleanedTheme, cleanedQuery, pageable);
    }

    public PhilatelicItem getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Philatelic item not found with ID: " + id));
    }

    public List<PhilatelicItem> getFeaturedItems() {
        return itemRepository.findByFeaturedTrue();
    }

    public List<PhilatelicItem> getPopularItems() {
        return itemRepository.findByPopularTrue();
    }

    public List<PhilatelicItem> getRelatedItems(Long id) {
        PhilatelicItem current = getItemById(id);
        List<PhilatelicItem> related = itemRepository.findTop4ByThemeAndIdNot(current.getTheme(), id);
        if (related.isEmpty()) {
            return itemRepository.findByPopularTrue();
        }
        return related;
    }

    @Transactional
    public PhilatelicItem createItem(ItemDto dto) {
        PostalCircle circle = postalCircleRepository.findById(dto.getPostalCircleId())
                .orElseThrow(() -> new ResourceNotFoundException("Postal circle not found with ID: " + dto.getPostalCircleId()));

        PhilatelicItem item = new PhilatelicItem();
        copyDtoToEntity(dto, item, circle);
        return itemRepository.save(item);
    }

    @Transactional
    public PhilatelicItem updateItem(Long id, ItemDto dto) {
        PhilatelicItem item = getItemById(id);
        PostalCircle circle = postalCircleRepository.findById(dto.getPostalCircleId())
                .orElseThrow(() -> new ResourceNotFoundException("Postal circle not found with ID: " + dto.getPostalCircleId()));

        copyDtoToEntity(dto, item, circle);
        return itemRepository.save(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item not found with ID: " + id);
        }
        itemRepository.deleteById(id);
    }

    private void copyDtoToEntity(ItemDto dto, PhilatelicItem item, PostalCircle circle) {
        item.setTitle(dto.getTitle());
        item.setItemCode(dto.getItemCode());
        item.setPostalCircle(circle);
        item.setMaterialType(dto.getMaterialType());
        item.setTheme(dto.getTheme());
        item.setReleaseYear(dto.getReleaseYear());
        item.setReleaseDate(dto.getReleaseDate());
        item.setPrice(dto.getPrice());
        item.setDenomination(dto.getDenomination());
        item.setStockQuantity(dto.getStockQuantity() != null ? dto.getStockQuantity() : 50);
        item.setFeatured(dto.isFeatured());
        item.setPopular(dto.isPopular());
        item.setImageUrl(dto.getImageUrl());
        item.setSpecialFeatures(dto.getSpecialFeatures());
        item.setDescription(dto.getDescription());
    }
}

