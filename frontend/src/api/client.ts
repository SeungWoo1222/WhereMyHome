import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
});

export interface Region {
  id: number;
  sidoName: string;
  sigunguName: string;
  sigunguCode: string;
}

export interface Apartment {
  id: number;
  complexName: string;
  dongName: string;
  builtYear: number;
  sidoName: string;
  sigunguName: string;
}

export interface Trade {
  tradeDate: string;
  area: number;
  floor: number;
  price: number;
}

export interface SliceResult<T> {
  content: T[];
  last: boolean;   // Spring Slice 직렬화 시 hasNext 대신 last(마지막 페이지 여부) 제공
  number: number;
}

export const fetchRegions = (sido?: string) =>
  api.get<Record<string, Region[]>>('/regions', { params: { sido } }).then(r => r.data);

export const searchApartments = (params: { regionId?: number; name?: string; page?: number; size?: number }) =>
  api.get<SliceResult<Apartment>>('/apartments', { params }).then(r => r.data);

export const fetchApartment = (id: number) =>
  api.get<Apartment>(`/apartments/${id}`).then(r => r.data);

export const fetchTrades = (apartmentId: number) =>
  api.get<Trade[]>(`/apartments/${apartmentId}/trades`).then(r => r.data);

export interface MonthlyTrade {
  month: string;
  avgPrice: number;
  minPrice: number;
  maxPrice: number;
  count: number;
}

export const fetchMonthlyTrades = (apartmentId: number, all: boolean = false, area?: string) =>
  api.get<MonthlyTrade[]>(`/apartments/${apartmentId}/trades/monthly`, { params: { all, area } }).then(r => r.data);
