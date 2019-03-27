/*------------------------------------------------------------------------------------------------------------------
--	SOURCE FILE:	server.js - The primary script file for the GPS tracking application's
--												web server.
--
--
--	PROGRAM:		GPS Tracker
--
--
--	FUNCTIONS:		server.on('connection', function(sock))
--								app.use(function(req, res, next))
--								app.get("/hello", function(request, response))
--
--	DATE:			Mar 25, 2019
--
--
--	REVISIONS:
--
--
--	DESIGNER:		Ben Zhang, Kiaan Castillo
--
--
--	PROGRAMMER:		Ben Zhang, Kiaan Castillo
--
--
--	NOTES:
--
----------------------------------------------------------------------------------------------------------------------*/

var net = require('net');

const hostname = '0.0.0.0';
const port = 3000;

// create server
var server = net.createServer(function(socket) {
	socket.write('Echo server\r\n');
	socket.pipe(socket);
});

// initiate listening on port
server.listen(port, hostname, () => {
    console.log(`Server running at http://${hostname}:${port}/`);
});

let sockets = [];

/*------------------------------------------------------------------------------------------------------------------
--	FUNCTION:		server-on-connection
--
--
--	DATE:			March 25, 2019
--
--
--	REVISIONS:
--
--
--	DESIGNER:		Ben Zhang, Kiaan Castillo
--
--
--	PROGRAMMER:		Ben Zhang
--
--
--	INTERFACE:		server.on('connection', function(sock))
--
--	RETURNS:
--
--
--	NOTES:			Responsds to connection requests from remote client and creates a connection
--								with the remote client and saves received data to a JSON file based
--								on the sender
----------------------------------------------------------------------------------------------------------------------*/
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

/*------------------------------------------------------------------------------------------------------------------
--	FUNCTION:		app-use
--
--
--	DATE:			March 25, 2019
--
--
--	REVISIONS:
--
--
--	DESIGNER:		Ben Zhang, Kiaan Castillo
--
--
--	PROGRAMMER:		Ben Zhang
--
--
--	INTERFACE:		app.use(function(req, res, next))
--
--	RETURNS:
--
--
--	NOTES:			Sets expected access control and content headers
----------------------------------------------------------------------------------------------------------------------*/
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

/*------------------------------------------------------------------------------------------------------------------
--	FUNCTION:		app-get
--
--
--	DATE:			March 25, 2019
--
--
--	REVISIONS:
--
--
--	DESIGNER:		Ben Zhang, Kiaan Castillo
--
--
--	PROGRAMMER:		Ben Zhang, Kiaan Castillo
--
--
--	INTERFACE:		app.get("/hello", function(request, response) )
--
--	RETURNS:
--
--
--	NOTES:			Callback function to be executed when the server receives a GET request.
----------------------------------------------------------------------------------------------------------------------*/
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
