import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import SearchPage from './pages/SearchPage';
import ApartmentPage from './pages/ApartmentPage';
import RegionPage from './pages/RegionPage';
import CalculatorPage from './pages/CalculatorPage';
import ComparePage from './pages/ComparePage';

function App() {
  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/search" element={<SearchPage />} />
        <Route path="/apartment/:id" element={<ApartmentPage />} />
        <Route path="/regions" element={<RegionPage />} />
        <Route path="/calculator" element={<CalculatorPage />} />
        <Route path="/compare" element={<ComparePage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
