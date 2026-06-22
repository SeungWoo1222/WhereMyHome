import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchRegions, Region } from '../api/client';
import './RegionPage.css';

const SIDO_ORDER = [
  '서울특별시', '경기도', '인천광역시', '부산광역시', '대구광역시',
  '광주광역시', '대전광역시', '울산광역시', '세종특별자치시',
  '강원특별자치도', '충청북도', '충청남도', '전북특별자치도',
  '전라남도', '경상북도', '경상남도', '제주특별자치도'
];

const RegionPage: React.FC = () => {
  const navigate = useNavigate();
  const [regionMap, setRegionMap] = useState<Record<string, Region[]>>({});
  const [selectedSido, setSelectedSido] = useState<string>('');

  useEffect(() => {
    fetchRegions().then(data => {
      setRegionMap(data);
      setSelectedSido('서울특별시');
    });
  }, []);

  const sigungus = selectedSido ? (regionMap[selectedSido] || []) : [];

  return (
    <div className="region-page">
      <h1>지역별 아파트 조회</h1>

      <div className="sido-tabs">
        {SIDO_ORDER.filter(s => regionMap[s]).map(sido => (
          <button
            key={sido}
            className={selectedSido === sido ? 'active' : ''}
            onClick={() => setSelectedSido(sido)}
          >
            {sido.replace('특별시', '').replace('광역시', '').replace('특별자치시', '').replace('특별자치도', '').replace('도', '')}
          </button>
        ))}
      </div>

      <div className="sigungu-grid">
        {sigungus.map(sg => (
          <div
            key={sg.id}
            className="sigungu-card"
            onClick={() => navigate(`/search?regionId=${sg.id}&name=`)}
          >
            <div className="sg-name">{sg.sigunguName}</div>
            <div className="sg-code">{sg.sigunguCode}</div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default RegionPage;
