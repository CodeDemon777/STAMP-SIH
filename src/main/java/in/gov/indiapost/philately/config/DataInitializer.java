package in.gov.indiapost.philately.config;

import in.gov.indiapost.philately.entity.*;
import in.gov.indiapost.philately.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PostalCircleRepository postalCircleRepository;
    private final UserRepository userRepository;
    private final DepositAccountRepository depositAccountRepository;
    private final DepositTransactionRepository depositTransactionRepository;
    private final PhilatelicItemRepository itemRepository;
    private final SpecialCancellationRepository cancellationRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PostalCircleRepository postalCircleRepository,
                           UserRepository userRepository,
                           DepositAccountRepository depositAccountRepository,
                           DepositTransactionRepository depositTransactionRepository,
                           PhilatelicItemRepository itemRepository,
                           SpecialCancellationRepository cancellationRepository,
                           OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           PasswordEncoder passwordEncoder) {
        this.postalCircleRepository = postalCircleRepository;
        this.userRepository = userRepository;
        this.depositAccountRepository = depositAccountRepository;
        this.depositTransactionRepository = depositTransactionRepository;
        this.itemRepository = itemRepository;
        this.cancellationRepository = cancellationRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (postalCircleRepository.count() > 0) {
            return; // Already initialized
        }

        // 1. Seed Postal Circles
        PostalCircle dl = postalCircleRepository.save(new PostalCircle("DL", "Delhi Circle", "New Delhi GPO"));
        PostalCircle mh = postalCircleRepository.save(new PostalCircle("MH", "Maharashtra Circle", "Mumbai GPO"));
        PostalCircle tn = postalCircleRepository.save(new PostalCircle("TN", "Tamil Nadu Circle", "Chennai GPO"));
        PostalCircle ka = postalCircleRepository.save(new PostalCircle("KA", "Karnataka Circle", "Bengaluru GPO"));
        PostalCircle wb = postalCircleRepository.save(new PostalCircle("WB", "West Bengal Circle", "Kolkata GPO"));
        PostalCircle kl = postalCircleRepository.save(new PostalCircle("KL", "Kerala Circle", "Thiruvananthapuram GPO"));
        PostalCircle gj = postalCircleRepository.save(new PostalCircle("GJ", "Gujarat Circle", "Ahmedabad GPO"));
        PostalCircle rj = postalCircleRepository.save(new PostalCircle("RJ", "Rajasthan Circle", "Jaipur GPO"));

        // 2. Seed Admin & Demo Collector Users
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@indiapost.gov.in");
        adminUser.setPassword(passwordEncoder.encode("AdminPassword123!"));
        adminUser.setFullName("Senior Philatelic Officer");
        adminUser.setRole(Role.ROLE_ADMIN);
        adminUser.setPhoneNumber("+91 11 2309 6000");
        adminUser.setAddressLine1("Dak Bhawan, Sansad Marg");
        adminUser.setCity("New Delhi");
        adminUser.setState("Delhi");
        adminUser.setPincode("110001");
        userRepository.save(adminUser);

        User demoUser = new User();
        demoUser.setUsername("philatelist");
        demoUser.setEmail("collector@philately.in");
        demoUser.setPassword(passwordEncoder.encode("Password123!"));
        demoUser.setFullName("Aarav Sharma");
        demoUser.setRole(Role.ROLE_USER);
        demoUser.setPhoneNumber("+91 98765 43210");
        demoUser.setAddressLine1("Flat 402, Lotus Orchid Heights");
        demoUser.setAddressLine2("Sector 14, Dwarka");
        demoUser.setCity("New Delhi");
        demoUser.setState("Delhi");
        demoUser.setPincode("110078");
        User savedDemoUser = userRepository.save(demoUser);

        // Demo User PDA Account
        DepositAccount pda = new DepositAccount();
        pda.setUser(savedDemoUser);
        pda.setAccountNumber("PDA-DEL-2024-8841");
        pda.setBalance(BigDecimal.valueOf(2450.00));
        pda.setStatus("ACTIVE");
        pda.setStandingInstructions("Commemorative Stamps, First Day Covers (FDC), Miniature Sheets");
        DepositAccount savedPda = depositAccountRepository.save(pda);

        // Transactions
        DepositTransaction t1 = new DepositTransaction();
        t1.setAccount(savedPda);
        t1.setTransactionType("CREDIT_TOPUP");
        t1.setAmount(BigDecimal.valueOf(3000.00));
        t1.setBalanceAfter(BigDecimal.valueOf(3000.00));
        t1.setReferenceNumber("TXN-INIT-9011");
        t1.setPaymentMethod("NET_BANKING");
        t1.setDescription("PDA Account Initial Opening Deposit");
        t1.setTransactionDate(LocalDateTime.now().minusDays(15));
        depositTransactionRepository.save(t1);

        DepositTransaction t2 = new DepositTransaction();
        t2.setAccount(savedPda);
        t2.setTransactionType("DEBIT_PURCHASE");
        t2.setAmount(BigDecimal.valueOf(550.00));
        t2.setBalanceAfter(BigDecimal.valueOf(2450.00));
        t2.setReferenceNumber("TXN-ORD-8812");
        t2.setPaymentMethod("PDA_WALLET");
        t2.setDescription("Auto-debit for Chandrayaan-3 Special Commemorative Pack");
        t2.setTransactionDate(LocalDateTime.now().minusDays(3));
        depositTransactionRepository.save(t2);

        // 3. Seed Philatelic Items
        PhilatelicItem i1 = createItem(
                "Chandrayaan-3 Historic Lunar Landing",
                "STP-IND-2023-088",
                ka,
                MaterialType.STAMP,
                "Science & Space",
                2023,
                LocalDate.of(2023, 8, 23),
                BigDecimal.valueOf(25.00),
                "₹ 25.00",
                120,
                true,
                true,
                "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=600&auto=format&fit=crop&q=80",
                "Commemorative single stamp honoring ISRO's historic soft landing near Moon's South Pole.",
                "Issued to celebrate India's milestone achievement as the first nation to touch down in the lunar south polar region. Highly sought after by astronomy and space exploration collectors globally."
        );

        PhilatelicItem i2 = createItem(
                "Royal Bengal Tiger Conservation Series",
                "STP-WB-2024-012",
                wb,
                MaterialType.FIRST_DAY_COVER,
                "Wildlife",
                2024,
                LocalDate.of(2024, 4, 10),
                BigDecimal.valueOf(45.00),
                "₹ 45.00",
                85,
                true,
                true,
                "https://images.unsplash.com/photo-1561731216-c3a4d99437d5?w=600&auto=format&fit=crop&q=80",
                "Official First Day Cover (FDC) with special Sundarbans pictorial cancellation postmark.",
                "A vivid philatelic tribute to Project Tiger's golden jubilee. Featuring majestic portraits of Panthera tigris tigris in the mangrove wilderness of the Sundarbans."
        );

        PhilatelicItem i3 = createItem(
                "Konark Sun Temple Architectural Splendor",
                "STP-DL-2024-005",
                dl,
                MaterialType.MINIATURE_SHEET,
                "History",
                2024,
                LocalDate.of(2024, 1, 26),
                BigDecimal.valueOf(75.00),
                "₹ 75.00",
                60,
                true,
                true,
                "https://images.unsplash.com/photo-1600100397608-f010e42e4e75?w=600&auto=format&fit=crop&q=80",
                "Exquisite Miniature Sheet showcasing the intricate stone chariot wheel carvings.",
                "Part of the Monumental Heritage series. Depicts the UNESCO World Heritage 13th-century chariot temple built by King Narasimhadeva I of the Eastern Ganga dynasty."
        );

        PhilatelicItem i4 = createItem(
                "Vande Bharat Express Semi-High Speed Rail",
                "STP-MH-2024-031",
                mh,
                MaterialType.SHEETLET,
                "Science & Space",
                2024,
                LocalDate.of(2024, 6, 15),
                BigDecimal.valueOf(120.00),
                "₹ 120.00",
                40,
                false,
                true,
                "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=600&auto=format&fit=crop&q=80",
                "Collector's Sheetlet of 6 stamps highlighting Indian engineering innovation in rail transit.",
                "Commemorating the expansion of the indigenous semi-high speed train network designed and manufactured by Integral Coach Factory (ICF) under Make in India."
        );

        PhilatelicItem i5 = createItem(
                "Asiatic Lion Sanctuary - Gir Forest",
                "STP-GJ-2024-019",
                gj,
                MaterialType.STAMP,
                "Wildlife",
                2024,
                LocalDate.of(2024, 5, 22),
                BigDecimal.valueOf(15.00),
                "₹ 15.00",
                150,
                false,
                true,
                "https://images.unsplash.com/photo-1546182990-dffeafbe841d?w=600&auto=format&fit=crop&q=80",
                "Definitive high-relief stamp honoring the solitary refuge of the Asiatic Lion.",
                "Highlights Gir National Park's vital role as the last remaining bastion of Panthera leo persica in the wilderness of Kathiawar, Gujarat."
        );

        PhilatelicItem i6 = createItem(
                "Mahatma Gandhi & the Dandi Salt March",
                "STP-GJ-2024-002",
                gj,
                MaterialType.POSTCARD,
                "History",
                2024,
                LocalDate.of(2024, 3, 12),
                BigDecimal.valueOf(20.00),
                "₹ 20.00",
                200,
                false,
                false,
                "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?w=600&auto=format&fit=crop&q=80",
                "Picture postcard with archival photograph and embossed commemorative postmark.",
                "Commemorates the historic non-violent 240-mile march from Sabarmati Ashram to Dandi that catalyzed India's independence movement."
        );

        PhilatelicItem i7 = createItem(
                "Aditya-L1 Solar Observatory In Orbit",
                "STP-KA-2024-044",
                ka,
                MaterialType.STAMP,
                "Science & Space",
                2024,
                LocalDate.of(2024, 2, 5),
                BigDecimal.valueOf(30.00),
                "₹ 30.00",
                95,
                true,
                false,
                "https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=600&auto=format&fit=crop&q=80",
                "Features the satellite at Lagrange Point 1 observing solar flares and corona.",
                "Saluting ISRO scientists for successfully placing India's dedicated solar observatory at the Sun-Earth L1 halo orbit 1.5 million kilometers away."
        );

        PhilatelicItem i8 = createItem(
                "Himalayan Monal - National Bird Series",
                "STP-DL-2024-055",
                dl,
                MaterialType.SPECIAL_COVER,
                "Wildlife",
                2024,
                LocalDate.of(2024, 7, 18),
                BigDecimal.valueOf(60.00),
                "₹ 60.00",
                70,
                false,
                false,
                "https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=600&auto=format&fit=crop&q=80",
                "Special envelope cover with iridescent plumage foil stamping.",
                "Depicting the colorful Himalayan Monal pheasant native to the high-altitude forests of the Himalayas, reflecting the rich biodiversity of northern India."
        );

        // 4. Seed Special Cancellations
        SpecialCancellation sc1 = new SpecialCancellation();
        sc1.setTitle("Chandrayaan-3 First Anniversary Commemorative Postmark");
        sc1.setCancellationCode("SC-BLR-2024-0823");
        sc1.setPostalCircle(ka);
        sc1.setCancellationDate(LocalDate.of(2024, 8, 23));
        sc1.setPostOfficeLocation("Bengaluru General Post Office (GPO)");
        sc1.setImageUrl("https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?w=300&auto=format&fit=crop&q=80");
        sc1.setDescription("Unique cancellation featuring the Vikram lander silhouette and National Space Day insignia. Available for cachets and mail processed through Bengaluru GPO.");
        sc1.setActive(true);
        cancellationRepository.save(sc1);

        SpecialCancellation sc2 = new SpecialCancellation();
        sc2.setTitle("Project Tiger Golden Jubilee Special Pictorial Postmark");
        sc2.setCancellationCode("SC-MUM-2024-0729");
        sc2.setPostalCircle(mh);
        sc2.setCancellationDate(LocalDate.of(2024, 7, 29));
        sc2.setPostOfficeLocation("Mumbai General Post Office (GPO)");
        sc2.setImageUrl("https://images.unsplash.com/photo-1561731216-c3a4d99437d5?w=300&auto=format&fit=crop&q=80");
        sc2.setDescription("Artistic circular cancellation displaying the roaring Bengal Tiger seal, applied exclusively on Global Tiger Day by Maharashtra Circle.");
        sc2.setActive(true);
        cancellationRepository.save(sc2);

        SpecialCancellation sc3 = new SpecialCancellation();
        sc3.setTitle("National Postal Week Heritage Special Cancellation");
        sc3.setCancellationCode("SC-DEL-2024-1009");
        sc3.setPostalCircle(dl);
        sc3.setCancellationDate(LocalDate.of(2024, 10, 9));
        sc3.setPostOfficeLocation("New Delhi Head Post Office");
        sc3.setImageUrl("https://images.unsplash.com/photo-1589829545856-d10d557cf95f?w=300&auto=format&fit=crop&q=80");
        sc3.setDescription("Celebrates 170+ years of postal network service across India, stamped on commemorative philatelic envelopes.");
        sc3.setActive(true);
        cancellationRepository.save(sc3);

        // 5. Seed Demo Order
        Order demoOrder = new Order();
        demoOrder.setOrderNumber("INP1029481");
        demoOrder.setUser(savedDemoUser);
        demoOrder.setTotalAmount(BigDecimal.valueOf(550.00));
        demoOrder.setPaymentMethod("PDA_WALLET");
        demoOrder.setPaymentStatus("PAID");
        demoOrder.setOrderStatus(OrderStatus.SHIPPED);
        demoOrder.setTrackingNumber("SP948102914IN");
        demoOrder.setShippingAddress("Flat 402, Lotus Orchid Heights, Sector 14, Dwarka, New Delhi - 110078");
        demoOrder.setExpectedDeliveryDate(LocalDate.now().plusDays(2));
        demoOrder.setCreatedAt(LocalDateTime.now().minusDays(3));
        Order savedOrder = orderRepository.save(demoOrder);

        OrderItem oi1 = new OrderItem(savedOrder, i1, 10, i1.getPrice());
        OrderItem oi2 = new OrderItem(savedOrder, i3, 4, i3.getPrice());
        orderItemRepository.saveAll(Arrays.asList(oi1, oi2));
    }

    private PhilatelicItem createItem(String title, String itemCode, PostalCircle circle,
                                      MaterialType type, String theme, Integer year, LocalDate releaseDate,
                                      BigDecimal price, String denomination, Integer stock,
                                      boolean featured, boolean popular, String imageUrl,
                                      String features, String description) {
        PhilatelicItem item = new PhilatelicItem();
        item.setTitle(title);
        item.setItemCode(itemCode);
        item.setPostalCircle(circle);
        item.setMaterialType(type);
        item.setTheme(theme);
        item.setReleaseYear(year);
        item.setReleaseDate(releaseDate);
        item.setPrice(price);
        item.setDenomination(denomination);
        item.setStockQuantity(stock);
        item.setFeatured(featured);
        item.setPopular(popular);
        item.setImageUrl(imageUrl);
        item.setThumbnailUrls(imageUrl);
        item.setSpecialFeatures(features);
        item.setDescription(description);
        return itemRepository.save(item);
    }
}

