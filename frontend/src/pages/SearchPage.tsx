import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { searchApartments, Apartment } from '../api/client';
import './SearchPage.css';

const SearchPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const name = searchParams.get('name') || '';
  const regionId = searchParams.get('regionId') ? Number(searchParams.get('regionId')) : undefined;
  const [apartments, setApartments] = useState<Apartment[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [keyword, setKeyword] = useState(name);

  useEffect(() => {
    searchApartments({ name, regionId, page, size: 20 }).then(data => {
      setApartments(data.content);
      setTotal(data.totalElements);
    }).catch(() => {});
  }, [name, regionId, page]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (keyword.trim()) {
      setPage(0);
      navigate(`/search?name=${encodeURIComponent(keyword.trim())}`);
    }
  };

  return (
    <div className="search-page">
      <form className="search-bar" onSubmit={handleSearch}>
        <input
          type="text"
          value={keyword}
          onChange={e => setKeyword(e.target.value)}
          placeholder="아파트명 검색"
        />
        <button type="submit">검색</button>
      </form>

      <p className="search-count">"{name}" 검색 결과 {total.toLocaleString()}건</p>

      <div className="apartment-list">
        {apartments.map(apt => (
          <div key={apt.id} className="apartment-card" onClick={() => navigate(`/apartment/${apt.id}`)}>
            <div className="apt-name">{apt.complexName}</div>
            <div className="apt-info">
              <span>{apt.sidoName} {apt.sigunguName} {apt.dongName}</span>
              <span>{apt.builtYear}년</span>
            </div>
          </div>
        ))}
      </div>

      {total > 20 && (
        <div className="pagination">
          <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>이전</button>
          <span>{page + 1} / {Math.ceil(total / 20)}</span>
          <button disabled={(page + 1) * 20 >= total} onClick={() => setPage(p => p + 1)}>다음</button>
        </div>
      )}
    </div>
  );
};

export default SearchPage;
