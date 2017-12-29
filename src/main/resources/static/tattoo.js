"use strict";

var statusXhr = new XMLHttpRequest();
statusXhr.open('GET', 'tournament');
statusXhr.onload = function () {
    console.log("Responese ", statusXhr);
    var statusDiv = document.getElementById('status');
    if (statusXhr.status === 200) {
        var status = JSON.parse(statusXhr.response);
        statusDiv.innerHTML = JSON.stringify(status, null, 2);
    } else {
        statusDiv.innerHTML = 'Request failed.  Returned status of ' + statusXhr.status;
    }
};
statusXhr.send();


function startTournament() {
    //var startTournamentXhr = new XMLHttpRequest();
    //startTournamentXhr.open('POST', 'tournament');
    //startTournamentXhr.onload = function () {
    //    console.log("Responese ", startTournamentXhr);
    //};
    //startTournamentXhr.send();
    alert("That would be a cool feature, don't you think?")
}
