'use strict';

angular.module('editorApp')
    .controller('SolverController', function ($scope, $http, Principal, VisDataSet, Model, Coupling, $timeout) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });
        
        $scope.graphOptions = {
			autoResize: true,
			height: '100%',
			width: '100%'
		};
        
        $scope.graphResize = function(param) {
        	this.fit(); // Zooms out so all nodes fit on the canvas.
        };

        $scope.graphEvents = {
        	resize: $scope.graphResize
        };
        
        $scope.$watch('modelId', function () {
        	$scope.solve();
        });
        
        $scope.solve=function(){
        	 $scope.solveModel($scope.modelId);
        }

        $scope.solveModel = function(modelId) {
        	if(parseInt(modelId) > 0) {
        		var solverConfig = {'weights': {'Same Entity': $scope.sameEntitySlider,
        										'Composition':$scope.compositionSlider,
        										'Aggregation':$scope.aggregationSlider,
        										'Shared Field Access':$scope.sharedFieldAccessSlider
        							},
        							'algorithmParams': {'inflation': $scope.inflationSlider,
        										  'power': $scope.powerSlider,
        										  'prune': $scope.pruneSlider,
        										  'extraClusters': $scope.extraClusterSlider,
        										  'numberOfClusters': $scope.numberSlider
        							},
        							'priorities': {
        							}
        		};
        		angular.forEach($scope.criteria, function(value, index){
        			if ('undefined' !== typeof value.priority) {
        				solverConfig.priorities[value.name] = parseFloat(value.priority)
        			}
        		})
        		
        		$http.post('/api/engine/solver/' + modelId, solverConfig).
		    		success(function(data) {
		    			$scope.boundedContexts = data;
		    			var contextNodes = new VisDataSet([]);
		    			var contextEdges = new VisDataSet([]);
		    			var nodeId = 1;
		    			var currentContextId = 0;
		    			for (var x in data) {
		    				contextNodes.add({id: nodeId, shape: 'square', color: '#93D276', label: 'Service'});
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
        
        $scope.criteria = Coupling.all(function(criteria) {
        	$scope.criterion = criteria[0]; 
        });
        
        $scope.priorityMetric = {
        		XS : {value: 0.5, name: "XS"},
        		S : {value: 1, name: "S"}, 
        		M: {value: 3, name: "M"}, 
        		L : {value: 5, name: "L"},
        		XL : {value: 8, name: "XL"},
        		XXL : {value: 13, name: "XXL"}
        }
        
        $scope.init = function(){
        	angular.forEach($scope.criteria, function(value, index){
    			value.priority = 3 // todo refactor 
    		})
        }
        // http://stackoverflow.com/questions/15458609/execute-function-on-page-load
        $timeout($scope.init)
        
		$scope.sameEntitySlider = 0.2;
		$scope.compositionSlider = 0.2;
		$scope.aggregationSlider = 0.2;
		$scope.sharedFieldAccessSlider = 0.4;
		
		$scope.powerSlider = 1;
		$scope.inflationSlider = 2;
		$scope.pruneSlider = 0.0;
		$scope.extraClusterSlider = 0;
		$scope.numberSlider = 3;


        $scope.availableModels = Model.all();
        
    });
