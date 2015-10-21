'use strict';

angular.module('editorApp')
    .controller('SolverController', function ($scope, $http, Principal, VisDataSet, Model) {
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
        										'Business Transaction':$scope.writeSlider,
        										'READ_WRITE_BUSINESS_TRANSACTION':$scope.readWriteSlider,
        										'READ_BUSINESS_TRANSACTION':$scope.readSlider
        										},
        							'mclParams': {'inflation': $scope.inflationSlider,
        										  'power': $scope.powerSlider,
        										  'prune': $scope.pruneSlider,
        										  'extraClusters': $scope.extraClusterSlider
        							}
        		};
        		$http.post('/api/engine/solver/' + modelId, solverConfig).
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
        
		$scope.sameEntitySlider = 0.2;
		$scope.compositionSlider = 0.2;
		$scope.writeSlider = 0.4;
		$scope.readSlider = 0.1;
		$scope.readWriteSlider = 0.2;
		
		$scope.powerSlider = 1;
		$scope.inflationSlider = 2;
		$scope.pruneSlider = 0.0;
		$scope.extraClusterSlider = 0;

        $scope.availableModels = Model.all();
        
    });
