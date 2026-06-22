import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Navbar.css';

const Navbar: React.FC = () => {
  const location = useLocation();
  const isHome = location.pathname === '/';

  if (isHome) return null;

  return (
    <nav className="navbar">
      <Link to="/" className="nav-logo">WhereMyHome</Link>
      <div className="nav-links">
        <Link to="/regions" className={location.pathname === '/regions' ? 'active' : ''}>지역별 조회</Link>
        <Link to="/search?name=" className={location.pathname === '/search' ? 'active' : ''}>아파트 검색</Link>
        <Link to="/calculator" className={location.pathname === '/calculator' ? 'active' : ''}>시세 계산기</Link>
      </div>
    </nav>
  );
};

export default Navbar;
