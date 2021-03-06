import './Search.css';
import ReactPaginate from 'react-paginate';
import React, { useState, useEffect } from 'react';
import Links from './Pages';
import { useNavigate } from "react-router-dom";
import Speech from './Speech';
import './Search.css';
import search from './search.png';
import logo from './logo.png';
import reset from './reset.png';

function Search() {
    const [links, setLinks] = useState([]);
    const [pageCount, setpageCount] = useState(0);
    const [query, setQuery] = useState(window.location.search.split('=')[1]);

    const [text, setText] = useState(null);
    let navigate = useNavigate();
    /* handling search button click*/
    const handleClick = () => {
        console.log(text + " fi click");
        setQuery(text);
        navigate(`/search?q=${text}`);
    }

    let per_page = 10;
    /* initializing the search results*/
    useEffect(() => {
        let t = query;
        if (t.indexOf('%20') === -1) {
            setText(t);
        }
        else {
            setText(t.split('%20').join(' '));
        }
        // var config = {
        //     headers: {'Access-Control-Allow-Origin': '*', 'Accept': 'application/json', 'Content-Type': 'application/json'}
        // };
        // async function getLinks() {
        //     await fetch(`http://localhost:5000/links?q=${query}&_page=1&_per_page=${per_page}`)
        //         .then(response => response.data)
        //         .then(response => {
        //             setLinks(response.links);
        //             setpageCount(Math.ceil(response.headers.get('x-total-count')/per_page));
        //             console.log(response.links);
        // })
        //         .catch(err => console.error(err));
        // }
        
        const getLinks = async () => {
            const res = await fetch(
                `http://localhost:5000/links?q=${query}&_page=1&_per_page=${per_page}`
            );
            const data = await res.json();
            const total = res.headers.get("x-total-count");
            setpageCount(Math.ceil(total / per_page));
            setLinks(data.links);
        }
            // const res = await fetch( 
            //     `http://localhost:5000/links?q=${query}&_page=1&_per_page=${per_page}`
            // );
            // console.log(res + "ana res");
            // const data = await res.json();
        getLinks();
    }, [per_page, query]);

    /* handling pagination*/
    // const fetchLinks = async (currentPage) => {
    //     await axios.get(`http://localhost:5000/links?q=${query}&_page=${currentPage}&_per_page=${per_page}`)
    //     .then(res => res.data).then(res => {
    //         setLinks(res.data.Links);
    //         console.log(links);
    //         return res.data.Links;
    //     })};

    const fetchLinks = async (currentPage) => {
        const res = await fetch(
            `http://localhost:5000/links?q=${query}&_page=${currentPage}&_per_page=${per_page}`
        );
        const data = await res.json();
        return data.links;
    }
        

    const handlePageClick = async (data) => {
        let currentPage = data.selected + 1;
        console.log(currentPage);
        setLinks([]);
        const linksFormServer = await fetchLinks(currentPage);
        setLinks(linksFormServer);
    };

    const handleSpeech = (t) => {
        setText(text + ' ' + t);
    }

    const handleReset = () => {
        setText('');
        console.log(text);
    }

    return (
        <div>
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
            <Links links={links} />
            <ReactPaginate
                previousLabel={"<"}
                nextLabel={">"}
                breakLabel={"..."}
                pageCount={pageCount}
                marginPagesDisplayed={2}
                pageRangeDisplayed={2}
                onPageChange={handlePageClick}
                containerClassName={"pagination justify-content-center"}
                pageClassName={"page-item"}
                pageLinkClassName={"page-link"}
                previousClassName={"page-item"}
                previousLinkClassName={"page-link"}
                nextClassName={"page-item"}
                nextLinkClassName={"page-link"}
                breakClassName={"page-item"}
                breakLinkClassName={"page-link"}
                activeClassName={"active"}
            />
        </div>
    );
}

export default Search;