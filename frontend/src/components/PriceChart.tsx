import React from 'react';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Filler,
} from 'chart.js';
import { MonthlyTrade } from '../api/client';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Filler);

interface Props {
  monthlyTrades: MonthlyTrade[];
}

const PriceChart: React.FC<Props> = ({ monthlyTrades }) => {
  if (monthlyTrades.length < 2) return null;

  const formatLabel = (price: number) => {
    if (price >= 10000) {
      const eok = Math.floor(price / 10000);
      const man = price % 10000;
      return man > 0 ? `${eok}억 ${man.toLocaleString()}만원` : `${eok}억`;
    }
    return `${price.toLocaleString()}만원`;
  };

  const data = {
    labels: monthlyTrades.map(t => t.month),
    datasets: [
      {
        label: '월 평균가',
        data: monthlyTrades.map(t => t.avgPrice),
        borderColor: '#0B6E6A',
        backgroundColor: 'rgba(11, 110, 106, 0.15)',
        fill: true,
        tension: 0.4,
        pointRadius: 4,
        pointHoverRadius: 7,
        borderWidth: 2,
      },
      {
        label: '월 최고가',
        data: monthlyTrades.map(t => t.maxPrice),
        borderColor: 'rgba(232, 93, 93, 0.4)',
        borderDash: [4, 4],
        tension: 0.4,
        pointRadius: 0,
        borderWidth: 1,
        fill: false,
      },
      {
        label: '월 최저가',
        data: monthlyTrades.map(t => t.minPrice),
        borderColor: 'rgba(74, 181, 122, 0.4)',
        borderDash: [4, 4],
        tension: 0.4,
        pointRadius: 0,
        borderWidth: 1,
        fill: false,
      },
    ],
  };

  const options = {
    responsive: true,
    interaction: {
      mode: 'index' as const,
      intersect: false,
    },
    plugins: {
      tooltip: {
        callbacks: {
          label: (context: any) => `${context.dataset.label}: ${formatLabel(context.parsed.y)}`,
        },
      },
    },
    scales: {
      y: {
        ticks: {
          callback: (value: any) => value >= 10000 ? `${Math.floor(value / 10000)}억` : `${value.toLocaleString()}만`,
        },
      },
      x: {
        ticks: { maxTicksLimit: 10 },
      },
    },
  };

  return (
    <div style={{ marginBottom: '2rem' }}>
      <h2 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>시세 추이</h2>
      <Line data={data} options={options} />
      <p style={{ fontSize: '0.75rem', color: '#aaa', textAlign: 'center', marginTop: '8px' }}>
        월별 평균가 기준 · 점선: 최고/최저
      </p>
    </div>
  );
};

export default PriceChart;
