package com.lethargicdev.cdc.repository;

import com.lethargicdev.cdc.entity.CustomerCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerCopyRepository extends JpaRepository<CustomerCopy, Long> {
}