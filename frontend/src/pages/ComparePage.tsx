import React, { useEffect, useState, useMemo } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip,
  Legend,
} from 'chart.js';
import { fetchApartment, fetchTrades, fetchMonthlyTrades, Apartment, Trade, MonthlyTrade } from '../api/client';
import './ComparePage.css';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Legend);

const formatPrice = (price: number) => {
  if (price >= 10000) {
    const eok = Math.floor(price / 10000);
    const man = price % 10000;
    return man > 0 ? `${eok}억 ${man.toLocaleString()}만` : `${eok}억`;
  }
  return `${price.toLocaleString()}만`;
};

type Period = '1y' | '2y' | '3y' | 'all';
const periodLabel: Record<Period, string> = { '1y': '1년', '2y': '2년', '3y': '3년', 'all': '전체' };

const ComparePage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const idA = Number(searchParams.get('a'));
  const idB = Number(searchParams.get('b'));
  const [aptA, setAptA] = useState<Apartment | null>(null);
  const [aptB, setAptB] = useState<Apartment | null>(null);
  const [tradesA, setTradesA] = useState<Trade[]>([]);
  const [tradesB, setTradesB] = useState<Trade[]>([]);
  const [monthlyA, setMonthlyA] = useState<MonthlyTrade[]>([]);
  const [monthlyB, setMonthlyB] = useState<MonthlyTrade[]>([]);
  const [period, setPeriod] = useState<Period>('3y');
  const [showCountA, setShowCountA] = useState(10);
  const [showCountB, setShowCountB] = useState(10);

  useEffect(() => {
    fetchApartment(idA).then(setAptA).catch(() => {});
    fetchApartment(idB).then(setAptB).catch(() => {});
    fetchTrades(idA).then(setTradesA).catch(() => {});
    fetchTrades(idB).then(setTradesB).catch(() => {});
    fetchMonthlyTrades(idA).then(setMonthlyA).catch(() => {});
    fetchMonthlyTrades(idB).then(setMonthlyB).catch(() => {});
  }, [idA, idB]);

  const getMinMonth = (p: Period): string | null => {
    if (p === 'all') return null;
    const years = p === '1y' ? 1 : p === '2y' ? 2 : 3;
    const d = new Date();
    d.setFullYear(d.getFullYear() - years);
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
  };

  const minMonth = getMinMonth(period);

  const filteredA = useMemo(() =>
    minMonth ? monthlyA.filter(m => m.month >= minMonth) : monthlyA,
    [monthlyA, minMonth]
  );
  const filteredB = useMemo(() =>
    minMonth ? monthlyB.filter(m => m.month >= minMonth) : monthlyB,
    [monthlyB, minMonth]
  );

  const allMonths = useMemo(() =>
    Array.from(new Set([...filteredA.map(m => m.month), ...filteredB.map(m => m.month)])).sort(),
    [filteredA, filteredB]
  );

  const mapA = useMemo(() => new Map(filteredA.map(m => [m.month, m.avgPrice])), [filteredA]);
  const mapB = useMemo(() => new Map(filteredB.map(m => [m.month, m.avgPrice])), [filteredB]);

  const latestA = tradesA[0];
  const latestB = tradesB[0];
  const nameA = aptA?.complexName || `#${idA}`;
  const nameB = aptB?.complexName || `#${idB}`;

  const chartData = {
    labels: allMonths,
    datasets: [
      {
        label: nameA,
        data: allMonths.map(m => mapA.get(m) ?? null),
        borderColor: '#4a7ab5', backgroundColor: 'rgba(74, 122, 181, 0.1)',
        fill: true, tension: 0.4, pointRadius: 4, pointHoverRadius: 7, borderWidth: 2, spanGaps: true,
      },
      {
        label: nameB,
        data: allMonths.map(m => mapB.get(m) ?? null),
        borderColor: '#e85d5d', backgroundColor: 'rgba(232, 93, 93, 0.1)',
        fill: true, tension: 0.4, pointRadius: 4, pointHoverRadius: 7, borderWidth: 2, spanGaps: true,
      },
    ],
  };

  const chartOptions = {
    responsive: true,
    plugins: {
      tooltip: {
        callbacks: {
          label: (context: any) => `${context.dataset.label}: ${formatPrice(context.parsed.y)}`,
        },
      },
    },
    scales: {
      y: { ticks: { callback: (value: any) => value >= 10000 ? `${Math.floor(value / 10000)}억` : `${value.toLocaleString()}만` } },
      x: { ticks: { maxTicksLimit: 8 } },
    },
  };

  const visibleA = tradesA.slice(0, showCountA);
  const visibleB = tradesB.slice(0, showCountB);

  return (
    <div className="compare-page">
      <button className="back-btn" onClick={() => navigate(-1)}>← 뒤로</button>
      <h1>아파트 비교</h1>

      <div className="compare-grid">
        <div className="compare-card blue">
          <div className="compare-name">{nameA}</div>
          {aptA && <div className="compare-loc">{aptA.sidoName} {aptA.sigunguName} {aptA.dongName} · {aptA.builtYear}년</div>}
          {latestA && (
            <>
              <div className="compare-price">{formatPrice(latestA.price)}</div>
              <div className="compare-detail">{latestA.area}㎡ · {latestA.tradeDate}</div>
              <div className="compare-count">최근 1년 거래 {tradesA.length}건</div>
            </>
          )}
        </div>
        <div className="vs">VS</div>
        <div className="compare-card red">
          <div className="compare-name">{nameB}</div>
          {aptB && <div className="compare-loc">{aptB.sidoName} {aptB.sigunguName} {aptB.dongName} · {aptB.builtYear}년</div>}
          {latestB && (
            <>
              <div className="compare-price">{formatPrice(latestB.price)}</div>
              <div className="compare-detail">{latestB.area}㎡ · {latestB.tradeDate}</div>
              <div className="compare-count">최근 1년 거래 {tradesB.length}건</div>
            </>
          )}
        </div>
      </div>

      <div className="compare-chart">
        <div className="chart-header">
          <h2>시세 추이 비교</h2>
          <div className="period-tabs">
            {(['1y', '2y', '3y', 'all'] as Period[]).map(p => (
              <button key={p} className={period === p ? 'active' : ''} onClick={() => setPeriod(p)}>
                {periodLabel[p]}
              </button>
            ))}
          </div>
        </div>
        {allMonths.length > 0 && <Line data={chartData} options={chartOptions} />}
        {allMonths.length === 0 && <p style={{color:'#aaa',textAlign:'center'}}>해당 기간에 거래 데이터가 없습니다</p>}
        <p style={{ fontSize: '0.75rem', color: '#aaa', textAlign: 'center', marginTop: '8px' }}>월별 평균가 기준</p>
      </div>

      <div className="compare-tables">
        <div className="compare-table-section">
          <h3>{nameA} 거래 이력</h3>
          <table className="trade-table">
            <thead><tr><th>거래일</th><th>면적</th><th>층</th><th>거래금액</th></tr></thead>
            <tbody>
              {visibleA.map((t, i) => (
                <tr key={i}><td>{t.tradeDate}</td><td>{t.area}㎡</td><td>{t.floor}층</td><td className="price">{formatPrice(t.price)}</td></tr>
              ))}
            </tbody>
          </table>
          {showCountA < tradesA.length && (
            <button className="load-more" onClick={() => setShowCountA(c => c + 10)}>더보기 ({tradesA.length - showCountA}건 남음)</button>
          )}
        </div>

        <div className="compare-table-section">
          <h3>{nameB} 거래 이력</h3>
          <table className="trade-table">
            <thead><tr><th>거래일</th><th>면적</th><th>층</th><th>거래금액</th></tr></thead>
            <tbody>
              {visibleB.map((t, i) => (
                <tr key={i}><td>{t.tradeDate}</td><td>{t.area}㎡</td><td>{t.floor}층</td><td className="price">{formatPrice(t.price)}</td></tr>
              ))}
            </tbody>
          </table>
          {showCountB < tradesB.length && (
            <button className="load-more" onClick={() => setShowCountB(c => c + 10)}>더보기 ({tradesB.length - showCountB}건 남음)</button>
          )}
        </div>
      </div>
    </div>
  );
};

export default ComparePage;
