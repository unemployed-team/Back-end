package com.unemployedteam.saferoom.contract;

import com.unemployedteam.saferoom.global.exception.CustomException;
import com.unemployedteam.saferoom.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {

  private final ContractService contractService;

  @PostMapping
  public ResponseEntity<ContractResponse> register(
      @RequestBody ContractRequest request, Authentication auth) {
    return ResponseEntity.ok(contractService.register(extractUserId(auth), request));
  }

  @GetMapping("/me")
  public ResponseEntity<List<ContractResponse>> myContracts(Authentication auth) {
    return ResponseEntity.ok(contractService.getMyContracts(extractUserId(auth)));
  }

  @PutMapping("/{contractId}")
  public ResponseEntity<ContractResponse> update(
      @PathVariable Long contractId,
      @RequestBody ContractRequest request,
      Authentication auth) {
    return ResponseEntity.ok(contractService.update(extractUserId(auth), contractId, request));
  }

  @DeleteMapping("/{contractId}")
  public ResponseEntity<Void> delete(
      @PathVariable Long contractId, Authentication auth) {
    contractService.delete(extractUserId(auth), contractId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{contractId}/simulate")
  public ResponseEntity<SimulationResponse> simulate(
      @PathVariable Long contractId, Authentication auth) {
    return ResponseEntity.ok(contractService.simulate(extractUserId(auth), contractId));
  }

  @GetMapping("/expiring")
  public ResponseEntity<List<ContractResponse>> expiring(Authentication auth) {
    return ResponseEntity.ok(contractService.getExpiringContracts(extractUserId(auth)));
  }

  private Long extractUserId(Authentication auth) {
    if (auth == null) {
      throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
    }
    return (Long) auth.getPrincipal();
  }
}