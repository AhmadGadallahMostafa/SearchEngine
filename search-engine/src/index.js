import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import Home from './Home';
import Search from './Search';
import Speech from './Speech';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <Router>
      <Routes>
        <Route exact path="/" element={<Home />} />
      </Routes>
      <Routes>
        <Route exact path="/search" element={<Search />} />
      </Routes>
      <Routes>
        <Route exact path="/speech" element={<Speech />} />
      </Routes>
    </Router>
  </React.StrictMode>
);
