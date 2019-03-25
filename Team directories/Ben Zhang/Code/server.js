var net = require('net');

const hostname = '0.0.0.0';
const port = 3000;

var server = net.createServer(function(socket) {
	socket.write('Echo server\r\n');
	socket.pipe(socket);
});

server.listen(port, hostname, () => {
    console.log(`Server running at http://${hostname}:${port}/`);
});

let sockets = [];

server.on('connection', function(sock) {
    console.log('CONNECTED: ' + sock.remoteAddress + ':' + sock.remotePort);
    sockets.push(sock);

    sock.on('data', function(data) {
        console.log('DATA ' + sock.remoteAddress + ': ' + data);

        // Saves received JSON into file
        const jsonfile = require('jsonfile')
        let userData = JSON.parse(data);
        const file = './users/' +userData.name + '.json'
        const obj = data.toString();
        jsonfile.writeFile(file, obj, function(err) {
            if (err) console.error(err)
        })

        // Write the data back to all the connected, the client will receive it as data from the server
        sockets.forEach(function(sock, index, array) {
            sock.write(sock.remoteAddress + ':' + sock.remotePort + " said " + data + '\n');
        });
    });

    // Add a 'close' event handler to this instance of socket
    sock.on('close', function(data) {
        let index = sockets.findIndex(function(o) {
            return o.remoteAddress === sock.remoteAddress && o.remotePort === sock.remotePort;
        })
        if (index !== -1) sockets.splice(index, 1);
        console.log('CLOSED: ' + sock.remoteAddress + ' ' + sock.remotePort);
    });
});


// http server

//Import the necessary libraries/declare the necessary objects
var express = require("express");
var myParser = require("body-parser");
var app = express();

app.use(function(req, res, next) {
    myParser.urlencoded({extended : true});
    var allowedOrigins = ['http://18.217.49.198'];
    var origin = req.headers.origin;
    if(allowedOrigins.indexOf(origin) > -1){
        res.setHeader('Access-Control-Allow-Origin', origin);
    }
    //res.header('Access-Control-Allow-Origin', 'http://127.0.0.1:8020');
    res.header('Access-Control-Allow-Methods', 'GET, OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization');
    res.header('Access-Control-Allow-Credentials', true);
    return next();
});

app.get("/hello", function(request, response) {
    //console.log(request.params); /* This prints the  JSON document received (if it is a JSON document) */

    let userlist = [];
    let fs = require('fs');
    const jsonfile = require('jsonfile')

    fs.readdir('./users', function(err, filenames) {
        if(err) {
            onerror(err);
            return;
        }
        filenames.forEach(function(file, index) {
            jsonfile.readFile('./users/' + file, function (err, obj) {
                if (err) console.error(err)
                userlist.push(obj);
                if(index == (filenames.length - 1)) {
                    console.log("user list is :" + userlist);
                    response.send(JSON.stringify(userlist));
                }
            })
        })
    })
});

app.listen(8080);
