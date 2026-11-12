package in.gov.indiapost.philately.service;

import in.gov.indiapost.philately.dto.CancellationRequestDto;
import in.gov.indiapost.philately.entity.CancellationRequest;
import in.gov.indiapost.philately.entity.Order;
import in.gov.indiapost.philately.entity.PhilatelicItem;
import in.gov.indiapost.philately.entity.SpecialCancellation;
import in.gov.indiapost.philately.entity.User;
import in.gov.indiapost.philately.exception.ResourceNotFoundException;
import in.gov.indiapost.philately.repository.CancellationRequestRepository;
import in.gov.indiapost.philately.repository.OrderRepository;
import in.gov.indiapost.philately.repository.PhilatelicItemRepository;
import in.gov.indiapost.philately.repository.SpecialCancellationRepository;
import in.gov.indiapost.philately.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SpecialCancellationService {

    private final SpecialCancellationRepository cancellationRepo;
    private final CancellationRequestRepository requestRepo;
    private final UserRepository userRepo;
    private final PhilatelicItemRepository itemRepo;
    private final OrderRepository orderRepo;

    public SpecialCancellationService(SpecialCancellationRepository cancellationRepo,
                                      CancellationRequestRepository requestRepo,
                                      UserRepository userRepo,
                                      PhilatelicItemRepository itemRepo,
                                      OrderRepository orderRepo) {
        this.cancellationRepo = cancellationRepo;
        this.requestRepo = requestRepo;
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.orderRepo = orderRepo;
    }

    public List<SpecialCancellation> getActiveCancellations() {
        return cancellationRepo.findByActiveTrueOrderByCancellationDateDesc();
    }

    public SpecialCancellation getById(Long id) {
        return cancellationRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Special Cancellation not found with id: " + id));
    }

    @Transactional
    public CancellationRequest submitRequest(String username, CancellationRequestDto dto) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        SpecialCancellation cancellation = getById(dto.getSpecialCancellationId());

        CancellationRequest req = new CancellationRequest();
        req.setUser(user);
        req.setSpecialCancellation(cancellation);
        req.setInstructions(dto.getInstructions());
        req.setStatus("PENDING");
        req.setRequestDate(LocalDateTime.now());

        if (dto.getItemId() != null) {
            PhilatelicItem item = itemRepo.findById(dto.getItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Philatelic item not found: " + dto.getItemId()));
            req.setItem(item);
        }

        if (dto.getOrderId() != null) {
            Order order = orderRepo.findById(dto.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + dto.getOrderId()));
            req.setOrder(order);
        }

        return requestRepo.save(req);
    }

    public List<CancellationRequest> getUserRequests(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return requestRepo.findByUserOrderByRequestDateDesc(user);
    }

    public List<CancellationRequest> getAllRequests() {
        return requestRepo.findAllByOrderByRequestDateDesc();
    }

    @Transactional
    public CancellationRequest updateRequestStatus(Long id, String status) {
        CancellationRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cancellation request not found with id: " + id));
        req.setStatus(status.toUpperCase());
        return requestRepo.save(req);
    }
}

