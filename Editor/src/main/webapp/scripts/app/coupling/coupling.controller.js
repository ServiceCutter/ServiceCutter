'use strict';

angular.module('editorApp')
    .controller('CouplingController', function ($scope, $http, Principal, Coupling) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });

        $scope.criteria = Coupling.all(function(criteria) {
        	$scope.criterion = criteria[0];
        });
        
        $scope.tabSwitched = function (criterion) {
        	$scope.criterion = criterion;
        };
        
        $scope.criterion = null;
    });
