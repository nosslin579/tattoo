"use strict";

function startTournament() {
    alert("This feature coming soon")
}

angular.module('tournamentApp', []).controller('TournamentController', function ($http) {
    var that = this;

    $http.get('/tournament')
        .then(function (response) {
            that.tournamentList = response.data;
        });

    $http.get('/result')
        .then(function (response) {
            console.log("asdf", response.data);
            that.resultList = response.data;
        });

    $http.get('/schedule')
        .then(function (response) {
            that.scheduleList = response.data;
        });
});

angular.module('tournamentApp').filter('cronToHumanReadable', function () {
    return function (cron) {
        return window.cronstrue.toString(cron);
    }
});


//*
// : [{name: "Atlanta, GA", value: "01b011ac8116"}, {name: "New York, NY", value: "9a6e1bc2c4c8"},…]
//
//     0: {name: "Atlanta, GA", value: "01b011ac8116"}
//     1: {name: "New York, NY", value: "9a6e1bc2c4c8"}
//     2: {name: "Miami, FL", value: "af82be9af5aa"}
//     3: {name: "Dallas, TX", value: "040f9334d182"}
//     4: {name: "London, UK", value: "abb1f9f7c95a"}
//     5: {name: "Chicago, IL", value: "26611098815d"}
//     6: {name: "Los Angeles, CA", value: "aef366ca693c"}
//     7: {name: "San Francisco, CA", value: "41adffac6f58"}
//     8: {name: "Seattle, WA", value: "ab0118cf5df3"}
//     9: {name: "Paris, FR", value: "bd408b384a78"}
//     10: {name: "Frankfurt, DE", value: "c0ba0ac39a00"}
//     11: {name: "Sydney, AU", value: "fd140ca477c9"}
//     12: {name: "Amsterdam, NL", value: "f395d5bf706f"}
//     13: {name: "Toronto, CAN", value: "fcdba9650e5e"*/
angular.module('tournamentApp').filter('tagProServer', function () {
    return function (serverId) {
        switch (serverId) {
            case '01b011ac8116':
                return 'Atlanta, GA';
            case '9a6e1bc2c4c8':
                return 'New York, NY';
            case 'af82be9af5aa':
                return 'Miami, FL';
            case '040f9334d182':
                return 'Dallas, TX';
            case 'abb1f9f7c95a':
                return 'London, UK';
            case '26611098815d':
                return 'Chicago, IL';
            case 'aef366ca693c':
                return 'Los Angeles, CA';
            case '41adffac6f58':
                return 'San Francisco, CA';
            case 'ab0118cf5df3':
                return 'Seattle, WA';
            case 'bd408b384a78':
                return 'Paris, FR';
            case 'c0ba0ac39a00':
                return 'Frankfurt, DE';
            case 'fd140ca477c9':
                return 'Sydney, AU';
            case 'f395d5bf706f':
                return 'Amsterdam, NL';
            case 'fcdba9650e5e':
                return 'Toronto, CAN';
            default:
                return serverId;
        }

    }
});

