document.addEventListener("DOMContentLoaded", () => {
    const statusMessage = document.getElementById("status-message");

    fetch('/api/status')
        .then(response => response.text())
        .then(data => {
            statusMessage.textContent = data;
            statusMessage.style.color = "green";
        })
        .catch(error => {
            console.error('Error:', error);
            statusMessage.textContent = "Error: System not reachable.";
            statusMessage.style.color = "red";
        });
});
