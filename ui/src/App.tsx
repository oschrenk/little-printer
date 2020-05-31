import React from 'react';
import logo from './images/printer.png'

import { Connection } from './components/Connection'

import './App.css';

function App() {
  return (
    <div className="app">
      <div className="main">
        <div className="center">
          <img src={logo} className="printer" alt="Little Printer"/>
          <Connection />
        </div>
      </div>
    </div>
  );
}

export default App;
