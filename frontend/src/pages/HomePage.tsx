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

const STATS = [
  { k: '누적 실거래', v: '1,055', unit: '만 건' },
  { k: '등록 단지', v: '43,360', unit: '곳' },
  { k: '커버 지역', v: '249', unit: '개 시군구' },
  { k: '실거래 이력', v: '20', unit: '년' },
];

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
      <nav className="home-nav">
        <div className="home-logo">내 집은 <span>어디에</span></div>
        <div className="home-nav-links">
          <button onClick={() => navigate('/regions')}>지역별 조회</button>
          <button onClick={() => navigate('/search?name=')}>아파트 검색</button>
        </div>
      </nav>

      <section className="home-hero">
        <div className="hero-eyebrow">국토부 실거래가 · 매월 갱신</div>
        <h1 className="home-title">전국 아파트 실거래가를,<br />검색 한번으로</h1>
        <p className="home-subtitle">전국 아파트 1,055만 건의 실거래를 단지·지역으로 바로 찾고, 월별 시세 추이까지 확인하세요.</p>

        <form className="home-search" onSubmit={handleSearch}>
          <input
            type="text"
            placeholder="단지명·지역으로 검색 (예: 래미안, 강남구)"
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

        <div className="home-stat-strip">
          {STATS.map(s => (
            <div className="stat-cell" key={s.k}>
              <span className="stat-v">{s.v}<em>{s.unit}</em></span>
              <span className="stat-k">{s.k}</span>
            </div>
          ))}
        </div>
      </section>

      <section className="home-features">
        <div className="feature-card" onClick={() => navigate('/regions')}>
          <div className="feature-icon">◱</div>
          <h3>지역별 조회</h3>
          <p>시도·시군구별 아파트 목록을 한눈에 훑어보세요.</p>
        </div>
        <div className="feature-card" onClick={() => navigate('/search?name=래미안')}>
          <div className="feature-icon">⇄</div>
          <h3>시세 비교</h3>
          <p>두 단지의 가격 흐름을 나란히 겹쳐 봅니다.</p>
        </div>
      </section>

      <section className="home-popular">
        <p className="popular-label">인기 지역</p>
        <div className="popular-grid">
          {POPULAR.map(r => (
            <button key={r.regionId} onClick={() => navigate(`/search?regionId=${r.regionId}&name=`)}>
              {r.label}
            </button>
          ))}
        </div>
      </section>
    </div>
  );
};

export default HomePage;
