import './Search.css';
import ReactPaginate from 'react-paginate';
import React, { useState, useEffect } from 'react';
import Links from './Pages';
import NavBar from './NavBar';

function Search() {
    const [links, setLinks] = useState([]);
    const [pageCount, setpageCount] = useState(0);
    const [query, setQuery] = useState(window.location.search);
    let per_page = 10;
    
    /* initializing the search results*/
    useEffect(() => {
        const getLinks = async () => {
            const res = await fetch(
                `http://localhost:8000/Links?${query}_page=1&_per_page=${per_page}`
            );
            console.log(`http://localhost:8000/Links${query}_page=1&_per_page=${per_page}`);
            const data = await res.json();
            const total = res.headers.get("x-total-count");
            setpageCount(Math.ceil(total / per_page));
            setLinks(data);
        }
        console.log(window.location.search);
        setQuery(window.location.search);
        getLinks();
    }, [per_page, query]);

    /* handling pagination*/
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
        <div>
            <NavBar />
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