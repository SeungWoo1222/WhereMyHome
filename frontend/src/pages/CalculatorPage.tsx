import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { fetchRegions, searchApartments, fetchTrades, Apartment, Region } from '../api/client';
import './CalculatorPage.css';

const PRESET_FOODS = [
  { name: '치킨', price: 20000 },
  { name: '커피', price: 5000 },
  { name: '피자', price: 25000 },
  { name: '삼겹살', price: 15000 },
  { name: '짜장면', price: 8000 },
  { name: '햄버거 세트', price: 10000 },
];

const formatPrice = (manwon: number) => {
  if (manwon >= 10000) {
    const eok = Math.floor(manwon / 10000);
    const man = manwon % 10000;
    return man > 0 ? `${eok}억 ${man.toLocaleString()}만원` : `${eok}억`;
  }
  return `${manwon.toLocaleString()}만원`;
};

const CalculatorPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const initialPrice = searchParams.get('price') ? Number(searchParams.get('price')) : 90000;

  const [aptPrice, setAptPrice] = useState(initialPrice);
  const [priceInput, setPriceInput] = useState(String(initialPrice));
  const [foodName, setFoodName] = useState('치킨');
  const [foodPrice, setFoodPrice] = useState(20000);
  const [monthlySaving, setMonthlySaving] = useState(200);

  const [regionMap, setRegionMap] = useState<Record<string, Region[]>>({});
  const [selectedSido, setSelectedSido] = useState('');
  const [selectedRegionId, setSelectedRegionId] = useState<number | undefined>();
  const [aptSearch, setAptSearch] = useState('');
  const [aptResults, setAptResults] = useState<Apartment[]>([]);
  const [selectedApt, setSelectedApt] = useState<Apartment | null>(null);

  useEffect(() => {
    fetchRegions().then(data => setRegionMap(data)).catch(() => {});
  }, []);

  const sidos = Object.keys(regionMap);
  const sigungus = selectedSido ? (regionMap[selectedSido] || []) : [];

  const aptPriceWon = aptPrice * 10000;
  const foodCount = Math.ceil(aptPriceWon / Math.max(foodPrice, 1));
  const savingMonths = Math.ceil(aptPrice / Math.max(monthlySaving, 1));
  const savingYears = Math.floor(savingMonths / 12);
  const savingRemainMonths = savingMonths % 12;

  const handleAptSearch = () => {
    if (aptSearch.trim()) {
      searchApartments({ name: aptSearch, regionId: selectedRegionId, size: 20 }).then(data => {
        setAptResults(data.content);
      }).catch(() => {});
    }
  };

  const selectApt = (apt: Apartment) => {
    setSelectedApt(apt);
    setAptResults([]);
    setAptSearch(apt.complexName);
    fetchTrades(apt.id).then(trades => {
      if (trades.length > 0) {
        setAptPrice(trades[0].price);
        setPriceInput(String(trades[0].price));
      }
    }).catch(() => {});
  };

  const selectPresetFood = (food: typeof PRESET_FOODS[0]) => {
    setFoodName(food.name);
    setFoodPrice(food.price);
  };

  const handlePriceChange = (value: string) => {
    setPriceInput(value);
    const num = Number(value);
    if (!isNaN(num) && num > 0) setAptPrice(num);
  };

  const adjustSaving = (delta: number) => {
    setMonthlySaving(prev => Math.max(10, prev + delta));
  };

  return (
    <div className="calc-page">
      <h1>시세 계산기</h1>

      <div className="price-input-section">
        <p className="section-desc">아파트를 검색하거나 직접 가격을 입력하세요</p>

        <div className="apt-region-row">
          <select value={selectedSido} onChange={e => { setSelectedSido(e.target.value); setSelectedRegionId(undefined); }}>
            <option value="">시도 선택</option>
            {sidos.map(s => <option key={s} value={s}>{s}</option>)}
          </select>
          <select value={selectedRegionId ?? ''} onChange={e => setSelectedRegionId(e.target.value ? Number(e.target.value) : undefined)}>
            <option value="">시군구 선택</option>
            {sigungus.map(sg => <option key={sg.id} value={sg.id}>{sg.sigunguName}</option>)}
          </select>
        </div>

        <div className="apt-search-row">
          <input
            type="text"
            placeholder="아파트명 (예: 래미안)"
            value={aptSearch}
            onChange={e => setAptSearch(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleAptSearch()}
          />
          <button onClick={handleAptSearch}>찾기</button>
        </div>

        {aptResults.length > 0 && (
          <div className="apt-dropdown">
            {aptResults.map(apt => (
              <div key={apt.id} className="apt-option" onClick={() => selectApt(apt)}>
                <span className="apt-opt-name">{apt.complexName}</span>
                <span className="apt-opt-loc">{apt.sidoName} {apt.sigunguName} {apt.dongName}</span>
              </div>
            ))}
          </div>
        )}

        {selectedApt && (
          <p className="selected-apt">{selectedApt.complexName} ({selectedApt.sidoName} {selectedApt.sigunguName}) — 최근 거래가 자동 적용</p>
        )}

        <div className="price-direct">
          <label>아파트 가격 (만원)</label>
          <input type="number" value={priceInput} onChange={e => handlePriceChange(e.target.value)} />
          <span className="price-display">{formatPrice(aptPrice)}</span>
        </div>
      </div>

      <div className="calc-grid">
        <div className="calc-card">
          <h2>참으면 내 집 마련</h2>

          <div className="food-presets">
            {PRESET_FOODS.map(food => (
              <button key={food.name} className={foodName === food.name ? 'active' : ''} onClick={() => selectPresetFood(food)}>
                {food.name}
              </button>
            ))}
          </div>

          <div className="food-custom">
            <input type="text" placeholder="음식 이름" value={foodName} onChange={e => setFoodName(e.target.value)} />
            <input type="number" step="500" placeholder="가격 (원)" value={foodPrice} onChange={e => setFoodPrice(Number(e.target.value) || 500)} />
          </div>

          <div className="calc-result">
            <div className="result-label">{foodName}을(를)</div>
            <div className="result-number">{foodCount.toLocaleString()}번</div>
            <div className="result-label">참으면 {formatPrice(aptPrice)} 아파트를 살 수 있어요</div>
          </div>
          <div className="calc-sub">
            하루에 1번 참으면 {Math.ceil(foodCount / 365).toLocaleString()}년 걸려요
          </div>
        </div>

        <div className="calc-card">
          <h2>월급 모으기 계산기</h2>

          <div className="saving-control">
            <label>월 저축액</label>
            <div className="saving-buttons">
              <button onClick={() => adjustSaving(-100)}>-100</button>
              <button onClick={() => adjustSaving(-10)}>-10</button>
              <span className="saving-value">{monthlySaving}만원</span>
              <button onClick={() => adjustSaving(10)}>+10</button>
              <button onClick={() => adjustSaving(100)}>+100</button>
            </div>
          </div>

          <div className="calc-result">
            <div className="result-label">매달 {monthlySaving}만원씩 모으면</div>
            <div className="result-number">
              {savingYears > 0 ? `${savingYears}년 ` : ''}{savingRemainMonths > 0 ? `${savingRemainMonths}개월` : ''}
            </div>
            <div className="result-label">이면 {formatPrice(aptPrice)} 아파트를 살 수 있어요</div>
          </div>
          <div className="calc-sub">
            총 저축액: {formatPrice(monthlySaving * savingMonths)} (이자 미포함)
          </div>
        </div>
      </div>
    </div>
  );
};

export default CalculatorPage;
