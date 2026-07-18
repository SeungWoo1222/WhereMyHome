import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { searchApartments, Apartment } from '../api/client';
import './SearchPage.css';

type SortKey = 'name' | 'year-asc' | 'year-desc';

const SearchPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const name = searchParams.get('name') || '';
  const regionId = searchParams.get('regionId') ? Number(searchParams.get('regionId')) : undefined;
  const [apartments, setApartments] = useState<Apartment[]>([]);
  const [hasNext, setHasNext] = useState(false);
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState(name);
  const [sort, setSort] = useState<SortKey>('name');
  const [yearMin, setYearMin] = useState('');
  const [yearMax, setYearMax] = useState('');
  const [compareList, setCompareList] = useState<Apartment[]>([]);

  useEffect(() => {
    searchApartments({ name, regionId, page, size: 100 }).then(data => {
      setApartments(data.content);
      setHasNext(data.hasNext);
    }).catch(() => {});
  }, [name, regionId, page]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (keyword.trim().length < 3) return;
    setPage(0);
    navigate(`/search?name=${encodeURIComponent(keyword.trim())}`);
  };

  const toggleCompare = (apt: Apartment) => {
    setCompareList(prev => {
      const exists = prev.find(a => a.id === apt.id);
      if (exists) return prev.filter(a => a.id !== apt.id);
      if (prev.length >= 2) return prev;
      return [...prev, apt];
    });
  };

  const filtered = apartments
    .filter(apt => {
      if (yearMin && apt.builtYear < Number(yearMin)) return false;
      if (yearMax && apt.builtYear > Number(yearMax)) return false;
      return true;
    })
    .sort((a, b) => {
      if (sort === 'year-asc') return (a.builtYear || 0) - (b.builtYear || 0);
      if (sort === 'year-desc') return (b.builtYear || 0) - (a.builtYear || 0);
      return a.complexName.localeCompare(b.complexName);
    });

  return (
    <div className="search-page">
      <form className="search-bar" onSubmit={handleSearch}>
        <input
          type="text"
          value={keyword}
          onChange={e => setKeyword(e.target.value)}
          placeholder="단지명·지역 검색 (3글자 이상)"
        />
        <button type="submit">검색</button>
      </form>

      <div className="filter-bar">
        <div className="filter-group">
          <label>건축년도</label>
          <input type="number" placeholder="시작" value={yearMin} onChange={e => setYearMin(e.target.value)} />
          <span>~</span>
          <input type="number" placeholder="끝" value={yearMax} onChange={e => setYearMax(e.target.value)} />
        </div>
        <div className="filter-group">
          <label>정렬</label>
          <select value={sort} onChange={e => setSort(e.target.value as SortKey)}>
            <option value="name">이름순</option>
            <option value="year-desc">최신순</option>
            <option value="year-asc">오래된순</option>
          </select>
        </div>
      </div>

      <p className="search-count">
        {name ? `"${name}" ` : ''}검색 결과 {filtered.length}건
      </p>

      {compareList.length === 2 && (
        <button
          className="compare-btn"
          onClick={() => navigate(`/compare?a=${compareList[0].id}&b=${compareList[1].id}`)}
        >
          {compareList[0].complexName} vs {compareList[1].complexName} 비교하기
        </button>
      )}

      <div className="apartment-list">
        {filtered.map(apt => (
          <div key={apt.id} className="apartment-card">
            <div className="apt-card-main" onClick={() => navigate(`/apartment/${apt.id}`)}>
              <div className="apt-name">{apt.complexName}</div>
              <div className="apt-info">
                <span>{apt.sidoName} {apt.sigunguName} {apt.dongName}</span>
                <span>{apt.builtYear}년</span>
              </div>
            </div>
            <button
              className={`compare-toggle ${compareList.find(a => a.id === apt.id) ? 'active' : ''}`}
              onClick={() => toggleCompare(apt)}
            >
              {compareList.find(a => a.id === apt.id) ? '선택됨' : '비교'}
            </button>
          </div>
        ))}
      </div>

      {(page > 0 || hasNext) && (
        <div className="pagination">
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>이전</button>
          <span>{page + 1}페이지</span>
          <button disabled={!hasNext} onClick={() => setPage(p => p + 1)}>다음</button>
        </div>
      )}
    </div>
  );
};

export default SearchPage;
