import './Home.css';
import {useState} from 'react';
import logo from './logo.png';
import search from './search.png'; 

function Home() {
    const [text, setText] = useState('');
    const handleClick = () => {
        console.log(text);
    }
    return (
        <div className="App">
            <div className="App-header" style={{ backgroundImage: `url(${logo})`}}/>
            <input className= "search-bar" type="text" onChange={(e) => setText(e.target.value)} value = {text}/>
            <img className = "search-button" src={search} alt="search"  onClick = {() => {handleClick()}}/>
        </div>
    );
}

export default Home;
