package in.gov.indiapost.philately.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "special_cancellations")
public class SpecialCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 50)
    private String cancellationCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "postal_circle_id", nullable = false)
    private PostalCircle postalCircle;

    @Column(nullable = false)
    private LocalDate cancellationDate;

    @Column(length = 150)
    private String postOfficeLocation;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 2000)
    private String description;

    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    public SpecialCancellation() {}

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

    public String getCancellationCode() {
        return cancellationCode;
    }

    public void setCancellationCode(String cancellationCode) {
        this.cancellationCode = cancellationCode;
    }

    public PostalCircle getPostalCircle() {
        return postalCircle;
    }

    public void setPostalCircle(PostalCircle postalCircle) {
        this.postalCircle = postalCircle;
    }

    public LocalDate getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(LocalDate cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public String getPostOfficeLocation() {
        return postOfficeLocation;
    }

    public void setPostOfficeLocation(String postOfficeLocation) {
        this.postOfficeLocation = postOfficeLocation;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

