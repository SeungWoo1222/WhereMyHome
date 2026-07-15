package com.wheremyhome.domain.pipeline;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pipeline_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자, protected로 외부 직접 생성 차단
public class PipelineLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB AUTO_INCREMENT에 id 생성 위임
    private Long id;

    // 배치 잡 이름
    @Column(name = "job_name", nullable = false, length = 50)
    private String jobName;

    // 수집한 시군구 코드
    @Column(name = "sigungu_code", nullable = false, length = 5)
    private String sigunguCode;

    // 수집한 거래 연도
    @Column(name = "trade_year", nullable = false)
    private Short tradeYear;

    // 수집한 거래 월
    @Column(name = "trade_month", nullable = false)
    private Short tradeMonth;

    // 상태: RUNNING / SUCCESS / FAILED
    @Column(name = "status", nullable = false, length = 10)
    private String status;

    // 수집된 레코드 수, 실패 시 null
    @Column(name = "record_count")
    private Integer recordCount;

    // 실패 시 에러 메시지
    @Column(name = "error_message")
    private String errorMessage;

    // 배치 시작 시각
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    // 배치 완료 시각, 진행 중이면 null
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
