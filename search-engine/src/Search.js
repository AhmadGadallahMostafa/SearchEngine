import logo from './logo.png';
import './Search.css';
import search from './search.png';
import { useNavigate } from "react-router-dom";
import {useState, useEffect} from 'react';

function Search () {
    const [text, setText] = useState('');
    const [links, setLinks] = useState(null);
    let navigate = useNavigate();
    const handleClick = () => {
        navigate(`/search?q=${text}`);
    }
    useEffect(() => {
        let t = window.location.search.split('=')[1];
        if (t.indexOf('%20') === -1) {
            setText(t);
        }
        else{
            setText(t.split('%20').join(' '));
        }
        fetch('http://localhost:8000/Links')
        .then(res => {
            return res.json();
        })
        .then(data => {
            setLinks(data);
        });
    }, []);
    return (
        <div className="search">
        <div className="search-header">
            <div className="image-container">
                <img className="search-logo"src= {logo} alt="linkdig" onClick={(e) => navigate('/')}/>
            </div>
            <input className= "search-bar-nav" type="text" onChange={(e) => setText(e.target.value)} value = {text}/>
            <img className = "search-button-nav" src={search} alt="search"  onClick = {() => {handleClick()}}/>
        </div>
        {links && links.map(link => {
            return (
                <div className="search-container" key ={link.url}>
                    <div className="search-title">{link.title}</div>
                    <div className="search-description">{link.description}</div>
                </div>
            )
        })}
        </div>
    );
}

export default Search;