import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';

window.addEventListener('unhandledrejection', e => e.preventDefault());

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);
root.render(
  <App />
);
