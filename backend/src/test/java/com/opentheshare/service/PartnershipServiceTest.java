package com.opentheshare.service;

import com.opentheshare.dto.PartnershipInquiryDto;
import com.opentheshare.entity.PartnershipInquiry;
import com.opentheshare.repository.PartnershipInquiryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PartnershipServiceTest {

    @InjectMocks
    private PartnershipService partnershipService;

    @Mock
    private PartnershipInquiryRepository inquiryRepository;

    @Test
    @DisplayName("Create Inquiry saves and returns inquiry")
    void createInquiry_Success() {
        // Given
        PartnershipInquiryDto dto = new PartnershipInquiryDto();
        dto.setCompany("Test Company");
        dto.setManager("Manager Name");
        dto.setEmail("manager@test.com");
        dto.setPhone("010-1234-5678");

        given(inquiryRepository.save(any(PartnershipInquiry.class))).willAnswer(invocation -> {
            PartnershipInquiry inquiry = invocation.getArgument(0);
            inquiry.setId(1L);
            return inquiry;
        });

        // When
        PartnershipInquiry result = partnershipService.createInquiry(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCompany()).isEqualTo(dto.getCompany());
        assertThat(result.getManager()).isEqualTo(dto.getManager());
        assertThat(result.getEmail()).isEqualTo(dto.getEmail());

        verify(inquiryRepository).save(any(PartnershipInquiry.class));
    }
}
