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

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
}

export const fetchRegions = (sido?: string) =>
  api.get<Record<string, Region[]>>('/regions', { params: { sido } }).then(r => r.data);

export const searchApartments = (params: { regionId?: number; name?: string; page?: number; size?: number }) =>
  api.get<Page<Apartment>>('/apartments', { params }).then(r => r.data);

export const fetchApartment = (id: number) =>
  api.get<Apartment>(`/apartments/${id}`).then(r => r.data);

export const fetchTrades = (apartmentId: number) =>
  api.get<Trade[]>(`/apartments/${apartmentId}/trades`).then(r => r.data);
