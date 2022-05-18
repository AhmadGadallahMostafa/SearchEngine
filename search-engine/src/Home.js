import './Home.css';
import {useState} from 'react';
import logo from './logo.png';
import search from './search.png'; 
import { useNavigate } from "react-router-dom";

function Home() {
    const [text, setText] = useState('');
    let navigate = useNavigate();
    const handleClick = () => {
        navigate(`/search?q=${text}`);
    }
    return (
        <div className="App">
            <div className="App-header" style={{ backgroundImage: `url(${logo})`}} onClick={(e) => navigate('/')}/>
            <input className= "search-bar" type="text" onChange={(e) => setText(e.target.value)} value = {text}/>
            <img className = "search-button" src={search} alt="search"  onClick = {() => {handleClick()}}/>
        </div>
    );
}

export default Home;
