package com.swyp.plogging.backend.announce.service;

import com.swyp.plogging.backend.announce.controller.dto.AnnounceResponse;
import com.swyp.plogging.backend.announce.domain.Announce;
import com.swyp.plogging.backend.announce.repository.AnnounceRepository;
import com.swyp.plogging.backend.common.exception.AnnounceNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnounceService {

    private final AnnounceRepository announceRepository;

    public List<AnnounceResponse> getAll() {
        List<Announce> announces = announceRepository.findByActiveTrue();
        return announces.stream()
            .map(AnnounceResponse::from)
            .collect(Collectors.toList());
    }

    public AnnounceResponse getAnnounce(Long id) {
        Announce announce = findById(id);
        return AnnounceResponse.from(announce);
    }

    private Announce findById(Long id) {
        return announceRepository.findByIdAndActiveTrue(id)
            .orElseThrow(AnnounceNotFoundException::new);
    }

    @Transactional
    public AnnounceResponse createAnnounce(String title, String content) {
        Announce newAnnounce = Announce.newInstance(title, content, true);
        announceRepository.save(newAnnounce);
        return AnnounceResponse.from(newAnnounce);
    }

    @Transactional
    public void deleteAnnounce(Long id) {
        Announce announce = findById(id);
        announce.inactivation();
    }
}















