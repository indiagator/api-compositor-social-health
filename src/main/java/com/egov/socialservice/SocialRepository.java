package com.egov.socialservice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialRepository extends JpaRepository<Social, String> {
}