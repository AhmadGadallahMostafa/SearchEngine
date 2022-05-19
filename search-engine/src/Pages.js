import React from 'react';
import './Search.css';

function Links(props) {
return (
    <div className="links">
    {props.links.map((link) => {
    return (
        <div className="search-container" key ={link.url}>
            <a href={link.url} target="_blank" className="search-title">{link.title}</a>
            <div className="search-description">{link.description}</div>
        </div>
    )
    })}
    </div>
)
}

export default Links;