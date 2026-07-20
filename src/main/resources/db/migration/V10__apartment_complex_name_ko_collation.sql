-- complex_name 컬럼이 en_US.utf8(영어 규칙) collation을 쓰고 있어서
-- ORDER BY complex_name 시 한글 가나다순이 아닌 이상한 순서로 정렬됨.
-- 한글 ICU collation으로 바꿔서 이름순 정렬이 실제 가나다순이 되도록 함.
-- 43,360행짜리 작은 테이블이라 재작성 비용 거의 없음.
ALTER TABLE apartment_complexes
    ALTER COLUMN complex_name TYPE varchar(100) COLLATE "ko-KR-x-icu";
