"use strict";

angular.module('tournamentApp', []).controller('TournamentController', function ($scope, $http) {
    var that = this;

    $scope.startTournament = function () {
        $http(
            {
                url: '/tournament',
                dataType: 'json',
                method: 'POST',
                data: {},
                headers: {"Content-Type": "application/json"}
            })
            .then(function (response) {
                console.log("Tournament started", response);
            });
    };

    $http.get('/tournament')
        .then(function (response) {
            that.tournamentList = response.data;
        });

    $http.get('/result')
        .then(function (response) {
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

