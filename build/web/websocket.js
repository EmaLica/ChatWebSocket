var ws;

function connect() {
    document.getElementById("yourUsername").value = "Il tuo username: " + document.getElementById("username").value;
    var username = document.getElementById("username").value;
    var host = document.location.host;
    var pathname = document.location.pathname;

    ws = new WebSocket("ws://" + host + pathname + "chat/" + username);

    ws.onmessage = function (event) {
        var log = document.getElementById("log");
        console.log(event.data);
        var message = JSON.parse(event.data);
        log.innerHTML += message.from + " : " + message.content + "\n";
    };
}

function send() {
    var content = document.getElementById("msg").value;
    var username = document.getElementById("username-destinatario").value;
    var json = JSON.stringify({
        "content": content,
        "username": username
    });
    ws.send(json);
    document.getElementById("username-destinatario").value = "";
    document.getElementById("msg").value = "";
}