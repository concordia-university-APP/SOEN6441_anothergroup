// websocket.js
export const socket = new WebSocket("ws://localhost:9000/ws/search");

socket.onopen = function (event) {
    console.log("WebSocket connection established.");
};

socket.onerror = function (event) {
    console.error("WebSocket error:", event);
};

socket.onclose = function (event) {
    console.warn(`WebSocket closed. Code: ${event.code}, Reason: ${event.reason}`);
};

