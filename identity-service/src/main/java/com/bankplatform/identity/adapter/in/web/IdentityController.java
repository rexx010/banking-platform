package com.bankplatform.identity.adapter.in.web;

import com.bankplatform.identity.adapter.in.web.dto.request.IdentityRequests.*;
import com.bankplatform.identity.adapter.in.web.dto.response.IdentityResponses.*;
import com.bankplatform.identity.adapter.in.web.mapper.IdentityWebMapper;
import com.bankplatform.identity.application.usecase.IdentityCommands.*;
import com.bankplatform.identity.domain.model.DocumentType;
import com.bankplatform.identity.domain.port.in.*;
import com.bankplatform.shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class IdentityController {

    private final CreateBvnUseCase          createBvnUseCase;
    private final SubmitKycDocumentsUseCase kycUseCase;
    private final GetCustomerUseCase        getCustomerUseCase;
    private final IdentityWebMapper         mapper;

    //  Customer-facing endpoints

    /**
     * POST /api/v1/identity/bvn
     * Creates a BVN and customer profile for the authenticated user.
     */
    @PostMapping("/api/v1/identity/bvn")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CustomerResponse> createBvn(
            @Valid @RequestBody CreateBvnRequest request,
            @AuthenticationPrincipal String authUserId
    ) {
        var customer = createBvnUseCase.createBvn(
                mapper.toCommand(request, authUserId)
        );
        return ApiResponse.ok(
                mapper.toResponse(customer),
                "BVN created successfully"
        );
    }

    /**
     * GET /api/v1/identity/me
     * Returns the authenticated user's identity profile.
     */
    @GetMapping("/api/v1/identity/me")
    public ApiResponse<CustomerResponse> getMyProfile(
            @AuthenticationPrincipal String authUserId
    ) {
        var customer = getCustomerUseCase.getByAuthUserId(authUserId);
        return ApiResponse.ok(mapper.toResponse(customer));
    }

    /**
     * POST /api/v1/identity/kyc/documents
     * Uploads a KYC document. Uses multipart/form-data
     * because we are sending binary file data.
     *
     * documentType: the enum name e.g. "LIVE_SELFIE"
     * file:         the actual document binary
     */
    @PostMapping(
            value    = "/api/v1/identity/kyc/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<CustomerResponse> uploadDocument(
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String authUserId
    ) throws IOException {

        var command = new UploadDocumentCommand(
                authUserId,
                DocumentType.valueOf(documentType.toUpperCase()),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                file.getInputStream()
        );

        var updated = kycUseCase.uploadDocument(command);
        return ApiResponse.ok(
                mapper.toResponse(updated),
                "Document uploaded successfully"
        );
    }

    //  Internal service-to-service endpoints
    // Not exposed through API Gateway — internal network only

    /**
     * GET /internal/identity/bvn/{bvn}
     * Called by account-service to verify a BVN before
     * opening an account. Returns lightweight verification result.
     */
    @GetMapping("/internal/identity/bvn/{bvn}")
    public ApiResponse<BvnVerificationResponse> verifyBvn(
            @PathVariable String bvn
    ) {
        var customer = getCustomerUseCase.getByBvn(bvn);
        return ApiResponse.ok(
                mapper.toBvnVerificationResponse(customer)
        );
    }
}