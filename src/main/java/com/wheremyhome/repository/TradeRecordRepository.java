package com.wheremyhome.repository;

import com.wheremyhome.domain.apartment.TradeRecord;
import com.wheremyhome.domain.apartment.TradeRecordId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRecordRepository extends JpaRepository<TradeRecord, TradeRecordId> {
}
