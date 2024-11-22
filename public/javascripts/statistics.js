import { socket } from './websocket.js'; // Import WebSocket connection

console.log("Statistics script loaded.");
console.log("Search term received from HTML:", searchTerm);

// Ensure WebSocket connection is open
window.onload = function () {
    if (searchTerm) {
        requestStatistics(searchTerm);
    } else {
        document.body.innerHTML = "<p>No search term provided.</p>";
    }
};

// Request statistics from WebSocket server
function requestStatistics(searchTerm) {
    if (socket.readyState === WebSocket.OPEN) {
        console.log("Requesting statistics for term:", searchTerm);
        socket.send(JSON.stringify({ type: "getStatistics", searchTerm }));
    } else {
        console.log("WebSocket not open yet. Waiting for connection...");
        socket.addEventListener('open', function sendStatistics() {
            console.log("WebSocket connection opened. Sending statistics request.");
            socket.send(JSON.stringify({ type: "getStatistics", searchTerm }));
            socket.removeEventListener('open', sendStatistics); // Clean up listener
        });
    }
}

// Handle incoming WebSocket messages
socket.onmessage = function (event) {
    try {
        const data = JSON.parse(event.data);
        console.log("Received WebSocket message:", data);

        if (data.dataType === "statistics") {
            const wordFrequencyMap = data.frequency;

            const wordFrequencyArray = Array.isArray(wordFrequencyMap) ? wordFrequencyMap :
                Object.entries(wordFrequencyMap).map(([word, count]) => ({ word, count }));

            wordFrequencyArray.sort((a, b) => b.count - a.count);

            console.log(wordFrequencyArray);


            if (wordFrequencyArray.length> 0) {
                console.log("Displaying word frequency statistics.");
                const tbody = document.querySelector("tbody");
                tbody.innerHTML = ""; // Clear previous data

                // Loop through the wordFrequency data and create table rows
                wordFrequencyArray.forEach(item  => {
                    const row = document.createElement("tr");
                    const wordCell = document.createElement("td");
                    const countCell = document.createElement("td");

                    wordCell.textContent = item.word;
                    countCell.textContent = item.count;

                    row.appendChild(wordCell);
                    row.appendChild(countCell);
                    tbody.appendChild(row);
                });

                displayTable(1);  // Display the first page after receiving data
            } else {
                document.querySelector("tbody").innerHTML = "<tr><td colspan='2'>No word frequency data available.</td></tr>";
            }
        } else {
            console.log("data type mismatch", data.dataType);
        }
    } catch (error) {
        console.error("Error processing statistics data:", error);
    }
};

// Pagination logic
let currentPage = 1;
const rowsPerPage = 10;

function displayTable(page) {
    const rows = document.querySelectorAll("tbody tr");
    const totalRows = rows.length;
    const totalPages = Math.ceil(totalRows / rowsPerPage);

    if (totalRows === 0) return;

    // Debugging: log the total rows and pages
    console.log(`Total rows: ${totalRows}, Total pages: ${totalPages}`);

    rows.forEach((row) => {
        row.style.display = "none";  // Initially hide all rows
    });

    const start = (page - 1) * rowsPerPage;
    const end = start + rowsPerPage;

    // Show the rows for the current page
    for (let i = start; i < end && i < totalRows; i++) {
        rows[i].style.display = "";
    }

    // Update pagination buttons
    document.getElementById("prevBtn").style.display = page === 1 ? "none" : "inline";
    document.getElementById("nextBtn").style.display = page === totalPages ? "none" : "inline";
    document.getElementById("pageInfo").textContent = `Page ${page} of ${totalPages}`;
}

// Globalize changePage function
window.changePage = function(offset) {
    currentPage += offset;
    displayTable(currentPage);
}
