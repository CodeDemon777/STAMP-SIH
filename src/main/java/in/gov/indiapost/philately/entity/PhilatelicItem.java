package in.gov.indiapost.philately.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "philatelic_items")
public class PhilatelicItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 50)
    private String itemCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "postal_circle_id", nullable = false)
    private PostalCircle postalCircle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaterialType materialType = MaterialType.STAMP;

    @Column(nullable = false, length = 50)
    private String theme;

    @Column(nullable = false)
    private Integer releaseYear;

    private LocalDate releaseDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 20)
    private String denomination;

    @Column(nullable = false)
    private Integer stockQuantity = 50;

    private boolean featured = false;
    private boolean popular = false;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 1000)
    private String thumbnailUrls;

    @Column(length = 500)
    private String specialFeatures;

    @Column(length = 3000)
    private String description;

    private LocalDateTime createdAt = LocalDateTime.now();

    public PhilatelicItem() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public PostalCircle getPostalCircle() {
        return postalCircle;
    }

    public void setPostalCircle(PostalCircle postalCircle) {
        this.postalCircle = postalCircle;
    }

    public MaterialType getMaterialType() {
        return materialType;
    }

    public void setMaterialType(MaterialType materialType) {
        this.materialType = materialType;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDenomination() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public boolean isPopular() {
        return popular;
    }

    public void setPopular(boolean popular) {
        this.popular = popular;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbnailUrls() {
        return thumbnailUrls;
    }

    public void setThumbnailUrls(String thumbnailUrls) {
        this.thumbnailUrls = thumbnailUrls;
    }

    public String getSpecialFeatures() {
        return specialFeatures;
    }

    public void setSpecialFeatures(String specialFeatures) {
        this.specialFeatures = specialFeatures;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

