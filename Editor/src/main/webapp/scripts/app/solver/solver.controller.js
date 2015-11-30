'use strict';

angular.module('editorApp')
    .controller('SolverController', function ($scope, $http, Principal, VisDataSet, Model, Coupling, $timeout) {
        Principal.identity().then(function(account) {
            $scope.account = account;
            $scope.isAuthenticated = Principal.isAuthenticated;
        });
        
        $scope.availableAlgorithms = ['Leung','Girvan-Newman','MCL'];
        $scope.algorithm = 'Girvan-Newman';
        
        $scope.graphOptions = {
			autoResize: true,
			height: '100%',
			width: '100%'
		};
        
        $scope.graphResize = function(param) {
        	this.fit(); // Zooms out so all nodes fit on the canvas.
        };
        
        
        $scope.updateSelection = function(param) {
        	$scope.$apply(function() {
        		$scope.selectedServiceName = (function () { return; })();
        		$scope.selectedServiceUseCases = (function () { return; })();
        		$scope.selectedServiceRelations = (function () { return; })();
        	});
        	if(param.nodes.length > 0) {
        		var nodeId = param.nodes[0];
        		var listOfUseCases = $scope.result.useCaseResponsibility[nodeId];
        		if(typeof listOfUseCases != 'undefined' && listOfUseCases.length > 0){
        			$scope.$apply(function() { $scope.selectedServiceName = nodeId;});
        			$scope.$apply(function() { $scope.selectedServiceUseCases = listOfUseCases;});
        		}
        		var selectedServiceRelations = [];
        		var relations = $scope.result.relations;
        		for(var relation in relations){
					if(relations[relation].serviceA == nodeId || relations[relation].serviceB == nodeId){
						var r = {};
						r['name'] = relations[relation].serviceA + ' - ' +  relations[relation].serviceB
						r['fields'] = relations[relation].sharedFields;
						selectedServiceRelations.push(r);
					}
        		}
        		$scope.$apply(function() { $scope.selectedServiceRelations = selectedServiceRelations;});
    		}
        };
        
        $scope.graphEvents = {
            	selectNode: $scope.updateSelection,
            	deselectNode: $scope.updateSelection,
            	deselectEdge: $scope.updateSelection,
            	selectEdge: $scope.updateSelection,
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
        										  'numberOfClusters': $scope.numberSlider,
        										  'leungM': $scope.leungM,
        										  'leungDelta': $scope.leungDelta,
        										  'iterations' : $scope.iterations
        							},
        							'priorities': {
        							},
        							'algorithm': $scope.algorithm
        		};
        		angular.forEach($scope.criteria, function(value, index){
        			if ('undefined' !== typeof value.priority) {
        				solverConfig.priorities[value.name] = parseFloat(value.priority)
        			}
        		})
        		
        		$http.post('/api/engine/solver/' + modelId, solverConfig).
		    		success(function(data) {
		        		$scope.result = data;
		    			var serviceNodes = new VisDataSet([]);
		    			var serviceEdges = new VisDataSet([]);
		    			var nodeId = 1;
		    			var services = data.services;
		    			// services
		    			for (var x in services) {
		    				serviceNodes.add({id: services[x].name, shape: 'square', color: '#93D276', label: services[x].name});
		    				for (var y in services[x].dataFields) {
		    					var field = services[x].dataFields[y];
		    					serviceNodes.add({id: nodeId, shape: 'square', size: 10, color: '#909090', label: field});
		    					serviceEdges.add({from: services[x].name, to: nodeId});
		    					nodeId++;
		    				}
		    			}
		    			// service relations
		    			if($scope.showRelations){
			    			for(var relation in data.relations){
			    					serviceEdges.add({from: data.relations[relation].serviceA, to: data.relations[relation].serviceB, color:'#B0DF9B', label: data.relations[relation].score});
			    			}
		    			}
		    	        $scope.graphData = {
	    	            	'nodes': serviceNodes,
	    	            	'edges': serviceEdges
	    	            };
	            });
        	}
        }
        
        
        $scope.criteria = Coupling.all(function(criteria) {
        	$scope.criterion = criteria[0]; 
        });
        
        $scope.priorityMetric = {
        		IGNORE : {value: 0, name: "IGNORE"},
        		XS : {value: 0.5, name: "XS"},
        		S : {value: 1, name: "S"}, 
        		M: {value: 3, name: "M"}, 
        		L : {value: 5, name: "L"},
        		XL : {value: 8, name: "XL"},
        		XXL : {value: 13, name: "XXL"}
        }
        
        $scope.init = function(){
        	angular.forEach($scope.criteria, function(value, index){
        		if(value.type == "PROXIMITY"){
        			value.priority = 3 // todo refactor
        		}else{
        			value.priority = 0.5 
        		}
    		})
        }
        // http://stackoverflow.com/questions/15458609/execute-function-on-page-load
        //TODO: doesn't work well, look for another solution
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
		$scope.showRelations = false;
		
		$scope.leungM = 0.1;
		$scope.leungDelta = 0.05;
		$scope.iterations = 1;


        $scope.availableModels = Model.all();
        
    });
