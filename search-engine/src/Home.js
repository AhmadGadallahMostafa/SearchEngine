import './Home.css';
import {useState} from 'react';
import logo from './logo.png';
import search from './search.png'; 
import { useNavigate } from "react-router-dom";
import Speech from './Speech';
import reset from './reset.png';

function Home() {
    const [text, setText] = useState('');
    let navigate = useNavigate();
    const handleClick = () => {
        navigate(`/search?q=${text}`);
    }

    const handleSpeech = (t) => {
        setText(text + ' ' + t);
    }

    const handleReset = () => {
        console.log("ana geit");
        setText('');
        console.log(text);
    }

    return (
        <div className="App">
            <div className="App-header" style={{ backgroundImage: `url(${logo})`}} onClick={(e) => navigate('/')}/>
            <input className= "search-bar" type="text" onKeyPress={(e) => {
                        if (e.key === "Enter") {
                            console.log("ana enter");
                            handleClick();
                        }}} onChange={(e) => setText(e.target.value)} value = {text}/>
            <img className = "search-button" src={search} alt="search"  onClick = {() => {handleClick()}}/>
            <div className="v-line-home"></div>
            <div className="speech-home">
            <Speech handleSpeech={handleSpeech}  />
            </div>
            <img className="reset-button" src={reset} alt="reset" id = "reset-home" onClick={() => { handleReset() }} />
        </div>
    );
}

export default Home;
