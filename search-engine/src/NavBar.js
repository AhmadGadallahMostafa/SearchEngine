import React from "react";
import { useNavigate } from "react-router-dom";
import { useState, useEffect } from 'react';
import Speech from './Speech';
import './Search.css';
import search from './search.png';
import logo from './logo.png';
import reset from './reset.png';

function NavBar({handleQ}) {

    const [text, setText] = useState(null);
    let navigate = useNavigate();
    /* handling search button click*/
    const handleClick = () => {
        handleQ(text);
        navigate(`/search?q=${text}`);
    }

    useEffect(() => {
        let t = window.location.search.split('=')[1];
        if (t.indexOf('%20') === -1) {
            setText(t);
        }
        else {
            setText(t.split('%20').join(' '));
        }
    }, []);

    const handleSpeech = (t) => {
        setText(text + ' ' + t);
    }

    const handleReset = () => {
        setText('');
        console.log(text);
    }

    return (
        <div className="search">
            <div className="search-header">
                <div className="image-container">
                    <img className="search-logo" src={logo} alt="linkdig" onClick={(e) => navigate('/')} />
                </div>
                <input className="search-bar-nav" type="text" onKeyPress={(e) => {
                    if (e.key === "Enter") {
                        handleClick();
                    }
                }} onChange={(e) => setText(e.target.value)} value={text} />
                <img className="search-button-nav" src={search} alt="search" onClick={() => { handleClick() }} />
                <div className="v-line"></div>
                <Speech handleSpeech={handleSpeech} />
                <img className="reset-button" src={reset} alt="reset" onClick={() => { handleReset() }} />
            </div>
        </div>
    );
}

export default NavBar