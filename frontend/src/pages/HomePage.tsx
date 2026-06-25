import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './HomePage.css';

const POPULAR = [
  { label: '강남구', regionId: 24 },
  { label: '서초구', regionId: 23 },
  { label: '송파구', regionId: 25 },
  { label: '분당구', regionId: 83 },
  { label: '해운대구', regionId: 35 },
  { label: '수성구', regionId: 48 },
];

const QUICK_SEARCH = ['래미안', '자이', '힐스테이트', '아이파크', '푸르지오'];

const HomePage: React.FC = () => {
  const [keyword, setKeyword] = useState('');
  const navigate = useNavigate();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (keyword.trim()) {
      navigate(`/search?name=${encodeURIComponent(keyword.trim())}`);
    }
  };

  return (
    <div className="home">
      <div className="home-hero">
        <h1 className="home-title">내 집은 어디에</h1>
        <p className="home-subtitle">전국 아파트 실거래가를 한눈에</p>
        <form className="home-search" onSubmit={handleSearch}>
          <input
            type="text"
            placeholder="아파트명을 검색하세요 (예: 래미안, 자이, 힐스테이트)"
            value={keyword}
            onChange={e => setKeyword(e.target.value)}
          />
          <button type="submit">검색</button>
        </form>

        <div className="quick-search">
          {QUICK_SEARCH.map(name => (
            <button key={name} onClick={() => navigate(`/search?name=${encodeURIComponent(name)}`)}>
              {name}
            </button>
          ))}
        </div>
      </div>

      <div className="home-features">
        <div className="feature-card" onClick={() => navigate('/regions')}>
          <div className="feature-icon">&#127968;</div>
          <h3>지역별 조회</h3>
          <p>시도 / 시군구별 아파트 목록을 한눈에 확인하세요</p>
        </div>
        <div className="feature-card" onClick={() => navigate('/calculator')}>
          <div className="feature-icon">&#128178;</div>
          <h3>시세 계산기</h3>
          <p>좋아하는 음식 몇 번 참으면 살 수 있을까?</p>
        </div>
        <div className="feature-card" onClick={() => navigate('/search?name=래미안')}>
          <div className="feature-icon">&#128200;</div>
          <h3>시세 비교</h3>
          <p>두 아파트의 가격 추이를 비교해보세요</p>
        </div>
      </div>

      <div className="home-popular">
        <p className="popular-label">인기 지역</p>
        <div className="popular-grid">
          {POPULAR.map(r => (
            <button key={r.regionId} onClick={() => navigate(`/search?regionId=${r.regionId}&name=`)}>
              {r.label}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default HomePage;
