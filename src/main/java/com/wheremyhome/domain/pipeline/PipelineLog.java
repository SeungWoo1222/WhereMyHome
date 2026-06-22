package com.wheremyhome.domain.pipeline;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pipeline_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PipelineLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false, length = 50)
    private String jobName;

    @Column(name = "sigungu_code", nullable = false, length = 5)
    private String sigunguCode;

    @Column(name = "trade_year", nullable = false)
    private Short tradeYear;

    @Column(name = "trade_month", nullable = false)
    private Short tradeMonth;

    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @Column(name = "record_count")
    private Integer recordCount;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    public static PipelineLog start(String jobName, String sigunguCode, int year, int month) {
        PipelineLog log = new PipelineLog();
        log.jobName = jobName;
        log.sigunguCode = sigunguCode;
        log.tradeYear = (short) year;
        log.tradeMonth = (short) month;
        log.status = "RUNNING";
        log.startedAt = LocalDateTime.now();
        return log;
    }

    public void success(int recordCount) {
        this.status = "SUCCESS";
        this.recordCount = recordCount;
        this.finishedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
        this.finishedAt = LocalDateTime.now();
    }
}
