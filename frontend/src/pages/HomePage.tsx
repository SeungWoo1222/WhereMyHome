import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './HomePage.css';

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
      </div>
    </div>
  );
};

export default HomePage;
