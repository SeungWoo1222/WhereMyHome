import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchApartment, fetchTrades, Apartment, Trade } from '../api/client';
import './ApartmentPage.css';

const formatPrice = (price: number) => {
  if (price >= 10000) {
    const eok = Math.floor(price / 10000);
    const man = price % 10000;
    return man > 0 ? `${eok}억 ${man.toLocaleString()}만` : `${eok}억`;
  }
  return `${price.toLocaleString()}만`;
};

const ApartmentPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [apartment, setApartment] = useState<Apartment | null>(null);
  const [trades, setTrades] = useState<Trade[]>([]);

  useEffect(() => {
    const aptId = Number(id);
    fetchApartment(aptId).then(setApartment).catch(() => {});
    fetchTrades(aptId).then(setTrades).catch(() => {});
  }, [id]);

  const latestTrade = trades[0];

  return (
    <div className="apt-page">
      <button className="back-btn" onClick={() => navigate(-1)}>← 뒤로</button>

      <div className="apt-header">
        <h1>{apartment?.complexName || '로딩 중...'}</h1>
        {apartment && (
          <p className="apt-location">{apartment.sidoName} {apartment.sigunguName} {apartment.dongName} · {apartment.builtYear}년</p>
        )}
      </div>

      {latestTrade && (
        <div className="stat-grid">
          <div className="stat-card">
            <div className="stat-label">최근 거래가</div>
            <div className="stat-value">{formatPrice(latestTrade.price)}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">전용면적</div>
            <div className="stat-value">{latestTrade.area}㎡</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">거래일</div>
            <div className="stat-value">{latestTrade.tradeDate}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">총 거래 건수</div>
            <div className="stat-value">{trades.length.toLocaleString()}건</div>
          </div>
        </div>
      )}

      {latestTrade && (
        <button
          className="calc-link"
          onClick={() => navigate(`/calculator?price=${latestTrade.price}`)}
        >
          이 아파트로 시세 계산기 해보기 →
        </button>
      )}

      <h2 className="section-title">거래 이력</h2>
      <table className="trade-table">
        <thead>
          <tr>
            <th>거래일</th>
            <th>면적</th>
            <th>층</th>
            <th>거래금액</th>
          </tr>
        </thead>
        <tbody>
          {trades.map((t, i) => (
            <tr key={i}>
              <td>{t.tradeDate}</td>
              <td>{t.area}㎡</td>
              <td>{t.floor}층</td>
              <td className="price">{formatPrice(t.price)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ApartmentPage;
