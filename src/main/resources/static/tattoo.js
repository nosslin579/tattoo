"use strict";

function startTournament() {
    alert("That would be a cool feature, don't you think?")
}

angular.module('tournamentApp', []).controller('TournamentController', function ($http) {
    var that = this;

    $http.get('/tournament')
        .then(function (response) {
            that.tournaments = response.data;
        });
});