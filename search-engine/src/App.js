import './App.css';
import * as mdb from 'mdb-ui-kit';
import logo from './logo.png';
import search from './search.png';  

function App() {
  return (
    <div className="App">
      <div className="App-header" style={{ backgroundImage: `url(${logo})`}}/>
      <input className= "search-bar" type="text"/>
    </div>
  );
}

export default App;
