import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Navbar.css';

const Navbar: React.FC = () => {
  const location = useLocation();
  const isHome = location.pathname === '/';

  if (isHome) return null;

  return (
    <nav className="navbar">
      <Link to="/" className="nav-logo">내 집은 <span>어디에</span></Link>
      <div className="nav-links">
        <Link to="/regions" className={location.pathname === '/regions' ? 'active' : ''}>지역별 조회</Link>
        <Link to="/search?name=" className={location.pathname === '/search' ? 'active' : ''}>아파트 검색</Link>
      </div>
    </nav>
  );
};

export default Navbar;
