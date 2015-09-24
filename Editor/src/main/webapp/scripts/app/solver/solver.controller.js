'use strict';

angular.module('editorApp')
    .controller('SolverController', ['$scope', '$http', 'Principal', 'VisDataSet', function ($scope, $http, Principal, VisDataSet) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });
        
        $scope.graphOptions = {
			autoResize: true,
			height: '400',
			width: '100%'
		};
        
        $scope.$watch('modelId', function () {
        	$scope.solveModel($scope.modelId);
        });

        $scope.solveModel = function(modelId) {
        	if(parseInt(modelId) > 0) {
        		var solverConfig = {'weights': {'SAME_ENTITIY': 1}};
        		$http.post($scope.config['engineUrl'] + '/engine/solver/' + modelId, solverConfig).
		    		success(function(data) {
		    			$scope.boundedContexts = data;
		    			var contextNodes = new VisDataSet([]);
		    			var contextEdges = new VisDataSet([]);
		    			var nodeId = 1;
		    			var currentContextId = 0;
		    			for (var x in data) {
		    				contextNodes.add({id: nodeId, shape: 'square', color: '#93D276', label: 'Bounded Context'});
		    				currentContextId = nodeId;
	    					nodeId++;
		    				for (var y in data[x].dataFields) {
		    					var field = data[x].dataFields[y];
		    					contextNodes.add({id: nodeId, shape: 'square', size: 10, color: '#909090', label: field});
		    					contextEdges.add({from: currentContextId, to: nodeId});
		    					nodeId++;
		    				}
		    			}
		    	        $scope.graphData = {
	    	            	'nodes': contextNodes,
	    	            	'edges': contextEdges
	    	            };
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
        
        $scope.loadConfig();
        
    }]);
