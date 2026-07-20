import React, { useEffect, useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { fetchApartment, fetchTrades, fetchMonthlyTrades, Apartment, Trade, MonthlyTrade } from '../api/client';
import PriceChart from '../components/PriceChart';
import './ApartmentPage.css';

const formatPrice = (price: number) => {
  if (price >= 10000) {
    const eok = Math.floor(price / 10000);
    const man = price % 10000;
    return man > 0 ? `${eok}억 ${man.toLocaleString()}만` : `${eok}억`;
  }
  return `${price.toLocaleString()}만`;
};

type Period = '6m' | '1y' | '3y' | 'all';
const periodLabel: Record<Period, string> = { '6m': '6개월', '1y': '1년', '3y': '3년', 'all': '전체' };

const ApartmentPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [apartment, setApartment] = useState<Apartment | null>(null);
  const [trades, setTrades] = useState<Trade[]>([]);
  const [monthlyTrades, setMonthlyTrades] = useState<MonthlyTrade[]>([]);
  const [period, setPeriod] = useState<Period>('1y');
  const [selectedArea, setSelectedArea] = useState<string>('all');
  const [showCount, setShowCount] = useState(20);

  useEffect(() => {
    const aptId = Number(id);
    fetchApartment(aptId).then(setApartment).catch(() => {});
    fetchTrades(aptId).then(setTrades).catch(() => {});
  }, [id]);

  // period가 '전체'일 때만 MV(최근 3년) 대신 원본 전체 집계를 다시 요청
  // selectedArea가 바뀌면 그 면적만 필터된 월별 집계를 다시 요청
  const isAllPeriod = period === 'all';
  const areaParam = selectedArea !== 'all' ? selectedArea : undefined;
  useEffect(() => {
    fetchMonthlyTrades(Number(id), isAllPeriod, areaParam).then(setMonthlyTrades).catch(() => {});
  }, [id, isAllPeriod, areaParam]);

  const latestDate = trades.length > 0 ? trades[0].tradeDate : null;

  const getPeriodMinDate = (p: Period): string | null => {
    if (p === 'all' || !latestDate) return null;
    const d = new Date(latestDate);
    if (p === '6m') d.setMonth(d.getMonth() - 6);
    else {
      const years = p === '1y' ? 1 : 3;
      d.setFullYear(d.getFullYear() - years);
    }
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  };

  const areas = useMemo(() => {
    const set = new Map<string, number>();
    trades.forEach(t => {
      const key = String(t.area);
      set.set(key, (set.get(key) || 0) + 1);
    });
    return Array.from(set.entries())
      .sort((a, b) => b[1] - a[1])
      .map(([area]) => area);
  }, [trades]);

  const filteredMonthlyTrades = useMemo(() => {
    if (period === 'all') return monthlyTrades;
    const months = period === '6m' ? 6 : period === '1y' ? 12 : 36;
    const from = new Date();
    from.setMonth(from.getMonth() - months);
    const fromStr = `${from.getFullYear()}-${String(from.getMonth() + 1).padStart(2, '0')}`;
    return monthlyTrades.filter(t => t.month >= fromStr);
  }, [monthlyTrades, period]);

  const filteredTrades = useMemo(() => {
    const minDate = getPeriodMinDate(period);
    return trades.filter(t => {
      if (minDate && t.tradeDate < minDate) return false;
      if (selectedArea !== 'all' && String(t.area) !== selectedArea) return false;
      return true;
    });
  }, [trades, period, selectedArea, latestDate]);

  const latestTrade = filteredTrades[0];
  const visibleTrades = filteredTrades.slice(0, showCount);

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
            <div className="stat-label">최근 거래일</div>
            <div className="stat-value">{latestTrade.tradeDate}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label">조회 거래 건수</div>
            <div className="stat-value">{filteredTrades.length.toLocaleString()}건</div>
          </div>
        </div>
      )}

      <div className="filter-section">
        <div className="period-tabs">
          {(['6m', '1y', '3y', 'all'] as Period[]).map(p => (
            <button key={p} className={period === p ? 'active' : ''} onClick={() => setPeriod(p)}>
              {periodLabel[p]}
            </button>
          ))}
          {latestDate && <span className="period-hint">최신: {latestDate}</span>}
        </div>
        {areas.length > 1 && (
          <div className="area-tabs">
            <button className={selectedArea === 'all' ? 'active' : ''} onClick={() => setSelectedArea('all')}>전체 면적</button>
            {areas.slice(0, 5).map(area => (
              <button key={area} className={selectedArea === area ? 'active' : ''} onClick={() => setSelectedArea(area)}>
                {area}㎡
              </button>
            ))}
          </div>
        )}
      </div>

      <PriceChart monthlyTrades={filteredMonthlyTrades} />

      <h2 className="section-title">거래 이력 ({filteredTrades.length}건)</h2>
      <table className="trade-table">
        <thead>
          <tr><th>거래일</th><th>면적</th><th>층</th><th>거래금액</th></tr>
        </thead>
        <tbody>
          {visibleTrades.map((t, i) => (
            <tr key={i}>
              <td>{t.tradeDate}</td>
              <td>{t.area}㎡</td>
              <td>{t.floor}층</td>
              <td className="price">{formatPrice(t.price)}</td>
            </tr>
          ))}
        </tbody>
      </table>

      {showCount < filteredTrades.length && (
        <button className="load-more" onClick={() => setShowCount(c => c + 20)}>
          더보기 ({filteredTrades.length - showCount}건 남음)
        </button>
      )}
    </div>
  );
};

export default ApartmentPage;
