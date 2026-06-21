package com.nurtureshare.backend.service;

import com.nurtureshare.backend.dto.response.CoupleStatusResponse;
import com.nurtureshare.backend.dto.response.UserResponse;
import com.nurtureshare.backend.exception.ResourceNotFoundException;
import com.nurtureshare.backend.model.Couple;
import com.nurtureshare.backend.model.User;
import com.nurtureshare.backend.repository.CoupleRepository;
import com.nurtureshare.backend.repository.PregnancyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CoupleService {

    private final CoupleRepository coupleRepository;
    private final PregnancyRepository pregnancyRepository;

    public CoupleStatusResponse getStatus(User currentUser) {
        Couple couple = getCoupleForUser(currentUser);
        return toCoupleStatusResponse(couple, currentUser);
    }

    @Transactional
    public CoupleStatusResponse pairWithPartner(User currentUser, String pairingCode) {
        Couple targetCouple = coupleRepository.findByPairingCode(pairingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid pairing code: " + pairingCode));

        if (targetCouple.getUser2() != null) {
            throw new IllegalArgumentException("This pairing code has already been used.");
        }
        if (targetCouple.getUser1().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You cannot pair with yourself.");
        }

        // Delete the joiner's own solo couple and its placeholder pregnancy
        coupleRepository.findByUser1AndUser2IsNull(currentUser).ifPresent(ownCouple -> {
            pregnancyRepository.findByCoupleId(ownCouple.getId()).ifPresent(pregnancyRepository::delete);
            coupleRepository.delete(ownCouple);
            log.info("Removed solo couple {} for user {} who is joining couple {}",
                    ownCouple.getId(), currentUser.getEmail(), targetCouple.getId());
        });

        targetCouple.setUser2(currentUser);
        targetCouple.setSyncedAt(LocalDateTime.now());
        Couple saved = coupleRepository.save(targetCouple);

        log.info("User {} joined couple {} (owner: {})",
                currentUser.getEmail(), saved.getId(), saved.getUser1().getEmail());

        return toCoupleStatusResponse(saved, currentUser);
    }

    public Couple getCoupleForUser(User user) {
        return coupleRepository.findByUser1OrUser2(user, user)
                .orElseThrow(() -> new ResourceNotFoundException("No couple found for user: " + user.getEmail()));
    }

    private CoupleStatusResponse toCoupleStatusResponse(Couple couple, User currentUser) {
        User partner = couple.getPartner(currentUser.getId());
        UserResponse partnerResponse = null;
        if (partner != null) {
            partnerResponse = UserResponse.builder()
                    .id(partner.getId())
                    .email(partner.getEmail())
                    .name(partner.getName())
                    .avatarUrl(partner.getAvatarUrl())
                    .createdAt(partner.getCreatedAt())
                    .build();
        }

        return CoupleStatusResponse.builder()
                .coupleId(couple.getId())
                .pairingCode(couple.getPairingCode())
                .connected(couple.isConnected())
                .partner(partnerResponse)
                .syncedAt(couple.getSyncedAt())
                .createdAt(couple.getCreatedAt())
                .build();
    }
}
