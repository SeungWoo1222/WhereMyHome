import React, { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import './CalculatorPage.css';

const FOODS = [
  { name: '치킨', price: 20000, emoji: '' },
  { name: '커피', price: 5000, emoji: '' },
  { name: '피자', price: 25000, emoji: '' },
  { name: '삼겹살', price: 15000, emoji: '' },
  { name: '짜장면', price: 8000, emoji: '' },
  { name: '햄버거 세트', price: 10000, emoji: '' },
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
  const [selectedFood, setSelectedFood] = useState(FOODS[0]);
  const [monthlySaving, setMonthlySaving] = useState(200);

  const aptPriceWon = aptPrice * 10000;
  const foodCount = Math.ceil(aptPriceWon / selectedFood.price);
  const savingMonths = Math.ceil(aptPrice / monthlySaving);
  const savingYears = Math.floor(savingMonths / 12);
  const savingRemainMonths = savingMonths % 12;

  return (
    <div className="calc-page">
      <h1>시세 계산기</h1>

      <div className="price-input-section">
        <label>아파트 가격</label>
        <div className="price-slider">
          <input
            type="range"
            min={5000}
            max={500000}
            step={1000}
            value={aptPrice}
            onChange={e => setAptPrice(Number(e.target.value))}
          />
          <span className="price-display">{formatPrice(aptPrice)}</span>
        </div>
      </div>

      <div className="calc-grid">
        <div className="calc-card food-card">
          <h2>참으면 내 집 마련</h2>
          <p className="calc-desc">좋아하는 음식을 골라보세요</p>

          <div className="food-grid">
            {FOODS.map(food => (
              <button
                key={food.name}
                className={selectedFood.name === food.name ? 'food-btn active' : 'food-btn'}
                onClick={() => setSelectedFood(food)}
              >
                <span className="food-name">{food.name}</span>
                <span className="food-price">{food.price.toLocaleString()}원</span>
              </button>
            ))}
          </div>

          <div className="calc-result">
            <div className="result-label">{selectedFood.name}을(를)</div>
            <div className="result-number">{foodCount.toLocaleString()}번</div>
            <div className="result-label">참으면 {formatPrice(aptPrice)} 아파트를 살 수 있어요</div>
          </div>

          <div className="calc-sub">
            하루에 1번 참으면 {Math.ceil(foodCount / 365).toLocaleString()}년 걸려요
          </div>
        </div>

        <div className="calc-card saving-card">
          <h2>월급 모으기 계산기</h2>
          <p className="calc-desc">매달 얼마씩 모을 수 있나요?</p>

          <div className="saving-input">
            <label>월 저축액</label>
            <div className="saving-slider">
              <input
                type="range"
                min={50}
                max={1000}
                step={10}
                value={monthlySaving}
                onChange={e => setMonthlySaving(Number(e.target.value))}
              />
              <span>{monthlySaving}만원</span>
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
