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
import { Trade } from '../api/client';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Filler);

interface Props {
  trades: Trade[];
}

const PriceChart: React.FC<Props> = ({ trades }) => {
  if (trades.length < 2) return null;

  const sorted = [...trades].reverse();

  const labels = sorted.map(t => t.tradeDate);
  const prices = sorted.map(t => t.price);

  const data = {
    labels,
    datasets: [
      {
        label: '거래가 (만원)',
        data: prices,
        borderColor: '#4a7ab5',
        backgroundColor: 'rgba(74, 122, 181, 0.1)',
        fill: true,
        tension: 0.3,
        pointRadius: 3,
        pointHoverRadius: 6,
      },
    ],
  };

  const options = {
    responsive: true,
    plugins: {
      tooltip: {
        callbacks: {
          label: (context: any) => {
            const price = context.parsed.y;
            if (price >= 10000) {
              const eok = Math.floor(price / 10000);
              const man = price % 10000;
              return man > 0 ? `${eok}억 ${man.toLocaleString()}만원` : `${eok}억`;
            }
            return `${price.toLocaleString()}만원`;
          },
        },
      },
    },
    scales: {
      y: {
        ticks: {
          callback: (value: any) => {
            if (value >= 10000) return `${Math.floor(value / 10000)}억`;
            return `${value.toLocaleString()}만`;
          },
        },
      },
      x: {
        ticks: {
          maxTicksLimit: 8,
        },
      },
    },
  };

  return (
    <div style={{ marginBottom: '2rem' }}>
      <h2 style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>시세 추이</h2>
      <Line data={data} options={options} />
    </div>
  );
};

export default PriceChart;
