import logo from './logo.png';
import './Search.css';
import search from './search.png';
import ReactPaginate from 'react-paginate';
import Links from './Pages';
import { useNavigate } from "react-router-dom";
import {useState, useEffect} from 'react';

function Search () {
    const [text, setText] = useState('');
    const [links, setLinks] = useState([]);
    const [pageCount, setpageCount] = useState(0);

    let per_page = 10;
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
        const getLinks = async () => {
            const res = await fetch(
                `http://localhost:8000/Links?_page=1&_per_page=${per_page}`
            );
            const data = await res.json();
            const total = res.headers.get("x-total-count");
            setpageCount(Math.ceil(total / per_page));
            setLinks(data);
        }
        console.log(pageCount);
        getLinks();
    }, [per_page]);

    const fetchLinks = async (currentPage) => {
        const res = await fetch(
          `http://localhost:8000/Links?_page=${currentPage}&_per_page=${per_page}`
        );
        const data = await res.json();
        return data;
      };

    const handlePageClick = async (data) => {
        let currentPage = data.selected + 1;
        console.log(currentPage);
        setLinks([]);
        const linksFormServer = await fetchLinks(currentPage);
        setLinks(linksFormServer);
    };

    return (
        <div className="search">
        <div className="search-header">
            <div className="image-container">
                <img className="search-logo"src= {logo} alt="linkdig" onClick={(e) => navigate('/')}/>
            </div>
            <input className= "search-bar-nav" type="text" onKeyPress={(e) => {
                        if (e.key === "Enter") {
                            handleClick();
                        }}} onChange={(e) => setText(e.target.value)} value = {text}/>
            <img className = "search-button-nav" src={search} alt="search"  onClick = {() => {handleClick()}}/>
        </div>
        <Links links={links}/>
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