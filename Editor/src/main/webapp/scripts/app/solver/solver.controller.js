'use strict';

angular.module('editorApp')
    .controller('SolverController', ['$scope', '$http', 'Principal', function ($scope, $http, Principal) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });

        $scope.$watch('modelId', function () {
        	$scope.solveModel($scope.modelId);
        });

        $scope.$watch('boundedContexts', function () {
        	$scope.data = JSON.stringify($scope.boundedContexts);
        });
        
        $scope.solveModel = function(modelId) {
        	if(modelId) {
        		var solverConfig = {'weights': {'SAME_ENTITIY': 1}};
        		$http.post($scope.config['engineUrl'] + '/engine/solver/' + modelId, solverConfig).
		    		success(function(data) {
		    			$scope.boundedContexts = data;
	            });
        	}
        }
        
        $scope.loadConfig = function () {
    		$http.get('/api/editor/config').
	    		success(function(data) {
	    			$scope.config = data;
	    			$scope.loadAvailableModels();
            });
        };

        $scope.loadAvailableModels = function () {
    		$http.get($scope.config['engineUrl'] + '/engine/models').
	    		success(function(data) {
	    			$scope.availableModels = data;
            });
        }
        
        $scope.data = null;
        $scope.loadConfig();
        
    }]);
