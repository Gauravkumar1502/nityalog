package dev.gaurav.nityalog.services;

import dev.gaurav.nityalog.repositories.UserSecurityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserSecurityService {
    private final UserSecurityRepository userSecurityRepository;
}
