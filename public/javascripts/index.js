// main.js (for the first tab)
import {socket} from './websocket.js'; // Import shared WebSocket connection

socket.onmessage = function (event) {
    try {
        const searchResults = JSON.parse(event.data);
        updateResults(searchResults);
    } catch (error) {
        console.error("Error processing WebSocket message:", error);
    }
};

// Prevent form submission and send the search query via WebSocket
document.getElementById('searchForm').addEventListener('submit', function (event) {
    event.preventDefault(); // Prevent default form submission
    const query = document.getElementById('query').value.trim();

    if (query === "") {
        alert("Please enter a search term.");
        return;
    }

    if (socket.readyState === WebSocket.OPEN) {
        console.log("Sending query:", query);
        socket.send(JSON.stringify({ type: "search", query: query }));
    } else {
        console.error("WebSocket is not open.");
        alert("WebSocket connection not available. Please try again later.");
    }
});

// Function to dynamically update the search results
function updateResults(searchResults) {
    const resultsDiv = document.getElementById('results');
    if (searchResults && searchResults.length > 0) {
        let html = '';
        searchResults.forEach(item => {
            html += `<h2>Search Results for terms: ${item.searchTerms}</h2>`;
            html += `<div><h3>Overall Sentiment: ${item.sentiment}</h3></div>`;
            html += `<div><a href="#" class="stats-link" data-term="${item.searchTerms}">View Word Frequency Statistics for "${item.searchTerms}"</a></div>`;
            html += `<p>Flesch-Kincaid Grade Level Avg. = ${item.fleschGradeLevelAvg}, Flesch-Kincaid Reading Score Avg. = ${item.fleschEaseScoreAvg}</p>`;
            html += '<ol>';
            item.results.videoList.forEach(video => {
                html += `<li><a href="https://www.youtube.com/watch?v=${video.id}" target="_blank">${video.title}</a></li>`;
            });
            html += '</ol>';
        });
        resultsDiv.innerHTML = html;

        // Attach click event listeners to statistics links
        document.querySelectorAll('.stats-link').forEach(link => {
            link.addEventListener('click', function (event) {
                event.preventDefault();
                const term = this.dataset.term;

                // Open the statistics page with the term in the URL
                const url = `/statistics/${encodeURIComponent(term)}`;
                window.open(url, '_blank');
            });
        });

    } else {
        resultsDiv.innerHTML = "<p>No results found.</p>";
    }
}
